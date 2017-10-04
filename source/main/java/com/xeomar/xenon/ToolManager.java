package com.xeomar.xenon;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.task.TaskThread;
import com.xeomar.xenon.util.Controllable;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneView;
import com.xeomar.xenon.workspace.ToolInstanceMode;
import com.xeomar.xenon.worktool.Tool;
import javafx.application.Platform;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ToolManager implements Controllable<ToolManager> {

	private static Logger log = LogUtil.get( ToolManager.class );

	private Program program;

	private Map<Class<? extends ProductTool>, ToolMetadata> toolClassMetadata;

	private Map<ResourceType, List<Class<? extends ProductTool>>> resourceTypeToolClasses;

	private Map<String, String> aliases;

	public ToolManager( Program program ) {
		this.program = program;
		toolClassMetadata = new ConcurrentHashMap<>();
		resourceTypeToolClasses = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();
	}

	public void registerTool( ResourceType resourceType, ToolMetadata metadata ) {
		Class<? extends ProductTool> type = metadata.getType();
		toolClassMetadata.put( type, metadata );

		List<Class<? extends ProductTool>> resourceTypeToolClasses = this.resourceTypeToolClasses.computeIfAbsent( resourceType, k -> new CopyOnWriteArrayList<Class<? extends ProductTool>>() );
		resourceTypeToolClasses.add( type );

		log.debug( "Tool registered: resourceType={} -> tool={}", resourceType.getKey(), type.getName() );
	}

	public void unregisterTool( ResourceType resourceType, Class<? extends ProductTool> type ) {
		toolClassMetadata.remove( type );

		List<Class<? extends ProductTool>> resourceTypeTools = resourceTypeToolClasses.get( resourceType );
		if( resourceTypeTools != null ) resourceTypeTools.remove( type );

		log.debug( "Tool unregistered: resourceType={} -> tool={}", resourceType.getKey(), type.getName() );
	}

	public ProductTool getTool( Resource resource ) {
		return getToolInstance( resource );
	}

	public void openTool( Resource resource ) {
		openTool( resource, null, null );
	}

	public void openTool( Resource resource, Workpane pane ) {
		openTool( resource, pane, null );
	}

	public void openTool( Resource resource, WorkpaneView view ) {
		openTool( resource, view == null ? null : view.getWorkpane(), view );
	}

	public void openTool( Resource resource, WorkpaneView view, boolean setActive ) {
		openTool( resource, view == null ? null : view.getWorkpane(), view, null, setActive );
	}

	public void openTool( Resource resource, Workpane pane, WorkpaneView view ) {
		openTool( resource, pane, view, null, true );
	}

	public void openTool( Resource resource, Workpane pane, WorkpaneView view, Class<? extends ProductTool> toolClass, boolean setActive ) {
		// The only thing that cannot be null is the resource
		if( resource == null ) throw new NullPointerException( "Resource cannot be null" );

		// Get the resource type to look up the registered tool classes
		ResourceType resourceType = resource.getType();

		// Determine which tool class will be used
		if( toolClass == null ) toolClass = determineToolClassForResourceType( resourceType );
		if( toolClass == null ) throw new NullPointerException( "No tools registered for: " + resourceType );

		// Check that the tool is registered
		ToolMetadata toolMetadata = toolClassMetadata.get( toolClass );
		if( toolMetadata == null ) throw new IllegalArgumentException( "Tool not registered: " + toolClass );

		// Determine how many instances the tool allows
		ToolInstanceMode instanceMode = getToolInstanceMode( toolClass );

		// Before checking for existing tools, the workpane needs to be determined
		if( pane == null && view != null ) pane = view.getWorkpane();
		if( pane == null ) pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		if( pane == null ) throw new NullPointerException( "Workpane cannot be null when opening tool" );

		// Create a tool if it is needed
		ProductTool tool = null;
		// If the instance mode is SINGLETON, check for an existing tool in the workpane
		if( instanceMode == ToolInstanceMode.SINGLETON ) tool = findToolOfClassInPane( pane, toolClass );
		boolean alreadyExists = tool != null;
		if( !alreadyExists ) tool = getToolInstance( toolClass, resource );

		// Verify there is a tool to use
		if( tool == null ) {
			String title = program.getResourceBundle().getString( "program", "no-tool-for-resource-title" );
			String message = program.getResourceBundle().getString( "program", "no-tool-for-resource-message" );
			program.getNotifier().warning( title, (Object)message, resource.getUri().toString() );
			return;
		}

		// Now that we have a tool...open dependent tools
		for( String dependency : tool.getResourceDependencies() ) {
			program.getResourceManager().open( program.getResourceManager().createResource( dependency ), true, false );
		}

		// Determine the placement override
		// A null value allows the tool to determine its placement
		Workpane.Placement placementOverride = toolClassMetadata.get( tool.getClass() ).getPlacement();

		final Workpane finalPane = pane;
		final Tool finalTool = tool;

		if( alreadyExists && instanceMode == ToolInstanceMode.SINGLETON ) {
			Platform.runLater( () -> finalPane.setActiveTool( finalTool ) );
		} else {
			Platform.runLater( () -> finalPane.addTool( finalTool, placementOverride, setActive ) );
		}
	}

	private ToolInstanceMode getToolInstanceMode( Class<? extends ProductTool> toolClass ) {
		ToolInstanceMode instanceMode = null;
		//if( instanceMode == null ) instanceMode = program.getSettings().getInstanceMode( toolClass );
		if( instanceMode == null ) instanceMode = toolClassMetadata.get( toolClass ).getInstanceMode();
		if( instanceMode == null ) instanceMode = ToolInstanceMode.UNLIMITED;
		return instanceMode;
	}

	private Class<? extends ProductTool> determineToolClassForResourceType( ResourceType resourceType ) {
		Class<? extends ProductTool> toolClass = null;
		List<Class<? extends ProductTool>> toolClasses = resourceTypeToolClasses.get( resourceType );
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

	public Product getToolProduct( ProductTool tool ) {
		ToolMetadata data = toolClassMetadata.get( tool.getClass() );
		return data == null ? null : data.getProduct();
	}

	public String getToolClassName( String className ) {
		String alias = null;
		if( className != null ) alias = aliases.get( className );
		return alias == null ? className : alias;
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

	@Override
	public ToolManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		// TODO Implement ToolManager.awaitStart()
		return this;
	}

	@Override
	public ToolManager restart() {
		// TODO Implement ToolManager.restart()
		return this;
	}

	@Override
	public ToolManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		// TODO Implement ToolManager.awaitRestart()
		return this;
	}

	@Override
	public ToolManager stop() {
		// TODO Implement ToolManager.stop()
		return this;
	}

	@Override
	public ToolManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		// TODO Implement ToolManager.awaitStop()
		return this;
	}

	private ProductTool getToolInstance( Resource resource ) {
		ResourceType resourceType = resource.getType();
		if( resourceType == null ) {
			log.warn( "Resource type is null: " + resource );
			return null;
		}

		// TODO Replace this logic with getting the default tool for the type
		List<Class<? extends ProductTool>> typeTools = resourceTypeToolClasses.get( resourceType );
		if( typeTools == null ) {
			log.warn( "No toolClassMetadata registered for resource type: " + resourceType );
			return null;
		}

		// Get the tool for the type
		Class<? extends ProductTool> toolClass = typeTools.get( 0 );
		ProductTool tool = getToolInstance( toolClass, resource );

		if( tool == null ) {
			log.warn( "Tool not found for resource: {}", resource );
		} else {
			log.debug( "Tool created for resource: {} -> {}", resource, toolClass );
		}

		return tool;
	}

	private boolean isTaskThread() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		return TaskThread.class.getName().equals( stack[ stack.length - 1 ].getClassName() );
	}

	private ProductTool getToolInstance( Class<? extends ProductTool> type, Resource resource ) {
		if( !isTaskThread() ) throw new RuntimeException( "ToolManager.getToolInstance() not called on Task thread" );

		// Have to have a ProductTool to support modules
		try {
			// Create the new tool instance
			Product product = toolClassMetadata.get( type ).getProduct();
			Constructor<? extends ProductTool> constructor = type.getConstructor( Product.class, Resource.class );
			ProductTool tool = constructor.newInstance( product, resource );

			// Set the tool settings
			tool.setSettings( program.getSettingsManager().getToolSettings( tool ) );

			// Wait for the resource to be "ready", then notify the tool
			// The getToolInstance() method should have been called from a Callable
			// class on a task manager thread, usually from
			// ResourceManager.OpenActionTask. That means the calling thread can
			// wait a bit for the resource to be ready.
			if( !resource.isNew() ) resource.waitForReady( 10, TimeUnit.SECONDS );
			tool.callResourceReady();
			return tool;
		} catch( Exception exception ) {
			log.error( "Error creating instance: " + type.getName(), exception );
		}

		return null;
	}

	private ProductTool findToolOfClassInPane( Workpane pane, Class<? extends Tool> type ) {
		for( Tool paneTool : pane.getTools() ) {
			if( type == paneTool.getClass() ) return (ProductTool)paneTool;
		}
		return null;
	}

}
