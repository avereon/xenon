package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.skill.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.throwable.NoToolRegisteredException;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.zerra.javafx.Fx;
import javafx.application.Platform;
import lombok.CustomLog;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@CustomLog
public class ToolManager implements Controllable<ToolManager> {

	private final Program program;

	private final Map<String, String> aliases;

	private final Map<Class<? extends ProgramTool>, ToolRegistration> toolClassMetadata;

	private final Map<AssetType, List<Class<? extends ProgramTool>>> assetTypeToolClasses;

	public ToolManager( Program program ) {
		this.program = program;
		toolClassMetadata = new ConcurrentHashMap<>();
		assetTypeToolClasses = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();
	}

	public Program getProgram() {
		return program;
	}

	public void registerTool( AssetType assetType, ToolRegistration metadata ) {
		Class<? extends ProgramTool> type = metadata.getType();
		toolClassMetadata.put( type, metadata );

		List<Class<? extends ProgramTool>> assetTypeToolClasses = this.assetTypeToolClasses.computeIfAbsent( assetType, k -> new CopyOnWriteArrayList<>() );
		assetTypeToolClasses.add( type );

		log.atFine().log( "Tool registered: assetType=%s -> tool=%s", assetType.getKey(), type.getName() );
	}

	public void unregisterTool( AssetType assetType, Class<? extends ProgramTool> type ) {
		toolClassMetadata.remove( type );

		List<Class<? extends ProgramTool>> assetTypeTools = assetTypeToolClasses.get( assetType );
		if( assetTypeTools != null ) assetTypeTools.remove( type );

		log.atFine().log( "Tool unregistered: assetType=%s -> tool=%s", assetType.getKey(), type.getName() );
	}

	/**
	 * Open a tool using the specified request. The request contains all the
	 * information regarding the request including the asset.
	 *
	 * @param request The open asset request
	 * @return The tool for the request or null if a tool was not created
	 * @apiNote Should be called from a {@link TaskManager} thread
	 */
	public ProgramTool openTool( OpenAssetRequest request ) throws NoToolRegisteredException {
		// Check the calling thread
		TaskManager.taskThreadCheck();

		// Verify the request parameters
		Asset asset = request.getAsset();
		if( asset == null ) throw new NullPointerException( "Asset cannot be null" );

		// Get the asset type to look up the registered tool classes
		AssetType assetType = asset.getType();

		// Determine which tool class will be used
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		if( toolClass == null ) toolClass = determineToolClassForAssetType( assetType );
		if( toolClass == null ) throw new NoToolRegisteredException( "No tools registered for: " + assetType );
		request.setToolClass( toolClass );

		// Check that the tool is registered
		ToolRegistration toolRegistration = toolClassMetadata.get( toolClass );
		if( toolRegistration == null ) throw new IllegalArgumentException( "Tool not registered: " + toolClass );

		// Determine how many instances the tool allows
		ToolInstanceMode instanceMode = getToolInstanceMode( toolClass );

		// Before checking for existing tools, the workpane needs to be determined
		WorkpaneView view = request.getView();
		Workpane pane = view == null ? null : view.getWorkpane();
		if( pane == null ) pane = program.getWorkspaceManager().getActiveWorkpane();
		if( pane == null ) throw new NullPointerException( "Workpane cannot be null when opening tool" );

		ProgramTool tool = findToolInPane( pane, toolClass );

		// If the instance mode is SINGLETON, check for an existing tool in the workpane
		if( instanceMode == ToolInstanceMode.SINGLETON && tool != null ) {
			final Workpane finalPane = pane;
			final ProgramTool finalTool = tool;
			if( request.isSetActive() ) Fx.run( () -> finalPane.setActiveTool( finalTool ) );
			return tool;
		}

		try {
			tool = getToolInstance( request );
		} catch( Exception exception ) {
			log.atSevere().withCause( exception ).log( "Error creating tool: %s", request.getToolClass().getName() );
			return null;
		}

		// Now that we have a tool...open dependent assets and associated tools
		if( !openDependencies( request, tool ) ) return null;

		// Determine the placement override
		// A null value allows the tool to determine its placement
		Workpane.Placement placementOverride = toolClassMetadata.get( tool.getClass() ).getPlacement();

		final Workpane finalPane = pane;
		final ProgramTool finalTool = tool;
		final WorkpaneView finalView = view;
		scheduleWaitForReady( request, finalTool );
		Fx.run( () -> finalPane.openTool( finalTool, finalView, placementOverride, request.isSetActive() ) );

		return tool;
	}

	private boolean openDependencies( OpenAssetRequest request, ProgramTool tool ) {
		try {
			for( URI dependency : tool.getAssetDependencies() ) {
				program.getAssetManager().openAsset( dependency, true, false ).get();
			}
			return true;
		} catch( InterruptedException ignored ) {
		} catch( ExecutionException exception ) {
			log.atSevere().withCause( exception ).log( "Error opening tool dependencies: %s", request.getToolClass().getName() );
		}
		return false;
	}

