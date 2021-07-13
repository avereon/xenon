package com.avereon.xenon.asset;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.settings.Settings;
import com.avereon.transaction.TxnEvent;
import com.avereon.util.IdGenerator;
import com.avereon.util.TextUtil;
import com.avereon.util.UriUtil;
import com.avereon.xenon.scheme.NewScheme;
import com.avereon.xenon.undo.DataNodeUndo;
import com.avereon.xenon.undo.NodeChange;
import com.avereon.zerra.event.FxEventHub;
import lombok.CustomLog;
import org.fxmisc.undo.UndoManager;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@CustomLog
public class Asset extends Node {

	public static final Asset NONE = new Asset( java.net.URI.create( "program:none" ) );

	public static final String SETTINGS_URI_KEY = "uri";

	public static final String SETTINGS_TYPE_KEY = "asset-type";

	public static final String MEDIA_TYPE_KEY = "asset.media.type";

	public static final String UNKNOWN_MEDIA_TYPE = "unknown";

	// FIXME Is this the same value as SETTINGS_TYPE_KEY above?
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

	private static final String FILE = "file";

	//	private static final String EDITABLE = "editable";

	//	private static final String UNDO_MANAGER = "undo-manager";

	private final FxEventHub eventHub;

	private final UndoManager<List<NodeChange>> undoManager;

	private boolean captureUndoChanges;

	private Settings settings;

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

	public Asset( AssetType type ) {
		this( type, null );
	}

