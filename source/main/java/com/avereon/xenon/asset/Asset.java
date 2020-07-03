package com.avereon.xenon.asset;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.settings.Settings;
import com.avereon.transaction.TxnEvent;
import com.avereon.undo.BasicUndoScope;
import com.avereon.undo.UndoScope;
import com.avereon.util.Configurable;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.util.UriUtil;
import com.avereon.venza.event.FxEventHub;
import com.avereon.xenon.scheme.NewScheme;

import java.io.File;
import java.lang.System.Logger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class Asset extends Node implements Configurable {

	public static final Asset NONE = new Asset( URI.create( "program:none" ) );

	public static final String SETTINGS_URI_KEY = "uri";

	public static final String SETTINGS_TYPE_KEY = "asset-type";

	public static final String MEDIA_TYPE_KEY = "asset.media.type";

	public static final String UNKNOWN_MEDIA_TYPE = "unknown";

	// FIXME Is this the same value as SETTINGS_TYPE_KEY above?
	private static final String TYPE_VALUE_KEY = "asset.type";

	private static final String URI_VALUE_KEY = "asset.uri";

	public static final String ICON_VALUE_KEY = "asset.icon";

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

	private UndoScope undoScope;

	private FxEventHub eventBus;

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
		this( uri, null );
	}

	// Testing only
	public Asset( String uri ) {
		this( URI.create( uri ), null );
	}

	public Asset( URI uri, AssetType type ) {
		setUri( uri );
		setType( type );

		if( isNew() && type == null ) throw new IllegalArgumentException( "New assets require an asset type" );

		eventBus = new FxEventHub().parent( super.getEventHub() );

		// Create the undo manager
		undoScope = new BasicUndoScope();
	}

	/**
	 * @return The URI for the asset
	 */
	public URI getUri() {
		return getValue( URI_VALUE_KEY );
	}

	public void setUri( URI uri ) {
		if( uri == null ) throw new NullPointerException( "The uri cannot be null." );
		if( !uri.toString().equals( uri.normalize().toString() ) ) throw new IllegalArgumentException( "URI must be normalized" );
		setValue( URI_VALUE_KEY, uri );
		updateAssetName();
	}

	public String getIcon() {
		return getValue( ICON_VALUE_KEY, "file" );
	}

	public Asset setIcon( String icon ) {
		setValue( ICON_VALUE_KEY, icon );
		return this;
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
		return getValue( ENCODING_VALUE_KEY, StandardCharsets.UTF_8.name() );
	}

	public void setEncoding( String encoding ) {
		setValue( ENCODING_VALUE_KEY, encoding );
	}

	public String getMediaType() {
		return getValue( MEDIA_TYPE_KEY, UNKNOWN_MEDIA_TYPE );
	}

	public void setMediaType( String mediaType ) {
		setValue( MEDIA_TYPE_KEY, mediaType );
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
			String path = uri.getPath();
			//boolean isFolder = path.endsWith( "/" );
			//if( isFolder ) path = path.substring( 0, path.length() - 1 );
			return UriUtil.parseName( uri );

			//return path.substring( uri.getPath().lastIndexOf( '/' ) + 1 );
		}
	}

	public UndoScope getUndoScope() {
		return undoScope;
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
		getEventBus().dispatch( new AssetEvent( this, AssetEvent.OPENED, this ) );
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
		//setReady();
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

	public boolean exists() throws AssetException {
		// NEXT Should the asset assume it exists if there is not a scheme to verify?
		// TODO What about remote resources when there is NOT a connection possible?
		// TODO What about remote resources when there IS a connection possible?

		Scheme scheme = getScheme();
		//if( scheme == null ) throw new IllegalStateException( "Unresolved scheme when checking if exists" );
		//log.log( Log.WARN, "NO SCHEME - Can't determine if the asset exists" );
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

	@Override
	public void setSettings( Settings settings ) {
		this.settings = settings;
	}

	@Override
	public Settings getSettings() {
		return settings;
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
		}
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

	public String toUserString() {
		String string = toString();
		if( string.startsWith( "file:" ) ) string = string.substring( 5 );
		return string;
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
		if( name == null && !TextUtil.isEmpty( path ) ) name = getFileName();
		//		if( name == null && !TextUtil.isEmpty( path ) ) {
		//			try {
		//				if( isFolder() && path.endsWith( "/" ) ) path = path.substring( 0, path.length() - 1 );
		//			} catch( AssetException exception ) {
		//				// Intentionally ignore exception
		//			}
		//			name = path.substring( path.lastIndexOf( '/' ) + 1 );
		//		}

		this.name = name;
	}

}