	/**
	 * Called from the {@link UiRegenerator} to restore a tool.
	 *
	 * @param request The open asset request for restoring the tool
	 * @param toolClassName The tool class name
	 * @return The restored tool
	 * @apiNote Could be called from a @code{task thread} or an @code{FX application thread}
	 */
	ProgramTool restoreTool( OpenAssetRequest request, String toolClassName ) {
		// Run this class through the alias map
		toolClassName = getToolClassName( toolClassName );

		// Find the registered tool type metadata
		ToolRegistration toolRegistration = null;
		for( ToolRegistration metadata : toolClassMetadata.values() ) {
			if( metadata.getType().getName().equals( toolClassName ) ) {
				toolRegistration = metadata;
				break;
			}
		}

		// Check for unregistered tool type
		if( toolRegistration == null ) {
			log.atSevere().log( "Tool class not registered: %s", toolClassName );
			return null;
		}

		request.setToolClass( toolRegistration.getType() );

		try {
			ProgramTool tool = getToolInstance( request );
			scheduleWaitForReady( request, tool );
			return tool;
		} catch( Exception exception ) {
			log.atSevere().withCause( exception ).log( "Error creating tool: %s", request.getToolClass().getName() );
			return null;
		}
	}

	public ToolInstanceMode getToolInstanceMode( Class<? extends ProgramTool> toolClass ) {
		ToolInstanceMode instanceMode = toolClassMetadata.get( toolClass ).getInstanceMode();
		if( instanceMode == null ) instanceMode = ToolInstanceMode.UNLIMITED;
		return instanceMode;
	}

	private Class<? extends ProgramTool> determineToolClassForAssetType( AssetType assetType ) {
		Class<? extends ProgramTool> toolClass = null;
		List<Class<? extends ProgramTool>> toolClasses = assetTypeToolClasses.get( assetType );
		if( toolClasses == null ) {
			// There are no registered tools for the asset type
			log.atWarning().log( "No tools registered for asset type %s", assetType.getKey() );
		} else if( toolClasses.size() == 1 ) {
			// There is exactly one tool registered for the asset type
			log.atFine().log( "One tool registered for asset type %s", assetType.getKey() );
			toolClass = toolClasses.get( 0 );
		} else {
			// There is more than one tool registered for the asset type
			log.atWarning().log( "Multiple tools registered for asset type %s", assetType.getKey() );
			toolClass = toolClasses.get( 0 );
		}
		return toolClass;
	}

	public Product getToolProduct( ProgramTool tool ) {
		ToolRegistration data = toolClassMetadata.get( tool.getClass() );
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
	private ProgramTool getToolInstance( OpenAssetRequest request ) throws Exception {
		Asset asset = request.getAsset();
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		ProgramProduct product = toolClassMetadata.get( toolClass ).getProduct();

		// In order for this to be safe on any thread a task needs to be created
		// that is then run on the FX platform thread and the result obtained on
		// the calling thread.
		String taskName = Rb.text( BundleKey.TOOL, "tool-manager-create-tool", toolClass.getSimpleName() );
		Task<ProgramTool> createToolTask = Task.of( taskName, () -> {
			// Create the new tool instance
			Constructor<? extends ProgramTool> constructor = toolClass.getConstructor( ProgramProduct.class, Asset.class );
			ProgramTool tool = constructor.newInstance( product, asset );

			// Set the id before using settings
			tool.setUid( request.getToolId() == null ? IdGenerator.getId() : request.getToolId() );
			tool.getSettings().set( Tool.SETTINGS_TYPE_KEY, tool.getClass().getName() );
			tool.getSettings().set( Asset.SETTINGS_URI_KEY, tool.getAsset().getUri() );
			if( tool.getAsset().getType() != null ) tool.getSettings().set( Asset.SETTINGS_TYPE_KEY, tool.getAsset().getType().getKey() );
			addToolListenerForSettings( tool );
			log.atFine().log( "Tool instance created: %s", tool.getClass().getName() );
			return tool;
		} );

		if( Platform.isFxApplicationThread() ) {
			createToolTask.run();
		} else {
			Fx.run( createToolTask );
		}

		// Return the task
		return createToolTask.get( 10, TimeUnit.SECONDS );
	}

	private void addToolListenerForSettings( ProgramTool tool ) {
		tool.addEventHandler( ToolEvent.ADDED,
			e -> ((ProgramTool)e.getTool()).getSettings().set( UiFactory.PARENT_WORKPANEVIEW_ID, e.getTool().getToolView().getUid() )
		);
		tool.addEventHandler( ToolEvent.ORDERED, e -> ((ProgramTool)e.getTool()).getSettings().set( "order", e.getTool().getTabOrder() ) );
		tool.addEventHandler( ToolEvent.ACTIVATED, e -> ((ProgramTool)e.getTool()).getSettings().set( "active", true ) );
		tool.addEventHandler( ToolEvent.DEACTIVATED, e -> ((ProgramTool)e.getTool()).getSettings().set( "active", null ) );
		tool.addEventHandler( ToolEvent.CLOSED, e -> ((ProgramTool)e.getTool()).getSettings().delete() );
	}

	private ProgramTool findToolInPane( Workpane pane, Class<? extends Tool> type ) {
		return (ProgramTool)pane.getTools().stream().filter( t -> t.getClass() == type ).findAny().orElse( null );
	}

	/**
	 * This method creates a task that waits for the asset to be ready then calls the tool assetReady() method.
	 *
	 * @param request The open tool request object
	 * @param tool The tool that should be notified when the asset is ready
	 */
	private void scheduleWaitForReady( OpenAssetRequest request, ProgramTool tool ) {
		tool.waitForReady( request );
	}

}
