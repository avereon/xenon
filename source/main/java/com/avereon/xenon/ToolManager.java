package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.util.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.util.LogUtil;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;
import com.avereon.xenon.asset.AssetListener;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.tool.ToolInstanceMode;
import com.avereon.xenon.tool.ToolMetadata;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneView;
import javafx.application.Platform;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

//import javafx.concurrent.Task;

public class ToolManager implements Controllable<ToolManager> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Map<Class<? extends ProgramTool>, ToolMetadata> toolClassMetadata;

	private Map<AssetType, List<Class<? extends ProgramTool>>> assetTypeToolClasses;

	private Map<String, String> aliases;

	public ToolManager( Program program ) {
		this.program = program;
		toolClassMetadata = new ConcurrentHashMap<>();
		assetTypeToolClasses = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();
	}

	public void registerTool( AssetType assetType, ToolMetadata metadata ) {
		Class<? extends ProgramTool> type = metadata.getType();
		toolClassMetadata.put( type, metadata );

		List<Class<? extends ProgramTool>> assetTypeToolClasses = this.assetTypeToolClasses.computeIfAbsent(
			assetType,
			k -> new CopyOnWriteArrayList<>()
		);
		assetTypeToolClasses.add( type );

		log.debug( "Tool registered: assetType={} -> tool={}", assetType.getKey(), type.getName() );
	}

	public void unregisterTool( AssetType assetType, Class<? extends ProgramTool> type ) {
		toolClassMetadata.remove( type );

		List<Class<? extends ProgramTool>> assetTypeTools = assetTypeToolClasses.get( assetType );
		if( assetTypeTools != null ) assetTypeTools.remove( type );

		log.debug( "Tool unregistered: assetType={} -> tool={}", assetType.getKey(), type.getName() );
	}

	/**
	 * @param request
	 * @return
	 * @apiNote Should be called from a @code{TaskThread}
	 */
	public ProgramTool openTool( OpenToolRequest request ) {
		// Check the calling thread
		TaskManager.taskThreadCheck();

		// Verify the request parameters
		Asset asset = request.getAsset();
		if( asset == null ) throw new NullPointerException( "asset cannot be null" );

		// Get the asset type to look up the registered tool classes
		AssetType assetType = asset.getType();

		// Determine which tool class will be used
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		if( toolClass == null ) toolClass = determineToolClassForAssetType( assetType );
		if( toolClass == null ) throw new NullPointerException( "No tools registered for: " + assetType );
		request.setToolClass( toolClass );

		// Check that the tool is registered
		ToolMetadata toolMetadata = toolClassMetadata.get( toolClass );
		if( toolMetadata == null ) throw new IllegalArgumentException( "Tool not registered: " + toolClass );

		// Determine how many instances the tool allows
		ToolInstanceMode instanceMode = getToolInstanceMode( toolClass );

		// Before checking for existing tools, the workpane needs to be determined
		Workpane pane = request.getPane();
		WorkpaneView view = request.getView();
		if( pane == null && view != null ) pane = view.getWorkpane();
		if( pane == null ) pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		if( pane == null ) throw new NullPointerException( "Workpane cannot be null when opening tool" );

		ProgramTool tool = null;
		// If the instance mode is SINGLETON, check for an existing tool in the workpane
		if( instanceMode == ToolInstanceMode.SINGLETON ) tool = findToolInPane( pane, toolClass );
		final boolean alreadyExists = tool != null;

		if( alreadyExists ) {
			final Workpane finalPane = pane;
			final ProgramTool finalTool = tool;
			if( request.isSetActive() ) Platform.runLater( () -> finalPane.setActiveTool( finalTool ) );
			return tool;
		} else {
			tool = getToolInstance( request );
		}

		// Verify there is a tool to use
		if( tool == null ) {
			String title = program.rb().text( "program", "no-tool-for-asset-title" );
			String message = program.rb().text( "program", "no-tool-for-asset-message", asset.getUri().toString() );
			program.getNoticeManager().warning( title, message, asset.getName() );
			return null;
		}

		// Now that we have a tool...open dependent assets and associated tools
		for( URI dependency : tool.getAssetDependencies() ) {
			program.getAssetManager().open( dependency, true, false );
		}

		// Determine the placement override
		// A null value allows the tool to determine its placement
		Workpane.Placement placementOverride = toolClassMetadata.get( tool.getClass() ).getPlacement();

		final Workpane finalPane = pane;
		final ProgramTool finalTool = tool;
		Platform.runLater( () -> finalPane.openTool( finalTool, placementOverride, request.isSetActive() ) );

		scheduleAssetReady( request, finalTool );

		return tool;
	}

	/**
	 * @param openToolRequest
	 * @param toolClassName
	 * @return The restored tool
	 * @apiNote Could be called from a @code{task thread} or an @code{FX application thread}
	 */
	ProgramTool restoreTool( OpenToolRequest openToolRequest, String toolClassName ) {
		// Run this class through the alias map
		toolClassName = getToolClassName( toolClassName );

		// Find the registered tool type metadata
		ToolMetadata toolMetadata = null;
		for( ToolMetadata metadata : toolClassMetadata.values() ) {
			if( metadata.getType().getName().equals( toolClassName ) ) {
				toolMetadata = metadata;
				break;
			}
		}

		// Check for unregistered tool type
		if( toolMetadata == null ) {
			log.error( "Tool class not registered: " + toolClassName );
			return null;
		}

		openToolRequest.setToolClass( toolMetadata.getType() );

		ProgramTool tool = getToolInstance( openToolRequest );

		if( tool != null ) scheduleAssetReady( openToolRequest, tool );

		return tool;
	}

	private ToolInstanceMode getToolInstanceMode( Class<? extends ProgramTool> toolClass ) {
		ToolInstanceMode instanceMode = toolClassMetadata.get( toolClass ).getInstanceMode();
		if( instanceMode == null ) instanceMode = ToolInstanceMode.UNLIMITED;
		return instanceMode;
	}

	private Class<? extends ProgramTool> determineToolClassForAssetType( AssetType assetType ) {
		Class<? extends ProgramTool> toolClass = null;
		List<Class<? extends ProgramTool>> toolClasses = assetTypeToolClasses.get( assetType );
		if( toolClasses == null ) {
			// There are no registered tools for the asset type
			log.warn( "No tools registered for asset type {}", assetType.getKey() );
		} else if( toolClasses.size() == 1 ) {
			// There is exactly one tool registered for the asset type
			log.debug( "One tool registered for asset type {}", assetType.getKey() );
			toolClass = toolClasses.get( 0 );
		} else {
			// There is more than one tool registered for the asset type
			log.warn( "Multiple tools registered for asset type {}", assetType.getKey() );
			toolClass = toolClasses.get( 0 );
		}
		return toolClass;
	}

	public Product getToolProduct( ProgramTool tool ) {
		ToolMetadata data = toolClassMetadata.get( tool.getClass() );
		return data == null ? null : data.getProduct();
	}

	public String getToolClassName( String className ) {
		String alias = null;
		if( className != null ) alias = aliases.get( className );
		return alias == null ? className : alias;
	}

	public void addToolAlias( String oldName, Class<? extends ProgramTool> newClass ) {
		addToolAlias( oldName, newClass.getName() );
	}

	public void addToolAlias( String oldName, String newName ) {
		aliases.putIfAbsent( oldName, newName );
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public ToolManager start() {
		return this;
	}

	@Override
	public ToolManager stop() {
		return this;
	}

	// Safe to call on any thread
	private ProgramTool getToolInstance( OpenToolRequest request ) {
		Asset asset = request.getAsset();
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		ProgramProduct product = toolClassMetadata.get( toolClass ).getProduct();

		String taskName = program.rb().text( BundleKey.TOOL, "tool-manager-create-tool", toolClass.getSimpleName() );
		Task<ProgramTool> createToolTask = Task.of( taskName, () -> {
			// Have to have a ProductTool to support modules
			try {
				// Create the new tool instance
				Constructor<? extends ProgramTool> constructor = toolClass.getConstructor( ProgramProduct.class, Asset.class );
				ProgramTool tool = constructor.newInstance( product, asset );

				// Set the id before using settings
				tool.setUid( request.getId() == null ? IdGenerator.getId() : request.getId() );
				tool.getSettings().set( "type", tool.getClass().getName() );
				tool.getSettings().set( "uri", tool.getAsset().getUri() );
				addToolListenerForSettings( tool );
				log.debug( "Tool instance created: " + tool.getClass().getName() );
				return tool;
			} catch( Exception exception ) {
				log.error( "Error creating instance: " + toolClass.getName(), exception );
			}

			return null;
		} );

		if( Platform.isFxApplicationThread() ) {
			createToolTask.run();
		} else {
			Platform.runLater( createToolTask );
		}

		try {
			return createToolTask.get( 10, TimeUnit.SECONDS );
		} catch( Exception exception ) {
			log.error( "Error creating tool: " + request.getToolClass().getName(), exception );
			return null;
		}
	}

	private void addToolListenerForSettings( ProgramTool tool ) {
		tool.addToolListener( ( event ) -> {
			ProgramTool eventTool = (ProgramTool)event.getTool();
			switch( event.getType() ) {
				case ADDED: {
					eventTool.getSettings().set( UiFactory.PARENT_WORKPANEVIEW_ID, eventTool.getToolView().getViewId() );
					break;
				}
				case ORDERED: {
					eventTool.getSettings().set( "order", eventTool.getTabOrder() );
					break;
				}
				case ACTIVATED: {
					eventTool.getSettings().set( "active", true );
					break;
				}
				case DEACTIVATED: {
					eventTool.getSettings().set( "active", null );
					break;
				}
				case CLOSED: {
					eventTool.getSettings().delete();
				}
			}
		} );
	}

	/**
	 * This method creates a task that waits for the asset to be ready then calls the tool assetReady() method.
	 *
	 * @param request The open tool request object
	 * @param tool The tool that should be notified when the asset is ready
	 */
	private void scheduleAssetReady( OpenToolRequest request, ProgramTool tool ) {
		Asset asset = request.getAsset();
		asset.callWhenReady( new AssetListener() {

			@Override
			public void eventOccurred( AssetEvent event ) {
				asset.removeAssetListener( this );
				Platform.runLater( () -> tool.callAssetReady( new OpenToolRequestParameters( request ) ) );
			}

		} );
	}

	private ProgramTool findToolInPane( Workpane pane, Class<? extends Tool> type ) {
		for( Tool paneTool : pane.getTools() ) {
			if( paneTool.getClass() == type ) return (ProgramTool)paneTool;
		}
		return null;
	}

}
