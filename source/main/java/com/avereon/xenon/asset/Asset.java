package com.avereon.xenon.asset;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.TxnEvent;
import com.avereon.util.TextUtil;
import com.avereon.util.UriUtil;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.scheme.NewScheme;
import com.avereon.xenon.scheme.XenonScheme;
import com.avereon.xenon.undo.DataNodeUndo;
import com.avereon.xenon.undo.NodeChange;
import com.avereon.zerra.event.FxEventHub;
import lombok.CustomLog;
import lombok.Getter;
import org.fxmisc.undo.UndoManager;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@CustomLog
public class Asset extends Node {

	public static final Asset NONE = new Asset( java.net.URI.create( XenonScheme.ID + ":none" ) );

	public static final String SETTINGS_URI_KEY = "uri";

	public static final String SETTINGS_TYPE_KEY = "asset-type-key";

	public static final String MEDIA_TYPE_KEY = "asset-media-type";

	public static final String UNKNOWN_MEDIA_TYPE = "unknown";

	private static final String TYPE = "type";

	private static final String URI = "uri";

	public static final String NAME = "name";

	public static final String ICON = "icon";

	private static final String SCHEME = "scheme";

	private static final String CODEC = "codec";

	private static final String ENCODING = "encoding";

	private static final String MODEL = "model";

	private static final String EXTERNALLY_MODIFIED = "externally-modified";

	private static final String LAST_SAVED_KEY = "last-saved";

	private static final String LAST_WATCHED_KEY = "last-watched";

	//	private static final String EDITABLE = "editable";

	//	private static final String UNDO_MANAGER = "undo-manager";

	private final FxEventHub eventHub;

	@Getter
	private final UndoManager<List<NodeChange>> undoManager;

	private boolean captureUndoChanges;

	private volatile boolean open;

	private volatile boolean loaded;

	private volatile boolean saved;

	private Asset parent;

	// Ready to use flag. This indicates the asset is now ready to be used,
	// particularly by tools. If the asset is new or has been loaded then the
	// asset is "ready".

	//private volatile boolean ready;

	public Asset( URI uri ) {
		this( null, uri );
	}

	// Testing only
	public Asset( String uri ) {
		this( null, java.net.URI.create( uri ) );
	}

	public Asset( ResourceType type ) {
		this( type, null );
	}

	public Asset( ResourceType type, URI uri ) {
		this.eventHub = new FxEventHub().parent( super.getEventHub() );
		this.undoManager = DataNodeUndo.manager( this );

		setUri( uri == null ? NewScheme.uri() : uri );
		setType( type );

		if( isNew() && type == null ) throw new IllegalArgumentException( "New assets require an asset type" );
	}

	/**
	 * @return The URI for the asset
	 */
	public URI getUri() {
		return getValue( URI );
	}

	public void setUri( URI uri ) {
		if( uri == null ) throw new NullPointerException( "The uri cannot be null." );
		if( !uri.toString().equals( uri.normalize().toString() ) ) throw new IllegalArgumentException( "URI must be normalized" );
		setValue( URI, uri );
	}

	/**
	 * Get the name of the asset. This returns the asset type name if the
	 * URI is null, the entire URI if the path portion of the URI is null, or the
	 * file portion of the URI path.
	 *
	 * @return The name of the asset.
	 */
	public String getName() {
		return getValue( NAME, getDefaultName() );
	}

	public Asset setName( String name ) {
		setValue( NAME, name );
		return this;
	}

	public String getIcon() {
		return getValue( ICON, "file" );
	}

	public Asset setIcon( String icon ) {
		setValue( ICON, icon );
		return this;
	}

	public ResourceType getType() {
		return getValue( TYPE );
	}

	public void setType( ResourceType type ) {
		setValue( TYPE, type );
	}

	/**
	 * The codec used to load/save the asset. The codec is usually null until
	 * the asset is loaded or saved. Then the codec used for that operation is
	 * stored for convenience to be used for later load or save operations.
	 *
	 * @return The codec used to load/save the asset.
	 */
	public Codec getCodec() {
		return getValue( CODEC );
	}

