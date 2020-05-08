package com.avereon.xenon;

import com.avereon.event.EventHandler;
import com.avereon.product.Product;
import com.avereon.util.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.util.Log;
import com.avereon.venza.javafx.FxUtil;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.throwable.NoToolRegisteredException;
import com.avereon.xenon.workpane.*;
import javafx.application.Platform;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.*;

public class ToolManager implements Controllable<ToolManager> {

	private static final System.Logger log = System.getLogger( MethodHandles.lookup().lookupClass().getName() );

	private Program program;

	private Map<Class<? extends ProgramTool>, ToolRegistration> toolClassMetadata;

	private Map<AssetType, List<Class<? extends ProgramTool>>> assetTypeToolClasses;

	private Map<String, String> aliases;

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

		log.log( DEBUG, "Tool registered: assetType={0} -> tool={1}", assetType.getKey(), type.getName() );
	}

	public void unregisterTool( AssetType assetType, Class<? extends ProgramTool> type ) {
		toolClassMetadata.remove( type );

		List<Class<? extends ProgramTool>> assetTypeTools = assetTypeToolClasses.get( assetType );
		if( assetTypeTools != null ) assetTypeTools.remove( type );

		log.log( DEBUG, "Tool unregistered: assetType={} -> tool={}", assetType.getKey(), type.getName() );
	}

	/**
	 * Open a tool using the specified request. The request contains all the
	 * information regarding the request including the asset.
	 *
	 * @param openToolRequest The open tool request
	 * @return The tool for the request or null if a tool was not created
	 * @apiNote Should be called from a {@link TaskManager} thread
	 */
	public ProgramTool openTool( OpenToolRequest openToolRequest ) throws NoToolRegisteredException {
		// Check the calling thread
		TaskManager.taskThreadCheck();

		// Verify the request parameters
		Asset asset = openToolRequest.getAsset();
		if( asset == null ) throw new NullPointerException( "Asset cannot be null" );

		// Get the asset type to look up the registered tool classes
		AssetType assetType = asset.getType();

		// Determine which tool class will be used
		Class<? extends ProgramTool> toolClass = openToolRequest.getToolClass();
		if( toolClass == null ) toolClass = determineToolClassForAssetType( assetType );
		if( toolClass == null ) throw new NoToolRegisteredException( "No tools registered for: " + assetType );
		openToolRequest.setToolClass( toolClass );

		// Check that the tool is registered
		ToolRegistration toolRegistration = toolClassMetadata.get( toolClass );
		if( toolRegistration == null ) throw new IllegalArgumentException( "Tool not registered: " + toolClass );

		// Determine how many instances the tool allows
		ToolInstanceMode instanceMode = getToolInstanceMode( toolClass );

		// Before checking for existing tools, the workpane needs to be determined
		Workpane pane = openToolRequest.getPane();
		WorkpaneView view = openToolRequest.getView();
		if( pane == null && view != null ) pane = view.getWorkpane();
		if( pane == null ) pane = program.getWorkspaceManager().getActiveWorkpane();
		if( pane == null ) throw new NullPointerException( "Workpane cannot be null when opening tool" );

		ProgramTool tool = findToolInPane( pane, toolClass );

		// If the instance mode is SINGLETON, check for an existing tool in the workpane
		if( instanceMode == ToolInstanceMode.SINGLETON && tool != null ) {
			final Workpane finalPane = pane;
			final ProgramTool finalTool = tool;
			if( openToolRequest.isSetActive() ) Platform.runLater( () -> finalPane.setActiveTool( finalTool ) );
			return tool;
		}

		try {
			tool = getToolInstance( openToolRequest ).get( 10, TimeUnit.SECONDS );
		} catch( Exception exception ) {
			log.log( ERROR, "Error creating tool: " + openToolRequest.getToolClass().getName(), exception );
			return null;
		}

		// Now that we have a tool...open dependent assets and associated tools
		for( URI dependency : tool.getAssetDependencies() ) {
			program.getAssetManager().openAsset( dependency, true, false );
		}

		// Determine the placement override
		// A null value allows the tool to determine its placement
		Workpane.Placement placementOverride = toolClassMetadata.get( tool.getClass() ).getPlacement();

		final Workpane finalPane = pane;
		final ProgramTool finalTool = tool;
		scheduleAssetReady( openToolRequest, finalTool );
		Platform.runLater( () -> finalPane.openTool( finalTool, placementOverride, openToolRequest.isSetActive() ) );

		return tool;
	}

	/**
	 * Called from the {@link UiRegenerator} to restore a tool.
	 *
	 * @param openToolRequest The open tool request for restoring the tool
	 * @param toolClassName The tool class name
	 * @return The restored tool
	 * @apiNote Could be called from a @code{task thread} or an @code{FX application thread}
	 */
	ProgramTool restoreTool( OpenToolRequest openToolRequest, String toolClassName ) {
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
			log.log( ERROR, "Tool class not registered: " + toolClassName );
			return null;
		}

		openToolRequest.setToolClass( toolRegistration.getType() );

		try {
			ProgramTool tool = getToolInstance( openToolRequest ).get( 10, TimeUnit.SECONDS );
			scheduleAssetReady( openToolRequest, tool );
			return tool;
		} catch( Exception exception ) {
			log.log( ERROR, "Error creating tool: " + openToolRequest.getToolClass().getName(), exception );
			return null;
		}
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
			log.log( WARNING, "No tools registered for asset type {}", assetType.getKey() );
		} else if( toolClasses.size() == 1 ) {
			// There is exactly one tool registered for the asset type
			log.log( DEBUG, "One tool registered for asset type {}", assetType.getKey() );
			toolClass = toolClasses.get( 0 );
		} else {
			// There is more than one tool registered for the asset type
			log.log( WARNING, "Multiple tools registered for asset type {}", assetType.getKey() );
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
	private Task<ProgramTool> getToolInstance( OpenToolRequest request ) {
		Asset asset = request.getAsset();
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		ProgramProduct product = toolClassMetadata.get( toolClass ).getProduct();

		// In order for this to be safe on any thread a task needs to be created
		// that is then run on the FX platform thread and the result obtained on
		// the calling thread.
		String taskName = program.rb().text( BundleKey.TOOL, "tool-manager-create-tool", toolClass.getSimpleName() );
		Task<ProgramTool> createToolTask = Task.of( taskName, () -> {
			// Create the new tool instance
			Constructor<? extends ProgramTool> constructor = toolClass.getConstructor( ProgramProduct.class, Asset.class );
			ProgramTool tool = constructor.newInstance( product, asset );

			// Set the id before using settings
			tool.setUid( request.getId() == null ? IdGenerator.getId() : request.getId() );
			tool.getSettings().set( Tool.SETTINGS_TYPE_KEY, tool.getClass().getName() );
			tool.getSettings().set( Asset.SETTINGS_URI_KEY, tool.getAsset().getUri() );
			if( tool.getAsset().getType() != null ) tool.getSettings().set( Asset.SETTINGS_TYPE_KEY, tool.getAsset().getType().getKey() );
			addToolListenerForSettings( tool );
			log.log( DEBUG, "Tool instance created: " + tool.getClass().getName() );
			return tool;
		} );

		// Run the task on the FX platform thread
		FxUtil.runLater( createToolTask );

		// Return the task
		return createToolTask;
	}

	private void addToolListenerForSettings( ProgramTool tool ) {
		tool.addEventHandler( ToolEvent.ADDED,
			e -> ((ProgramTool)e.getTool()).getSettings().set( UiFactory.PARENT_WORKPANEVIEW_ID, e.getTool().getToolView().getViewId() )
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
	private void scheduleAssetReady( OpenToolRequest request, ProgramTool tool ) {
		getProgram().getTaskManager().submit( Task.of( "wait for ready", () -> {
			Task<Void> toolLatch = getProgram().getTaskManager().submit( new ToolAddedLatch( tool ) );
			Task<Void> assetLatch = getProgram().getTaskManager().submit( new AssetLoadedLatch( tool.getAsset() ) );

			try {
				toolLatch.get();
				assetLatch.get();
				Platform.runLater( () -> {
					try {
						tool.ready( request.getOpenAssetRequest() );
						tool.open( request.getOpenAssetRequest() );
					} catch( ToolException exception ) {
						log.log( Log.ERROR, exception );
					}
				} );
			} catch( Exception exception ) {
				log.log( Log.ERROR, exception );
			}
		} ) );
	}

	private static class ToolAddedLatch extends Task<Void> {

		private final CountDownLatch latch = new CountDownLatch( 1 );

		private final ProgramTool tool;

		public ToolAddedLatch( ProgramTool tool ) {
			this.tool = tool;
		}

		@Override
		public Void call() throws Exception {
			javafx.event.EventHandler<ToolEvent> h = e -> latch.countDown();
			tool.addEventFilter( ToolEvent.ADDED, h );
			try {
				if( tool.getToolView() != null ) latch.await( 5, TimeUnit.SECONDS );
			} finally {
				log.log( Log.WARN, "Tool added=" + tool );
				tool.removeEventFilter( ToolEvent.ADDED, h );
			}
			return null;
		}

	}

	public static class AssetLoadedLatch extends Task<Void> {

		private final CountDownLatch latch = new CountDownLatch( 1 );

		private final Asset asset;

		public AssetLoadedLatch( Asset asset ) {
			this.asset = asset;
		}

		@Override
		public Void call() throws Exception {
			EventHandler<AssetEvent> h = e -> latch.countDown();
			asset.register( AssetEvent.LOADED, h );
			try {
				if( !asset.isLoaded() ) latch.await( 5, TimeUnit.SECONDS );
			} finally {
				log.log( Log.WARN, "Asset loaded=" + asset );
				asset.unregister( AssetEvent.LOADED, h );
			}
			return null;
		}

	}

}
