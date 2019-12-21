package com.avereon.xenon.asset;

import com.avereon.settings.Settings;
import com.avereon.undo.BasicUndoManager;
import com.avereon.undo.UndoManager;
import com.avereon.util.Configurable;
import com.avereon.util.LogUtil;
import com.avereon.util.TextUtil;
import com.avereon.xenon.node.Node;
import com.avereon.xenon.node.NodeEvent;
import com.avereon.xenon.node.NodeListener;
import com.avereon.xenon.asset.event.*;
import com.avereon.xenon.scheme.AssetScheme;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Asset extends Node implements Configurable {

	public static final int ASSET_READY_TIMEOUT = 10;

	public static final String SETTINGS_URI_KEY = "uri";

	public static final String SETTINGS_TYPE_KEY = "asset-type";

	public static final String MEDIA_TYPE_ASSET_KEY = "asset.media.type";

	public static final String FILE_NAME_ASSET_KEY = "asset.file.name";

	public static final String FIRST_LINE_ASSET_KEY = "asset.first.line";

	private static final String TYPE_VALUE_KEY = "value.type";

	private static final String URI_VALUE_KEY = "value.uri";

	private static final String SCHEME_VALUE_KEY = "value.scheme";

	private static final String CODEC_VALUE_KEY = "value.codec";

	private static final String ENCODING_VALUE_KEY = "value.encoding";

	private static final String MODEL_VALUE_KEY = "value.model";

	private static final String EXTERNALLY_MODIFIED = "flag.externally.modified";

	//	private static final String EDITABLE = "asset.editable";

	//	private static final String UNDO_MANAGER = "asset.undo.manager";

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	// Name is not stored in the node data, it is derived
	private String name;

	private UndoManager undoManager;

	private Set<AssetListener> listeners;

	private Settings settings;

	private volatile boolean open;

	private volatile boolean loaded;

	private volatile boolean saved;

	// Ready to use flag. This indicates the asset is now ready to be used,
	// particularly by tools. If the asset is new or has been loaded then the
	// asset is "ready".
	private volatile boolean ready;

	public Asset( URI uri ) {
		this( null, uri );
	}

	public Asset( String uri ) {
		this( null, URI.create( uri ) );
	}

	public Asset( AssetType type, URI uri ) {
		setUri( uri );
		setType( type );

		if( isNew() && type == null ) throw new IllegalArgumentException( "New assets require an asset type" );

		// Create the undo manager
		undoManager = new BasicUndoManager();

		listeners = new CopyOnWriteArraySet<>();
		addNodeListener( new NodeWatcher() );
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
	 * A asset is "new" if it is created with an asset type. The URI is
	 * assigned when the asset is saved.
	 * <p>
	 * A asset is "old" if it is created with a URI. The asset type is
	 * determined when the asset is opened.
	 * <p>
	 * A asset is "new" if it does not have a URI associated with it yet. This
	 * usually occurs when the asset is created with only an asset type and
	 * has not been saved yet. When it is saved, a URI will be associated to the
	 * asset and it will be considered "old" from that point forward.
	 *
	 * @return If the asset is "new"
	 */
	public synchronized final boolean isNew() {
		return AssetScheme.ID.equals( getUri().getScheme() );
	}

	public synchronized final boolean isOpen() {
		return open;
	}

	public synchronized final void open() throws AssetException {
		if( isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.open( this );

		open = true;
		fireAssetEvent( new AssetOpenedEvent( Asset.class, this ) );

		if( isNew() ) {
			ready = true;
			fireAssetEvent( new AssetReadyEvent( Asset.class, this ) );
		}

	}

	public synchronized final boolean isLoaded() {
		return loaded;
	}

	public synchronized final void load( AssetManager manager ) throws AssetException {
		if( !isOpen() ) throw new AssetException( this, "Asset must be opened to be loaded" );

		loaded = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.load( this, getCodec() );

		loaded = true;
		fireAssetEvent( new AssetLoadedEvent( Asset.class, this ) );

		if( !ready ) {
			ready = true;
			fireAssetEvent( new AssetReadyEvent( Asset.class, this ) );
		}

		notifyAll();
	}

	public synchronized final void refresh( AssetManager manager ) {
		if( !ready ) return;
		fireAssetEvent( new AssetRefreshedEvent( Asset.class, this ) );
	}

	public synchronized final boolean isSaved() {
		return saved;
	}

	public synchronized final void save( AssetManager manager ) throws AssetException {
		if( !isOpen() ) throw new AssetException( this, "Asset must be opened to be saved" );

		saved = false;
		Scheme scheme = getScheme();
		if( scheme != null ) scheme.save( this, getCodec() );
		saved = true;

		fireAssetEvent( new AssetSavedEvent( Asset.class, this ) );
	}

	public synchronized final boolean isClosed() {
		return !open;
	}

	public synchronized final void close( AssetManager manager ) throws AssetException {
		if( !isOpen() ) return;

		Scheme scheme = getScheme();
		if( scheme != null ) scheme.close( this );

		open = false;

		fireAssetEvent( new AssetClosedEvent( Asset.class, this ) );
	}

	public synchronized void callWhenReady( AssetListener listener ) {
		if( ready ) {
			listener.eventOccurred( new AssetReadyEvent( Asset.class, this ) );
		} else {
			addAssetListener( listener );
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

	public void addAssetListener( AssetListener listener ) {
		listeners.add( listener );
	}

	public void removeAssetListener( AssetListener listener ) {
		listeners.remove( listener );
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
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

	private void fireAssetEvent( AssetEvent event ) {
		for( AssetListener listener : listeners ) {
			listener.eventOccurred( event );
		}
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

	private class NodeWatcher implements NodeListener {

		@Override
		public void nodeEvent( NodeEvent event ) {
			if( event.getType() != NodeEvent.Type.FLAG_CHANGED ) return;

			if( Objects.equals( event.getKey(), Node.MODIFIED ) ) {
				if( Boolean.TRUE == event.getNewValue() ) {
					fireAssetEvent( new AssetModifiedEvent( this, (Asset)event.getSource() ) );
				} else {
					fireAssetEvent( new AssetUnmodifiedEvent( this, (Asset)event.getSource() ) );
				}
			}
		}

	}

}
