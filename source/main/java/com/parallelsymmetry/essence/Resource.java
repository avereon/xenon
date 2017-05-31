package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.ResourceListener;
import com.parallelsymmetry.essence.resource.ResourceType;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Resource {

	public static final String CONTENT_TYPE_RESOURCE_KEY = "resource.content.type";

	//public static final String FIRST_LINE_RESOURCE_KEY = "resource.first.line";

	public static final String LAST_SAVED_RESOURCE_KEY = "resource.last.saved";

	public static final String EXTERNALLY_MODIFIED = "resource.externally.modified";

	public static final String EDITABLE = "resource.editable";

	public static final String UNDO_MANAGER = "resource.undo.manager";

	//private static final String URI = "resource.uri";

	private static final String CODEC = "resource.codec";

	private static final String SCHEME = "resource.scheme";

	private static final String TYPE = "resource.type";

	//private ResourceUndoManager undoManager;

	private Set<ResourceListener> listeners;

	private volatile boolean open;

	private volatile boolean loaded;

	private volatile boolean saved;

	private volatile boolean ready;

	private URI uri;

	public Resource( URI uri ) {
		this( null, uri );
	}

	public Resource( String uri ) {
		this( null, URI.create( uri ) );
	}

	public Resource( ResourceType type ) {
		this( type, (URI)null );
	}

	public Resource( ResourceType type, String uri ) {
		this( type, URI.create( uri ) );
	}

	public Resource( ResourceType type, URI uri ) {
		if( type == null && uri == null ) throw new RuntimeException( "The type and uri cannot both be null." );

		listeners = new CopyOnWriteArraySet<ResourceListener>();
		// TODO What is the FX undo manager
		//undoManager = new ResourceUndoManager();

		// FIXME Finish implementing Resource constructor
		//addDataListener( new DataHandler() );
		//setType( type );
		//setUri( uri );
	}

	public URI getURI() {
		return uri;
	}

	public ResourceType getType() {
		return null;
	}

}
