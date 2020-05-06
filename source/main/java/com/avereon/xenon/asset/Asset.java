package com.avereon.xenon.asset;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.event.EventHandler;
import com.avereon.settings.Settings;
import com.avereon.transaction.TxnEvent;
import com.avereon.undo.BasicUndoManager;
import com.avereon.undo.UndoManager;
import com.avereon.util.Configurable;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.xenon.scheme.AssetScheme;
import com.avereon.venza.event.FxEventHub;

import java.io.File;
import java.lang.System.Logger;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class Asset extends Node implements Configurable {

	public static final int ASSET_READY_TIMEOUT = 10;

	public static final String SETTINGS_URI_KEY = "uri";

	public static final String SETTINGS_TYPE_KEY = "asset-type";

	public static final String MEDIA_TYPE_ASSET_KEY = "asset.media.type";

	public static final String FILE_NAME_ASSET_KEY = "asset.file.name";

	public static final String FIRST_LINE_ASSET_KEY = "asset.first.line";

	// FIXME Is this the same value as SETTINGS_TYPE_KEY above?
	private static final String TYPE_VALUE_KEY = "asset.type";

	private static final String URI_VALUE_KEY = "asset.uri";

	private static final String SCHEME_VALUE_KEY = "asset.scheme";

	private static final String CODEC_VALUE_KEY = "asset.codec";

	private static final String ENCODING_VALUE_KEY = "asset.encoding";

	private static final String MODEL_VALUE_KEY = "asset.model";

	private static final String EXTERNALLY_MODIFIED = "asset.externally.modified";

	private static final String LAST_SAVED_KEY = "asset.last.saved";

	private static final String FILE = "asset.file";

	//	private static final String EDITABLE = "asset.editable";

	//	private static final String UNDO_MANAGER = "asset.undo.manager";

	private static final Logger log = Log.get();

	// Name is not stored in the node data, it is derived
	private String name;

	private UndoManager undoManager;

	private FxEventHub eventBus;

	private Settings settings;

	private volatile boolean open;

	private volatile boolean loaded;

	private volatile boolean saved;

	// Ready to use flag. This indicates the asset is now ready to be used,
	// particularly by tools. If the asset is new or has been loaded then the
	// asset is "ready".
	private volatile boolean ready;

	public Asset( URI uri ) {
		this( uri, null );
	}

	public Asset( String uri ) {
		this( URI.create( uri ), null );
	}

	public Asset( URI uri, AssetType type ) {
		if( uri == null ) throw new IllegalArgumentException( "Asset URI cannot be null" );
		setUri( uri );
		setType( type );

		if( isNew() && type == null ) throw new IllegalArgumentException( "New assets require an asset type" );

		eventBus = new FxEventHub().parent( super.getEventHub() );

		// Create the undo manager
		undoManager = new BasicUndoManager();
	}

	/**
	 * @return The URI for the asset
	 */
	public URI getUri() {
		return getValue( URI_VALUE_KEY );
	}

	public void setUri( URI uri ) {
		if( uri == null ) throw new NullPointerException( "The uri cannot be null." );
		setValue( URI_VALUE_KEY, uri );
		updateAssetName();
	}

	public AssetType getType() {
		return getValue( TYPE_VALUE_KEY );
	}

	public void setType( AssetType type ) {
		setValue( TYPE_VALUE_KEY, type );
		updateAssetName();
	}

	/**
	 * The codec used to load/save the asset. The codec is usually null until
	 * the asset is loaded or saved. Then the codec used for that operation is
	 * stored for convenience to be used for later load or save operations.
	 *
	 * @return The codec used to load/save the asset.
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

	public String getMediaType() {
		return getValue( MEDIA_TYPE_ASSET_KEY );
	}

	public void setMediaType( String mediaType ) {
		setValue( MEDIA_TYPE_ASSET_KEY, mediaType );
	}
	/**
	 * Get the name of the asset. This returns the asset type name if the
	 * URI is null, the entire URI if the path portion of the URI is null, or the
	 * file portion of the URI path.
	 *
	 * @return The name of the asset.
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
		return getValue( MODEL_VALUE_KEY );
	}

	public <M> void setModel( M model ) {
		setValue( MODEL_VALUE_KEY, model );
	}

	/**
	 * A asset is "new" if it does not have a URI associated with it yet. This
	 * usually occurs when the asset is created with only an asset type and
	 * has not been saved yet. When it is saved, a URI will be associated to the
	 * asset and it will be considered "old" from that point forward.
	 * <p>
	 * A asset is "old" if it is created with a URI. The asset type is
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
		return AssetScheme.ID.equals( getUri().getScheme() );
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
		getEventBus().dispatch( new AssetEvent( this, AssetEvent.OPENED, this ) );

		if( isNew() ) setReady();
	}

	public synchronized final boolean isLoaded() {
		return loaded;
	}

	public synchronized final void load( AssetManager manager ) throws AssetException {
		if( !isOpen() ) throw new AssetException( this, "Asset must be opened to be loaded" );

		loaded = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.load( this, getCodec() );
		setModified( false );
		loaded = true;

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.LOADED, this ) );

		// FIXME Because ready is triggered by event and refresh is called directly
		// asset.refresh() is often called before tool.assetReady() causing a race
		// condition because ready is expected to be called only once before refresh
		// is ever called. Options:
		//   1) Get rid of the tool.assetReady() method any only use refresh
		//   2) Make tool.assetReady() more tightly integrated with asset
		//   3) Make asset.refresh() less tightly integrated with tool
		setReady();
		refresh();
		notifyAll();
	}

	@Deprecated
	public synchronized final void refresh() {
		if( ready ) getEventBus().dispatch( new AssetEvent( this, AssetEvent.REFRESHED, this ) );
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

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.SAVED, this ) );

		updateAssetName();
	}

	public synchronized final boolean isClosed() {
		return !open;
	}

	public synchronized final void close( AssetManager manager ) throws AssetException {
		if( !isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.close( this );
		open = false;

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.CLOSED, this ) );
	}

	public synchronized void callWhenReady( EventHandler<AssetEvent> handler ) {
		if( ready ) {
			handler.handle( new AssetEvent( this, AssetEvent.READY, this ) );
		} else {
			eventBus.register( AssetEvent.READY, handler );
		}
	}

	public boolean exists() throws AssetException {
		Scheme scheme = getScheme();
		return scheme != null && scheme.exists( this );
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

	public Asset getParent() {
		// TODO Implement Asset.getParent()
		return null;
	}

	@Override
	public void setSettings( Settings settings ) {
		this.settings = settings;
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	public FxEventHub getEventBus() {
		return eventBus;
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
		return isNew() ? assetTypeName : uri.toString();
	}

	private synchronized void setReady() {
		if( ready ) return;
		ready = true;
		getEventBus().dispatch( new AssetEvent( this, AssetEvent.READY, this ) );
	}

	private void updateAssetName() {
		AssetType type = getType();
		URI uri = getUri();
		String path = uri.getPath();
		String name = null;

		// If the asset is new return the type name
		if( isNew() && type != null ) name = type.getName();

		// If the uri path is empty return the entire URI
		if( name == null && TextUtil.isEmpty( path ) ) name = uri.toString();

		// Get the name from the path
		if( name == null && !TextUtil.isEmpty( path ) ) {
			try {
				if( isFolder() && path.endsWith( "/" ) ) path = path.substring( 0, path.length() - 1 );
			} catch( AssetException exception ) {
				// Intentionally ignore exception
			}
			name = path.substring( path.lastIndexOf( '/' ) + 1 );
		}

		this.name = name;
	}

	@Override
	public void dispatch( TxnEvent event ) {
		//log.log( Log.WARN,  "Asset " + event.getEventType() + ": modified=" + isModified() );
		super.dispatch( event );

		if( getEventBus() == null ) return;

		if( event.getEventType() == NodeEvent.UNMODIFIED ) {
			getEventBus().dispatch( new AssetEvent( this, AssetEvent.UNMODIFIED, Asset.this ) );
		} else if( event.getEventType() == NodeEvent.MODIFIED ) {
			getEventBus().dispatch( new AssetEvent( this, AssetEvent.MODIFIED, Asset.this ) );
		} else if( event.getEventType() == NodeEvent.NODE_CHANGED ) {
			refresh();
		}
	}

}
