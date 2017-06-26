package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceType;
import com.parallelsymmetry.essence.worktool.Tool;
import com.parallelsymmetry.essence.worktool.ToolMetadataComparator;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class ToolManager {

	private static Logger log = LoggerFactory.getLogger( ToolManager.class );

	private Program program;

	private Map<Class<? extends Tool>, ToolMetadata> tools;

	private Map<ResourceType, List<Class<? extends Tool>>> editTools;

	private SortedSet<ToolMetadata> workToolMetadata;

	private Map<String, String> aliases;

	public ToolManager( Program program ) {
		this.program = program;
		tools = new ConcurrentHashMap<>();
		editTools = new ConcurrentHashMap<>();
		workToolMetadata = new ConcurrentSkipListSet<>( new ToolMetadataComparator() );
		aliases = new ConcurrentHashMap<>();
	}

	// TODO Rename to registerTool
	public void addEditTool( Product product, ResourceType resourceType, Class<? extends Tool> type, String name, Node icon ) {
		ToolMetadata metadata = new ToolMetadata( product, type, name, icon );
		tools.put( type, metadata );

		List<Class<? extends Tool>> resourceTypeTools = editTools.get( resourceType );
		if( resourceTypeTools == null ) {
			resourceTypeTools = new CopyOnWriteArrayList<Class<? extends Tool>>();
			editTools.put( resourceType, resourceTypeTools );
		}
		resourceTypeTools.add( type );

		log.debug( "Tool registered: resourceType={} -> tool={}", resourceType, type.getName() );
	}

	// TODO Rename to unregisterTool
	public void removeEditTool( ResourceType resourceType, Class<? extends Tool> type ) {
		tools.remove( type );

		List<Class<? extends Tool>> resourceTypeTools = editTools.get( resourceType );
		if( resourceTypeTools != null ) resourceTypeTools.remove( type );

		log.debug( "Tool unregistered: resourceType={} -> tool={}", resourceType, type.getName() );
	}

	// TODO Rename to getTool()
	public Tool getEditTool( Resource resource ) {
		return doGetEditTool( resource );
	}

	public void addToolAlias( String oldName, String newName ) {
		aliases.putIfAbsent( oldName, newName );
	}

	public Product getToolProduct( Tool tool ) {
		ToolMetadata data = tools.get( tool.getClass() );
		return data == null ? null : data.getProduct();
	}

	public String getToolClassName( String className ) {
		String alias = null;
		if( className != null ) alias = aliases.get( className );
		return alias == null ? className : alias;
	}

	public Tool getToolInstance( Class<? extends Tool> type ) {
		return getToolInstance( type, null );
	}

	public Tool getToolInstance( Class<? extends Tool> type, Resource resource ) {
		Tool tool = null;
		Product product = tools.get( type ).getProduct();

		try {
			if( Tool.class.isAssignableFrom( type ) ) {
				Constructor<? extends Tool> constructor = type.getConstructor( Product.class, Resource.class );
				tool = constructor.newInstance( product, resource );
				// FIXME Should Tool.setReady() be implemented differently?
				//tool.setReady();
			} else {
				Constructor<? extends Tool> constructor = type.getConstructor( Product.class );
				tool = constructor.newInstance( product );
			}
		} catch( Exception exception ) {
			log.error( "Error creating instance: " + type.getName(), exception );
		}

		return tool;
	}

	private Tool doGetEditTool( Resource resource ) {
		ResourceType resourceType = resource.getType();
		if( resourceType == null ) {
			log.warn( "Resource type is null: " + resource );
			return null;
		}

		List<Class<? extends Tool>> typeTools = editTools.get( resourceType );
		if( typeTools == null ) {
			log.warn( "No tools registered for resource type: " + resourceType );
			return null;
		}

		Class<? extends Tool> type = typeTools.get( 0 );

		// TODO If there is more than one tool for a type then ask the user.

		Tool tool = getToolInstance( type, resource );

		if( tool == null ) {
			log.warn( "Tool not found for resource: " + resource );
		} else {
			log.debug( "Tool created for resource: " + resource );
		}

		return tool;
	}

	public void shutdown() {
		// TODO Implement ToolManager.shutdown()
	}

}