	public void setCodec( Codec codec ) {
		setValue( CODEC, codec );
	}

	public Scheme getScheme() {
		Scheme scheme = getValue( SCHEME );
		//if( scheme == null ) log.atWarn().log( "Asset missing scheme: " + this );
		if( scheme == null ) throw new IllegalStateException( "Unresolved scheme: " + this );
		return scheme;
	}

	public void setScheme( Scheme scheme ) {
		setValue( SCHEME, scheme );
	}

	public String getEncoding() {
		return getValue( ENCODING, StandardCharsets.UTF_8.name() );
	}

	public void setEncoding( String encoding ) {
		setValue( ENCODING, encoding );
	}

	public String getMediaType() {
		return getValue( MEDIA_TYPE_KEY, UNKNOWN_MEDIA_TYPE );
	}

	public void setMediaType( String mediaType ) {
		setValue( MEDIA_TYPE_KEY, mediaType );
	}

	/**
	 * A convenience method to get the "simple" name of the asset.
	 *
	 * @return The simple name of the asset.
	 */
	public String getSimpleName() {
		return getFileName();
	}

	/**
	 * A convenience method to get the file name from the URI.
	 *
	 * @return The file name from the URI.
	 */
	public String getFileName() {
		URI uri = getUri();

		if( uri.isOpaque() ) {
			return uri.getSchemeSpecificPart();
		} else {
			return UriUtil.parseName( uri );
		}
	}

