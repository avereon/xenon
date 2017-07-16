package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.LogUtil;
import com.parallelsymmetry.essence.ResourceManager;
import com.parallelsymmetry.essence.node.Node;
import com.parallelsymmetry.essence.node.NodeEvent;
import com.parallelsymmetry.essence.node.NodeListener;
import com.parallelsymmetry.essence.resource.event.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

// TODO Add Configurable interface to this class
public class Resource extends Node {

	public static final String MEDIA_TYPE_RESOURCE_KEY = "resource.media.type";

	public static final String FILE_NAME_RESOURCE_KEY = "resource.file.name";

	public static final String FIRST_LINE_RESOURCE_KEY = "resource.first.line";

	private static final String TYPE_VALUE_KEY = "value.type";

	private static final String URI_VALUE_KEY = "value.uri";

	private static final String SCHEME_VALUE_KEY = "value.scheme";

	private static final String CODEC_VALUE_KEY = "value.codec";

	private static final String ENCODING_VALUE_KEY = "value.encoding";

	private static final String MODEL_VALUE_KEY = "value.model";

	private static final String EXTERNALLY_MODIFIED = "flag.externally.modified";

	//	private static final String EDITABLE = "resource.editable";

	//	private static final String UNDO_MANAGER = "resource.undo.manager";

	private static Logger log = LogUtil.get( Resource.class );

	// Name is not stored in the node data, it is derived
	private String name;

	private UndoManager undoManager;

	private Set<ResourceListener> listeners;

	private volatile boolean open;

	private volatile boolean loaded;

	private volatile boolean saved;

	private volatile boolean ready;

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

		// FIXME Finish implementing Resource constructor
		setType( type );
		setUri( uri );

		// TODO What is the FX undo manager
		//undoManager = new ResourceUndoManager();

		listeners = new CopyOnWriteArraySet<>();
		addNodeListener( new NodeWatcher() );
	}

	public URI getUri() {
		return getValue( URI_VALUE_KEY );
	}

	public void setUri( URI uri ) {
		setValue( URI_VALUE_KEY, uri );
		updateResourceName( uri );
	}

	public ResourceType getType() {
		return getValue( TYPE_VALUE_KEY );
	}

	public void setType( ResourceType type ) {
		setValue( TYPE_VALUE_KEY, type );
	}

	/**
	 * The codec used to load/save the resource. The codec is usually null until
	 * the resource is loaded or saved. Then the codec used for that operation is
	 * stored for convenience to be used for later load or save operations.
	 *
	 * @return
	 */
	public Codec getCodec() {
		return getValue( CODEC_VALUE_KEY );
	}

	public void setCodec( Codec codec ) {
		setValue( CODEC_VALUE_KEY, codec );
	}

	public Scheme getScheme() {
		return getValue( SCHEME_VALUE_KEY );
	}

	public void setScheme( Scheme scheme ) {
		setValue( SCHEME_VALUE_KEY, scheme );
	}

	public String getEncoding() {
		return getValue( ENCODING_VALUE_KEY );
	}

	public void setEncoding( String encoding ) {
		setValue( ENCODING_VALUE_KEY, encoding );
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
		URI uri = getUri();
		if( uri == null ) return null;

		if( uri.isOpaque() ) {
			return uri.getSchemeSpecificPart();
		} else {
			return uri.getPath().substring( uri.getPath().lastIndexOf( '/' ) + 1 );
		}
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public boolean isExternallyModified() {
		return getFlag( EXTERNALLY_MODIFIED );
	}

	public void setExternallyModified( boolean modified ) {
		setFlag( EXTERNALLY_MODIFIED, modified );
	}

	public <M> M getModel() {
		return getValue( MODEL_VALUE_KEY );
	}

	public <M> void setModel( M model ) {
		setValue( MODEL_VALUE_KEY, model );
	}

	/**
	 * A resource is "new" if it does not have a URI associated with it yet. This
	 * usually occurs when the resource is created with only a resource type and
	 * has not been saved yet. When it is saved, a URI will be associated to the
	 * resource and it will be considered "old" from that point forward.
	 *
	 * @return If the resource is "new"
	 */
	public synchronized final boolean isNew() {
		return getUri() == null;
	}

	public synchronized final boolean isOpen() {
		return open;
	}

	public synchronized final void open() throws ResourceException {
		if( isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.open( this );

		open = true;

		fireResourceEvent( new ResourceOpenedEvent( Resource.class, this ) );
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

		fireResourceEvent( new ResourceLoadedEvent( Resource.class, this ) );
	}

	public synchronized final void refresh( ResourceManager manager ) {
		fireResourceEvent( new ResourceRefreshedEvent( Resource.class, this ) );
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

		fireResourceEvent( new ResourceSavedEvent( Resource.class, this ) );
	}

	public synchronized final boolean isClosed() {
		return !open;
	}

	public synchronized final void close( ResourceManager manager ) throws ResourceException {
		if( !isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.close( this );

		open = false;

		fireResourceEvent( new ResourceClosedEvent( Resource.class, this ) );
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

	public void addResourceListener( ResourceListener listener ) {
		listeners.add( listener );
	}

	public void removeResourceListener( ResourceListener listener ) {
		listeners.remove( listener );
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
		String resourceTypeName = type == null ? "Unknown resource type" : type.getName();
		return uri == null ? resourceTypeName : uri.toString();
	}

	protected void fireResourceEvent( ResourceEvent event ) {
		for( ResourceListener listener : listeners ) {
			listener.eventOccurred( event );
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

		this.name = name;
	}

	private class NodeWatcher implements NodeListener {

		@Override
		public void eventOccurred( NodeEvent event ) {
			if( event.getType() != NodeEvent.Type.FLAG_CHANGED ) return;

			if( Objects.equals( event.getKey(), Node.MODIFIED ) ) {
				if( Boolean.TRUE == event.getNewValue() ) {
					fireResourceEvent( new ResourceModifiedEvent( this, (Resource)event.getSource() ) );
				} else {
					fireResourceEvent( new ResourceUnmodifiedEvent( this, (Resource)event.getSource() ) );
				}
			}
		}

	}

}
