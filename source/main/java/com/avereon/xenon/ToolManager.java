package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.settings.Settings;
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
import com.avereon.zarra.javafx.Fx;
import javafx.application.Platform;
import lombok.CustomLog;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@CustomLog
public class ToolManager implements Controllable<ToolManager> {

	private static final long WORK_TIME_LIMIT = 2;

	private static final TimeUnit WORK_TIME_UNIT = TimeUnit.SECONDS;

	@Getter
	private final Xenon program;

	private final Map<String, String> aliases;

	private final Map<Class<? extends ProgramTool>, ToolRegistration> toolClassMetadata;

	private final Map<AssetType, List<Class<? extends ProgramTool>>> assetTypeToolClasses;

	private final Set<Class<?>> singletonLocks = new CopyOnWriteArraySet<>();

	public ToolManager( Xenon program ) {
		this.program = program;
		toolClassMetadata = new ConcurrentHashMap<>();
		assetTypeToolClasses = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();
	}

	public ToolRegistration getToolRegistration( Class<? extends ProgramTool> toolClass ) {
		return toolClassMetadata.get( toolClass );
	}

	public ToolRegistration getToolRegistration( ProgramTool tool ) {
		return toolClassMetadata.get( tool.getClass() );
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
	 * @apiNote This method is synchronized in order to enforce singleton instance mode
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
		Class<? extends ProgramTool> requestedToolClass = request.getToolClass();
		if( requestedToolClass == null ) requestedToolClass = determineToolClassForAssetType( assetType );
		if( requestedToolClass == null ) throw new NoToolRegisteredException( "No tools registered for: " + assetType );
		final Class<? extends ProgramTool> toolClass = requestedToolClass;
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

		try {
			// Check for a singleton lock before looking for a tool instance
			if( instanceMode == ToolInstanceMode.SINGLETON ) checkSingletonLock( toolClass );

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

			// Determine the placement
			// A null value allows the tool to determine its own placement
			Workpane.Placement placement = toolClassMetadata.get( tool.getClass() ).getPlacement();

			final Workpane finalPane = pane;
			final ProgramTool finalTool = tool;
			final WorkpaneView finalView = view;
			scheduleWaitForReady( request, finalTool );
			Fx.run( () -> finalPane.openTool( finalTool, finalView, placement, request.isSetActive() ) );
			Fx.waitFor( WORK_TIME_LIMIT, WORK_TIME_UNIT );

			return tool;
		} catch( InterruptedException ignore ) {
			return null;
		} finally {
			if( instanceMode == ToolInstanceMode.SINGLETON ) clearSingletonLock( toolClass );
		}
	}

	/**
	 * This very particular method acquires a lock when creating singleton tools
	 * that blocks other threads from creating singleton tools if there is already
	 * a thread creating one.
	 *
	 * @param toolClass The singleton tool class
	 * @throws InterruptedException If a waiting thread is interrupted
	 */
	private void checkSingletonLock( Class<? extends ProgramTool> toolClass ) throws InterruptedException {
		synchronized( singletonLocks ) {
			// Need special handling of singletons
			while( singletonLocks.contains( toolClass ) ) {
				singletonLocks.wait( 1000 );
			}
			singletonLocks.add( toolClass );
		}
	}

	/**
	 * This very particular method clears the lock when creating singleton tools.
	 *
	 * @param toolClass The singleton tool class
	 */
	private void clearSingletonLock( Class<? extends ProgramTool> toolClass ) {
		synchronized( singletonLocks ) {
			singletonLocks.remove( toolClass );
			singletonLocks.notifyAll();
		}
	}

	private boolean openDependencies( OpenAssetRequest request, ProgramTool tool ) {
		// NOTE There is no realistic way of opening the dependencies on the same thread
		try {
			for( Future<ProgramTool> future : program.getAssetManager().openAssets( tool.getAssetDependencies(), true, false ) ) {
				future.get( WORK_TIME_LIMIT, WORK_TIME_UNIT );
			}
			return true;
		} catch( InterruptedException ignored ) {
		} catch( ExecutionException | TimeoutException exception ) {
			log.atSevere().withCause( exception ).log( "Error opening tool dependencies: %s", request.getToolClass().getName() );
		}
		return false;
	}

	/**
	 * Called from the {@link UiRegenerator} to restore a tool.
	 *
	 * @param request The open asset request for restoring the tool
	 * @return The restored tool
	 * @apiNote Could be called from a {@code task thread} or an {@code FX application thread}
	 */
	ProgramTool restoreTool( OpenAssetRequest request ) {
		// Run this class through the alias map
		String toolClassName = getToolClassName( request.getToolClassName() );

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
		return instanceMode == null ? ToolInstanceMode.UNLIMITED : instanceMode;
	}