	public boolean isCaptureUndoChanges() {
		return getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );
	}

	public void setCaptureUndoChanges( boolean enabled ) {
		setValue( NodeChange.CAPTURE_UNDO_CHANGES, enabled );
	}

	public boolean isExternallyModified() {
		return getValue( EXTERNALLY_MODIFIED );
	}

	public void setExternallyModified( boolean modified ) {
		setValue( EXTERNALLY_MODIFIED, modified );
	}

	public long getLastSaved() {
		return getValue( LAST_SAVED_KEY, 0L );
	}

	public void setLastSaved( long timestamp ) {
		setValue( LAST_SAVED_KEY, timestamp );
	}

	public long getLastWatched() {
		return getValue( LAST_WATCHED_KEY );
	}

	public void setLastWatched( long timestamp ) {
		setValue( LAST_WATCHED_KEY, timestamp );
	}

	//	public File getFile() {
	//		return getValue( FILE );
	//	}
	//
	//	public void setFile( File file ) {
	//		setValue( FILE, file );
	//	}

	public <M> M getModel() {
		return getValue( MODEL );
	}

	public <M> Asset setModel( M model ) {
		setValue( MODEL, model );
		return this;
	}

	/**
	 * @return If the asset is "new"
	 */
	public final synchronized boolean isNew() {
		return NewScheme.ID.equals( getUri().getScheme() );
	}

	public final synchronized boolean isNewOrModified() {
		return isNew() || isModified();
	}

	public final synchronized boolean isSafeToSave() {
		return isNew() || (isLoaded() && isModified());
	}

	public final synchronized boolean isOpen() {
		return open;
	}

	public final synchronized void open( AssetManager manager ) throws ResourceException {
		if( isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.open( this );
		open = true;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.OPENED, this ) );

		notifyAll();
	}

	public synchronized final boolean isLoaded() {
		return loaded;
	}

	public synchronized final void load( AssetManager manager ) throws ResourceException {
		if( !isOpen() ) throw new ResourceException( this, "Asset must be opened to be loaded" );

		Scheme scheme = getScheme();
		if( scheme != null ) {
			log.atDebug().log( "Loading with scheme=" + scheme.getName() );
			scheme.load( this, getCodec() );
		} else {
			log.atWarn().log( "Undefined scheme for asset " + this );
		}
		setModified( false );
		loaded = true;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.LOADED, this ) );

		notifyAll();
	}

	public synchronized final boolean isSaved() {
		return saved;
	}

	public synchronized final void save( AssetManager manager ) throws ResourceException {
		if( !isOpen() ) throw new ResourceException( this, "Asset must be opened to be saved" );

		saved = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.save( this, getCodec() );
		setModified( false );
		saved = true;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.SAVED, this ) );

		notifyAll();
	}

	public synchronized final boolean isClosed() {
		return !open;
	}

	public synchronized final void close( AssetManager manager ) throws ResourceException {
		if( !isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.close( this );
		open = false;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.CLOSED, this ) );

		notifyAll();
	}

	public boolean exists() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme != null && scheme.exists( this );
	}

	public boolean delete() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme != null && scheme.delete( this );
	}

	/**
	 * Is the asset a container for other assets.
	 */
	public boolean isFolder() throws ResourceException {
		Scheme scheme = getScheme();
		//if( scheme == null ) throw new IllegalStateException( "Unresolved scheme when checking isFolder" );
		return scheme != null && scheme.isFolder( this );
	}

	/**
	 * Is the asset hidden.
	 */
	public boolean isHidden() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme != null && scheme.isHidden( this );
	}

	/**
	 * Get the child assets if this asset is a container for other
	 * assets.
	 */
	public List<Asset> listAssets() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme == null ? null : scheme.listAssets( this );
	}

	Asset add( Asset child ) {
		setValue( child.getUri().toString(), child );
		return this;
	}

	public List<Asset> getChildren() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme.listAssets( this );
	}

	public long getSize() throws ResourceException {
		Scheme scheme = getScheme();
		return scheme.getSize( this );
	}

	@Override
	public void dispatch( TxnEvent event ) {
		//		if( event instanceof NodeEvent ) {
		//			NodeEvent e = (NodeEvent)event;
		//			//			if( e.getNode().getValue( "preview", false )) {
		//			//				System.out.println( "Preview event leak=" + event.getEventType() );
		//			//			}
		//			System.out.println( "Asset.dispatch() event=" + event );
		//			if( e.getNode().getClass().getSimpleName().equals( "DesignLayer" ) ) {
		//				System.out.println( "DesignLayer event=" + event.getEventType() );
		//			}
		//		}
		if( event.getEventType() == NodeEvent.UNMODIFIED ) {
			getEventHub().dispatch( new AssetEvent( this, AssetEvent.UNMODIFIED, Asset.this ) );
		} else if( event.getEventType() == NodeEvent.MODIFIED ) {
			getEventHub().dispatch( new AssetEvent( this, AssetEvent.MODIFIED, Asset.this ) );
		}
		super.dispatch( event );
	}

	public FxEventHub getEventHub() {
		return eventHub;
	}

	@Override
	public int hashCode() {
		URI uri = getUri();
		return uri == null ? super.hashCode() : getUri().hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Asset that) ) return false;
		return this == that || Objects.equals( this.getUri(), that.getUri() );
	}

	@Override
	public String toString() {
		URI uri = getUri();
		ResourceType type = getType();
		String assetTypeName = type == null ? "Unknown" : type.getName();
		return "[" + assetTypeName + "](" + System.identityHashCode( this ) + ")" + (isNew() ? "" : " uri=" + uri);
	}

	private String getDefaultName() {
		URI uri = getUri();
		String path = uri.getPath();
		ResourceType type = getType();
		String name = null;

		// If the asset is new return the type name
		if( isNew() && type != null ) name = type.getName();

		// If the uri path is empty return the entire URI
		if( name == null && TextUtil.isEmpty( path ) ) name = uri.toString();

		// Get the name from the path
		if( name == null && !TextUtil.isEmpty( path ) ) name = getFileName();
		//		if( name == null && !TextUtil.isEmpty( path ) ) {
		//			try {
		//				if( isFolder() && path.endsWith( "/" ) ) path = path.substring( 0, path.length() - 1 );
		//			} catch( AssetException exception ) {
		//				// Intentionally ignore exception
		//			}
		//			name = path.substring( path.lastIndexOf( '/' ) + 1 );
		//		}

		return name;
	}

}