	public Asset( AssetType type, URI uri ) {
		if( uri == null ) uri = java.net.URI.create( NewScheme.ID + ":" + IdGenerator.getId() );
		this.eventHub = new FxEventHub().parent( super.getEventHub() );
		this.undoManager = DataNodeUndo.manager( this );

		setUri( uri );
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

	public AssetType getType() {
		return getValue( TYPE );
	}

	public void setType( AssetType type ) {
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
		return getValue( SCHEME );
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
	 * A convenience method to get the file name from the URI.
	 *
	 * @return The file name from the URI.
	 */
	public String getFileName() {
		URI uri = getUri();

		if( uri.isOpaque() ) {
			return uri.getSchemeSpecificPart();
		} else {
			String path = uri.getPath();
			//boolean isFolder = path.endsWith( "/" );
			//if( isFolder ) path = path.substring( 0, path.length() - 1 );
			return UriUtil.parseName( uri );

			//return path.substring( uri.getPath().lastIndexOf( '/' ) + 1 );
		}
	}

	public UndoManager<List<NodeChange>> getUndoManager() {
		return undoManager;
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
		return getValue( LAST_SAVED_KEY );
	}

	public void setLastSaved( long timestamp ) {
		setValue( LAST_SAVED_KEY, timestamp );
	}

	public File getFile() {
		return getValue( FILE );
	}

	public void setFile( File file ) {
		setValue( FILE, file );
	}

	public <M> M getModel() {
		return getValue( MODEL );
	}

	public <M> M setModel( M model ) {
		setValue( MODEL, model );
		return model;
	}

	/**
	 * A asset is "new" if it does not have a URI associated with it yet. This
	 * usually occurs when the asset is created with only an asset type and
	 * has not been saved yet. When it is saved, a URI will be associated to the
	 * asset and it will be considered "old" from that point forward.
	 * <p>
	 * A asset is existing if it is created with a URI. The asset type is
	 * determined when the asset is opened.
	 *
	 * @return If the asset is "new"
	 */
	public synchronized final boolean isNew() {
		// FIXME The isNew() logic may need improving
		// This logic is problematic in the case of an asset that has been created
		// but not yet saved. It can be in a tool, the program restarted and the
		// tool restored. In this case it should be restored with any prior
		// temporary state that should have been saved. The asset is not new but
		// it does not yet have a "real" URI.
		return NewScheme.ID.equals( getUri().getScheme() );
	}

	public synchronized final boolean isNewOrModified() {
		return isNew() || isModified();
	}

	public synchronized final boolean isOpen() {
		return open;
	}

	public synchronized final void open( AssetManager manager ) throws AssetException {
		if( isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.open( this );

		open = true;
		getEventHub().dispatch( new AssetEvent( this, AssetEvent.OPENED, this ) );
	}

	public synchronized final boolean isLoaded() {
		return loaded;
	}

	public synchronized final void load( AssetManager manager ) throws AssetException {
		if( !isOpen() ) throw new AssetException( this, "Asset must be opened to be loaded" );

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.load( this, getCodec() );
		setModified( false );
		loaded = true;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.LOADED, this ) );
		manager.getEventBus().dispatch( new AssetEvent( this, AssetEvent.LOADED, this ) );

		notifyAll();
	}

	public synchronized final boolean isSaved() {
		return saved;
	}

	public synchronized final void save( AssetManager manager ) throws AssetException {
		if( !isOpen() ) throw new AssetException( this, "Asset must be opened to be saved" );

		saved = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.save( this, getCodec() );
		setModified( false );
		saved = true;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.SAVED, this ) );
	}

	public synchronized final boolean isClosed() {
		return !open;
	}

	public synchronized final void close( AssetManager manager ) throws AssetException {
		if( !isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.close( this );
		open = false;

		getEventHub().dispatch( new AssetEvent( this, AssetEvent.CLOSED, this ) );
	}

	public boolean exists() throws AssetException {
		// NEXT Should the asset assume it exists if there is not a scheme to verify?
		// TODO What about remote resources when there is NOT a connection possible?
		// TODO What about remote resources when there IS a connection possible?

		Scheme scheme = getScheme();
		//if( scheme == null ) throw new IllegalStateException( "Unresolved scheme when checking if exists" );
		//log.atWarning().log( "NO SCHEME - Can't determine if the asset exists" );
		return scheme == null || scheme.exists( this );
	}

	public boolean delete() throws AssetException {
		Scheme scheme = getScheme();
		return scheme != null && scheme.delete( this );
	}

	/**
	 * Is the asset a container for other assets.
	 */
	public boolean isFolder() throws AssetException {
		Scheme scheme = getScheme();
		//if( scheme == null ) throw new IllegalStateException( "Unresolved scheme when checking if folder" );
		return scheme != null && scheme.isFolder( this );
	}

	/**
	 * Is the asset hidden.
	 */
	public boolean isHidden() throws AssetException {
		Scheme scheme = getScheme();
		return scheme != null && scheme.isHidden( this );
	}

	/**
	 * Get the child assets if this asset is a container for other
	 * assets.
	 */
	public List<Asset> listAssets() throws AssetException {
		Scheme scheme = getScheme();
		return scheme == null ? null : scheme.listAssets( this );
	}

	Asset add( Asset child ) {
		setValue( child.getUri().toString(), child );
		return this;
	}

	public List<Asset> getChildren() throws AssetException {
		Scheme scheme = getScheme();
		return scheme.listAssets( this );
	}

	public long getSize() throws AssetException {
		Scheme scheme = getScheme();
		return scheme.getSize( this );
	}

	/**
	 * These settings are set by the {@link AssetManager}.
	 *
	 * @return The asset settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * These settings are set by the {@link AssetManager}.
	 */
	public void setSettings( Settings settings ) {
		this.settings = settings;
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
		if( !(object instanceof Asset) ) return false;
		Asset that = (Asset)object;
		return this == that || Objects.equals( this.getUri(), that.getUri() );
	}

	@Override
	public String toString() {
		URI uri = getUri();
		AssetType type = getType();
		String assetTypeName = type == null ? "Unknown asset type" : type.getName();
		return isNew() ? assetTypeName : String.valueOf( uri );
	}

	private String getDefaultName() {
		URI uri = getUri();
		String path = uri.getPath();
		AssetType type = getType();
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