	public List<Class<? extends ProgramTool>> getRegisteredTools( AssetType assetType ) {
		return new ArrayList<>( assetTypeToolClasses.get( assetType ) );
	}

	public Class<? extends ProgramTool> getDefaultTool( AssetType assetType ) {
		return determineToolClassForAssetType( assetType );
	}

	public void setDefaultTool( AssetType assetType, Class<? extends ProgramTool> tool ) {
		List<Class<? extends ProgramTool>> toolClasses = assetTypeToolClasses.get( assetType );
		if( toolClasses.remove( tool ) ) toolClasses.addFirst( tool );

		// Set the default tool setting
		Settings settings = getProgram().getSettingsManager().getAssetTypeSettings( assetType ).getNode( "default" );
		settings.set( "tool", tool.getName() );
	}

	public void updateDefaultToolsFromSettings() {
		// Go through each asset type and set the default tool from the settings
		for( AssetType assetType : assetTypeToolClasses.keySet() ) {
			Settings settings = getProgram().getSettingsManager().getAssetTypeSettings( assetType ).getNode( "default" );
			String defaultTool = settings.get( "tool" );
			if( defaultTool != null ) {
				Class<? extends ProgramTool> toolClass = findAssetTypeToolClassByName( assetType, defaultTool );
				if( toolClass != null ) {
					setDefaultTool( assetType, toolClass );
				} else {
					log.atWarn().log( "%s default tool class not found: %s", assetType.getName(), defaultTool );
				}
			}
		}
	}

	private Class<? extends ProgramTool> findAssetTypeToolClassByName( AssetType assetType, String name ) {
		List<Class<? extends ProgramTool>> toolClasses = assetTypeToolClasses.get( assetType );
		for( Class<? extends ProgramTool> toolClass : toolClasses ) {
			if( toolClass.getName().equals( name ) ) return toolClass;
		}
		return null;
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
			toolClass = toolClasses.getFirst();
		} else {
			// There is more than one tool registered for the asset type
			log.atWarning().log( "Multiple tools registered for asset type %s", assetType.getKey() );
			toolClasses.forEach( c -> log.atConfig().log( "  %s", c.getName() ) );
			toolClass = toolClasses.getFirst();
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
		XenonProgramProduct product = toolClassMetadata.get( toolClass ).getProduct();

		// In order for this to be safe on any thread a task needs to be created
		// that is then run on the FX platform thread and the result obtained on
		// the calling thread.
		String taskName = Rb.text( RbKey.TOOL, "tool-manager-create-tool", toolClass.getSimpleName() );
		Task<ProgramTool> createToolTask = Task.of( taskName, () -> {
			// Create the new tool instance
			Constructor<? extends ProgramTool> constructor = toolClass.getConstructor( XenonProgramProduct.class, Asset.class );
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
		tool.addEventHandler( ToolEvent.ADDED, e -> ((ProgramTool)e.getTool()).getSettings().set( UiFactory.PARENT_WORKPANEVIEW_ID, e.getTool().getToolView().getUid() ) );
		tool.addEventHandler( ToolEvent.ORDERED, this::setToolOrder );
		tool.addEventHandler( ToolEvent.ACTIVATED, e -> ((ProgramTool)e.getTool()).getSettings().set( "active", true ) );
		tool.addEventHandler( ToolEvent.DEACTIVATED, e -> ((ProgramTool)e.getTool()).getSettings().set( "active", null ) );
		tool.addEventHandler( ToolEvent.CLOSED, e -> ((ProgramTool)e.getTool()).getSettings().delete() );
	}

	private void setToolOrder( ToolEvent event ) {
		ProgramTool tool = (ProgramTool)event.getTool();
		int order = tool.getTabOrder();
		tool.getSettings().set( Tool.ORDER, order );
		tool.setOrder( order );
	}

	private ProgramTool findToolInPane( Workpane pane, Class<? extends Tool> type ) {
		return (ProgramTool)pane.getTools( type ).stream().findAny().orElse( null );
	}

	/**
	 * This method creates a task that waits for both the tool to be added
	 * and asset to be loaded then calls the tool ready() method.
	 *
	 * @param request The open tool request object
	 * @param tool The tool that should be notified when the asset is ready
	 */
	private void scheduleWaitForReady( OpenAssetRequest request, ProgramTool tool ) {
		ProgramTool.waitForReady( request, tool );
	}

}
