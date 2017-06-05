package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.ResourceManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Resource {

	public static final String CONTENT_TYPE_RESOURCE_KEY = "resource.content.type";

	//public static final String FIRST_LINE_RESOURCE_KEY = "resource.first.line";

	public static final String LAST_SAVED_RESOURCE_KEY = "resource.last.saved";

	public static final String EXTERNALLY_MODIFIED = "resource.externally.modified";

	public static final String EDITABLE = "resource.editable";

	public static final String UNDO_MANAGER = "resource.undo.manager";

	private static Logger log = LoggerFactory.getLogger( Resource.class );

	private String name;

	private URI uri;

	private Codec codec;

	private Scheme scheme;

	private ResourceType type;

	private UndoManager undoManager;

	private Set<ResourceListener> listeners;

	private volatile boolean open;

	private volatile boolean loaded;

	private volatile boolean saved;

	private volatile boolean ready;

	private Map<String, Object> resources;

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

		resources = new ConcurrentHashMap<>();
	}

	public URI getUri() {
		return uri;
	}

	public void setUri( URI uri ) {
		this.uri = uri;
		updateResourceName( uri );
	}

	public ResourceType getType() {
		return type;
	}

	public void setType( ResourceType type ) {
		this.type = type;
	}

	public Codec getCodec() {
		return codec;
	}

	public void setCodec( Codec codec ) {
		this.codec = codec;
	}

	public Scheme getScheme() {
		if( scheme != null ) return scheme;

		URI uri = getUri();
		if( uri == null ) return null;

		//this.scheme = Schemes.getScheme( uri.getScheme() );
		if( scheme == null ) throw new RuntimeException( "Scheme not registered: " + uri.getScheme() );

		return scheme;
	}

	public void setName( String name ) {
		this.name = name;
	}

	/**
	 * Get the name of the resource. This returns the resource type name if the
	 * URI is null, the entire URI if the path portion of the URI is null, or the
	 * file portion of the URI path.
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * A convenience method to get the file name from the URI.
	 *
	 * @return The file name from the URI.
	 */
	public String getFileName() {
		String name = null;

		try {
			name = getUri().toURL().getFile();
		} catch( MalformedURLException exception ) {
			log.error( "Error getting file name from: " + uri, exception );
		}

		return name;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	//	public boolean isExternallyModified() {
	//		Boolean value = getMetaValue( EXTERNALLY_MODIFIED );
	//		return value == null ? false : value;
	//	}
	//
	//	public void setExternallyModified( boolean modified ) {
	//		setMetaValue( EXTERNALLY_MODIFIED, modified );
	//	}

	public synchronized final boolean isOpen() {
		return open;
	}

	public synchronized final void open() throws ResourceException {
		if( isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.open( this );

		open = true;

		fireResourceOpened( new ResourceEvent( Resource.class, this ) );
	}

	public synchronized final boolean isLoaded() {
		return loaded;
	}

	public synchronized final void load( ResourceManager manager ) throws ResourceException {
		if( !isOpen() ) throw new ResourceException( this, "Resource must be opened to be loaded." );

		loaded = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.load( this, getCodec() );
		loaded = true;

		fireResourceLoaded( new ResourceEvent( Resource.class, this ) );
	}

	public synchronized final boolean isReady() {
		return ready;
	}

	public synchronized void setReady() {
		if( this.ready == true ) return;

		this.ready = true;
		notifyAll();

		fireResourceReady( new ResourceEvent( Resource.class, this ) );
	}

	public synchronized final boolean isSaved() {
		return saved;
	}

	public synchronized final void save( ResourceManager manager ) throws ResourceException {
		if( !isOpen() ) throw new ResourceException( this, "Resource must be opened to be saved." );
		if( getUri() == null ) throw new ResourceException( this, "URI must be set in order to save resource." );

		saved = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.save( this, getCodec() );
		saved = true;

		fireResourceSaved( new ResourceEvent( Resource.class, this ) );
	}

	public synchronized final boolean isClosed() {
		return !open;
	}

	public synchronized final void close( ResourceManager manager ) throws ResourceException {
		if( !isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.close( this );

		open = false;

		fireResourceClosed( new ResourceEvent( Resource.class, this ) );
	}

	public boolean exists() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme == null ? false : scheme.exists( this );
	}

	public boolean delete() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme == null ? false : scheme.delete( this );
	}

	/**
	 * Is the resource a container for other resources.
	 */
	public boolean isFolder() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme == null ? false : scheme.isFolder( this );
	}

	/**
	 * Is the resource hidden.
	 */
	public boolean isHidden() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme == null ? false : scheme.isHidden( this );
	}

	/**
	 * Get the child resources if this resource is a container for other
	 * resources.
	 */
	public List<Resource> listResources() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme == null ? null : scheme.listResources( this );
	}

	public TreePath toTreePath() {
		List<Resource> path = new ArrayList<Resource>();

		Resource resource = this;
		while( resource != null ) {
			path.add( resource );
			resource = (Resource)resource.getParent();
		}

		Collections.reverse( path );

		return new TreePath( path.toArray( new Resource[ path.size() ] ) );
	}

	public Resource getParent() {
		// TODO Implement Resource.getParent()
		return null;
	}

	public <T> void putResource( String key, T value ) {
		resources.put( key, value );
	}

	public <T> T getResource( String key ) {
		return (T)resources.get( key );
	}

	public void addResourceListener( ResourceListener listener ) {
		listeners.add( listener );
	}

	public void removeResourceListener( ResourceListener listener ) {
		listeners.remove( listener );
	}

	public void refresh() {
		fireResourceRefresh( new ResourceEvent( Resource.class, this ) );
	}

	@Override
	public int hashCode() {
		URI uri = getUri();
		if( uri == null ) return System.identityHashCode( this );
		return uri.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Resource) ) return false;
		Resource that = (Resource)object;

		URI thisUri = this.getUri();
		URI thatUri = that.getUri();

		if( thisUri == null && thatUri == null ) return this == that;
		if( thisUri == null || thatUri == null ) return false;

		return thisUri.equals( thatUri );
	}

	@Override
	public String toString() {
		URI uri = getUri();
		ResourceType type = getType();
		String resourceTypeName = type == null? "Unknown resource type" : type.getName();
		return uri == null ? resourceTypeName : uri.toString();
	}

	protected void fireResourceOpened( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceOpened( event );
		}
	}

	protected void fireResourceLoaded( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceLoaded( event );
		}
	}

	protected void fireResourceReady( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceReady( event );
		}
	}

	protected void fireResourceSaved( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceSaved( event );
		}
	}

	protected void fireResourceClosed( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceClosed( event );
		}
	}

	protected void fireResourceModified( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceModified( event );
		}
	}

	protected void fireResourceUnmodified( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceUnmodified( event );
		}
	}

	protected void fireResourceRefresh( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.resourceRefreshed( event );
		}
	}

	private void updateResourceName( URI uri ) {
		String name = null;
		String path = null;

		// If the URI is null return the type name.
		if( name == null && uri == null ) name = getType().getName();

		// If the path is null return the entire URI.
		if( name == null && uri != null ) {
			path = uri.getPath();
			if( StringUtils.isEmpty( path ) ) name = uri.toString();
		}

		// Get the folder name from the path.
		if( name == null && path != null ) {
			try {
				if( isFolder() ) {
					if( path.endsWith( "/" ) ) path = path.substring( 0, path.length() - 1 );
					name = path.substring( path.lastIndexOf( '/' ) + 1 );
				}
			} catch( ResourceException exception ) {
				// Intentionally ignore exception.
			}
		}

		// Return just the name from the path.
		if( name == null && path != null ) name = path.substring( path.lastIndexOf( '/' ) + 1 );

		setName( name );
	}

	//	private class DataHandler extends DataAdapter {
	//
	//		@Override
	//		public void metaAttributeChanged( MetaAttributeEvent event ) {
	//			if( DataNode.MODIFIED == event.getAttributeName() ) {
	//				if( Boolean.TRUE == event.getNewValue() ) {
	//					fireResourceModified( new ResourceEvent( this, (Resource)event.getSender() ) );
	//				} else {
	//					fireResourceUnmodified( new ResourceEvent( this, (Resource)event.getSender() ) );
	//				}
	//			}
	//		}
	//
	//	}

}
