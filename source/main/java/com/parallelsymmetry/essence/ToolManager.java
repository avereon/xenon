package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceType;
import com.parallelsymmetry.essence.util.Controllable;
import com.parallelsymmetry.essence.worktool.Tool;
import com.parallelsymmetry.essence.worktool.ToolMetadataComparator;
import javafx.scene.Node;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ToolManager implements Controllable<ToolManager> {

	private static Logger log = LogUtil.get( ToolManager.class );

	private Program program;

	private Map<Class<? extends ProductTool>, ToolMetadata> tools;

	private Map<ResourceType, List<Class<? extends ProductTool>>> editTools;

	private SortedSet<ToolMetadata> workToolMetadata;

	private Map<String, String> aliases;

	public ToolManager( Program program ) {
		this.program = program;
		tools = new ConcurrentHashMap<>();
		editTools = new ConcurrentHashMap<>();
		workToolMetadata = new ConcurrentSkipListSet<>( new ToolMetadataComparator() );
		aliases = new ConcurrentHashMap<>();
	}

	public void registerTool( Product product, ResourceType resourceType, Class<? extends ProductTool> type, String name, Node icon ) {
		ToolMetadata metadata = new ToolMetadata( product, type, name, icon );
		tools.put( type, metadata );

		List<Class<? extends ProductTool>> resourceTypeTools = editTools.computeIfAbsent( resourceType, k -> new CopyOnWriteArrayList<Class<? extends ProductTool>>() );
		resourceTypeTools.add( type );

		log.debug( "Tool registered: resourceType={} -> tool={}", resourceType, type.getName() );
	}

	public void unregisterTool( ResourceType resourceType, Class<? extends ProductTool> type ) {
		tools.remove( type );

		List<Class<? extends ProductTool>> resourceTypeTools = editTools.get( resourceType );
		if( resourceTypeTools != null ) resourceTypeTools.remove( type );

		log.debug( "Tool unregistered: resourceType={} -> tool={}", resourceType, type.getName() );
	}

	public ProductTool getTool( Resource resource ) {
		return getToolInstance( resource );
	}

	public Product getToolProduct( ProductTool tool ) {
		ToolMetadata data = tools.get( tool.getClass() );
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

		List<Class<? extends ProductTool>> typeTools = editTools.get( resourceType );
		if( typeTools == null ) {
			log.warn( "No tools registered for resource type: " + resourceType );
			return null;
		}

		Class<? extends ProductTool> toolClass = typeTools.get( 0 );

		// TODO If there is more than one tool for a type then ask the user.

		ProductTool tool = getToolInstance( toolClass, resource );

		if( tool == null ) {
			log.warn( "Tool not found for resource: " + resource );
		} else {
			log.debug( "Tool created for resource: " + resource );
		}

		return tool;
	}

	private ProductTool getToolInstance( Class<? extends ProductTool> type, Resource resource ) {
		ProductTool tool = null;

		try {
			Constructor<? extends ProductTool> constructor = type.getConstructor( Product.class, Resource.class );
			Product product = tools.get( type ).getProduct();
			tool = constructor.newInstance( product, resource );
			// FIXME Should Tool.setReady() be implemented differently?
			// There really is no point to calling tool.setRead() here because
			// the constructor just completed. Calling tool.setReady() on the
			// same thread as the constructor just doesn't help any.
			//tool.setReady();
		} catch( Exception exception ) {
			log.error( "Error creating instance: " + type.getName(), exception );
		}

		return tool;
	}
}
