package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.util.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.util.LogUtil;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;
import com.avereon.xenon.resource.ResourceListener;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.tool.ToolInstanceMode;
import com.avereon.xenon.tool.ToolMetadata;
import com.avereon.xenon.workarea.Tool;
import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneView;
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

	private Map<ResourceType, List<Class<? extends ProgramTool>>> resourceTypeToolClasses;

	private Map<String, String> aliases;

	public ToolManager( Program program ) {
		this.program = program;
		toolClassMetadata = new ConcurrentHashMap<>();
		resourceTypeToolClasses = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();
	}

	public void registerTool( ResourceType resourceType, ToolMetadata metadata ) {
		Class<? extends ProgramTool> type = metadata.getType();
		toolClassMetadata.put( type, metadata );

		List<Class<? extends ProgramTool>> resourceTypeToolClasses = this.resourceTypeToolClasses.computeIfAbsent( resourceType,
			k -> new CopyOnWriteArrayList<>()
		);
		resourceTypeToolClasses.add( type );

		log.debug( "Tool registered: resourceType={} -> tool={}", resourceType.getKey(), type.getName() );
	}

	public void unregisterTool( ResourceType resourceType, Class<? extends ProgramTool> type ) {
		toolClassMetadata.remove( type );

		List<Class<? extends ProgramTool>> resourceTypeTools = resourceTypeToolClasses.get( resourceType );
		if( resourceTypeTools != null ) resourceTypeTools.remove( type );

		log.debug( "Tool unregistered: resourceType={} -> tool={}", resourceType.getKey(), type.getName() );
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
		Resource resource = request.getResource();
		if( resource == null ) throw new NullPointerException( "Resource cannot be null" );

		// Get the resource type to look up the registered tool classes
		ResourceType resourceType = resource.getType();

		// Determine which tool class will be used
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		if( toolClass == null ) toolClass = determineToolClassForResourceType( resourceType );
		if( toolClass == null ) throw new NullPointerException( "No tools registered for: " + resourceType );
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
			String title = program.getResourceBundle().getString( "program", "no-tool-for-resource-title" );
			String message = program.getResourceBundle().getString( "program", "no-tool-for-resource-message", resource.getUri().toString() );
			program.getNoticeManager().warning( title, message, resource.getName() );
			return null;
		}

		// Now that we have a tool...open dependent resources and associated tools
		for( URI dependency : tool.getResourceDependencies() ) {
			program.getResourceManager().open( dependency, true, false );
		}

		// Determine the placement override
		// A null value allows the tool to determine its placement
		Workpane.Placement placementOverride = toolClassMetadata.get( tool.getClass() ).getPlacement();

		final Workpane finalPane = pane;
		final ProgramTool finalTool = tool;
		Platform.runLater( () -> finalPane.openTool( finalTool, placementOverride, request.isSetActive() ) );

		scheduleResourceReady( request, finalTool );

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

		if( tool != null ) scheduleResourceReady( openToolRequest, tool );

		return tool;
	}

	private ToolInstanceMode getToolInstanceMode( Class<? extends ProgramTool> toolClass ) {
		ToolInstanceMode instanceMode = toolClassMetadata.get( toolClass ).getInstanceMode();
		if( instanceMode == null ) instanceMode = ToolInstanceMode.UNLIMITED;
		return instanceMode;
	}

	private Class<? extends ProgramTool> determineToolClassForResourceType( ResourceType resourceType ) {
		Class<? extends ProgramTool> toolClass = null;
		List<Class<? extends ProgramTool>> toolClasses = resourceTypeToolClasses.get( resourceType );
		if( toolClasses == null ) {
			// There are no registered tools for the resource type
			log.warn( "No tools registered for resource type {}", resourceType.getKey() );
		} else if( toolClasses.size() == 1 ) {
			// There is exactly one tool registered for the resource type
			log.debug( "One tool registered for resource type {}", resourceType.getKey() );
			toolClass = toolClasses.get( 0 );
		} else {
			// There is more than one tool registered for the resource type
			log.warn( "Multiple tools registered for resource type {}", resourceType.getKey() );
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
		// TODO Implement ToolManager.start()
		return this;
	}

//	@Override
//	public ToolManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
//		// TODO Implement ToolManager.awaitStart()
//		return this;
//	}
//
//	@Override
//	public ToolManager restart() {
//		// TODO Implement ToolManager.requestRestart()
//		return this;
//	}
//
//	@Override
//	public ToolManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
//		// TODO Implement ToolManager.awaitRestart()
//		return this;
//	}

	@Override
	public ToolManager stop() {
		// TODO Implement ToolManager.stop()
		return this;
	}

//	@Override
//	public ToolManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
//		// TODO Implement ToolManager.awaitStop()
//		return this;
//	}

	// Safe to call on any thread
	private ProgramTool getToolInstance( OpenToolRequest request ) {
		Resource resource = request.getResource();
		Class<? extends ProgramTool> toolClass = request.getToolClass();
		ProgramProduct product = toolClassMetadata.get( toolClass ).getProduct();

		String taskName = program.getResourceBundle().getString( BundleKey.TOOL, "tool-manager-create-tool", toolClass.getSimpleName() );
		Task<ProgramTool> createToolTask = Task.of( taskName, () -> {
			// Have to have a ProductTool to support modules
			try {
				// Create the new tool instance
				Constructor<? extends ProgramTool> constructor = toolClass.getConstructor( ProgramProduct.class, Resource.class );
				ProgramTool tool = constructor.newInstance( product, resource );

				// Set the id before using settings
				tool.setUid( request.getId() == null ? IdGenerator.getId() : request.getId() );
				tool.getSettings().set( "type", tool.getClass().getName() );
				tool.getSettings().set( "uri", tool.getResource().getUri() );
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
	 * This method creates a task that waits for the resource to be ready then calls the tool resourceReady() method.
	 *
	 * @param request The open tool request object
	 * @param tool The tool that should be notified when the resource is ready
	 */
	private void scheduleResourceReady( OpenToolRequest request, ProgramTool tool ) {
		Resource resource = request.getResource();
		resource.callWhenReady( new ResourceListener() {

			@Override
			public void eventOccurred( ResourceEvent event ) {
				resource.removeResourceListener( this );
				Platform.runLater( () -> tool.callResourceReady( new OpenToolRequestParameters( request ) ) );
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
