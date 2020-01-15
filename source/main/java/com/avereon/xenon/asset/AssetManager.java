package com.avereon.xenon.asset;

import com.avereon.event.EventHandler;
import com.avereon.settings.Settings;
import com.avereon.util.*;
import com.avereon.xenon.*;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.util.ProgramEventBus;
import com.avereon.xenon.workpane.WorkpaneView;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class AssetManager implements Controllable<AssetManager> {

	public static final String CURRENT_FOLDER_SETTING_KEY = "current-folder";

	// Linux defines this limit in BINPRM_BUF_SIZE
	private static final int FIRST_LINE_LIMIT = 128;

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final Asset NULL_ASSET = new Asset( URI.create( "program:null" ) );

	private Program program;

	private volatile Asset currentAsset;

	private final Set<Asset> openAssets;

	private final Map<URI, Asset> identifiedAssets;

	private final Map<String, Scheme> schemes;

	private final Map<String, AssetType> assetTypesByTypeKey;

	private final Map<URI, AssetType> uriAssetTypes;

	private final Map<String, AssetType> schemeAssetTypes;

	private final Map<String, Set<Codec>> registeredFileNames;

	private final Map<String, Set<Codec>> registeredFirstLines;

	private final Map<String, Set<Codec>> registeredMediaTypes;

	private ProgramEventBus eventBus;

	private CurrentAssetWatcher currentAssetWatcher;

	private NewActionHandler newActionHandler;

	private OpenActionHandler openActionHandler;

	private SaveActionHandler saveActionHandler;

	private SaveActionHandler saveAsActionHandler;

	private SaveActionHandler saveCopyAsActionHandler;

	private SaveAllActionHandler saveAllActionHandler;

	private CloseActionHandler closeActionHandler;

	private CloseAllActionHandler closeAllActionHandler;

	private final Object currentAssetLock = new Object();

	private boolean running;

	public AssetManager( Program program ) {
		this.program = program;
		openAssets = new CopyOnWriteArraySet<>();
		identifiedAssets = new ConcurrentHashMap<>();
		schemes = new ConcurrentHashMap<>();
		assetTypesByTypeKey = new ConcurrentHashMap<>();
		uriAssetTypes = new ConcurrentHashMap<>();
		schemeAssetTypes = new ConcurrentHashMap<>();
		registeredFileNames = new ConcurrentHashMap<>();
		registeredFirstLines = new ConcurrentHashMap<>();
		registeredMediaTypes = new ConcurrentHashMap<>();

		eventBus = new ProgramEventBus();
		eventBus.parent( program.getEventBus() );
		currentAssetWatcher = new CurrentAssetWatcher();

		newActionHandler = new NewActionHandler( program );
		openActionHandler = new OpenActionHandler( program );
		saveActionHandler = new SaveActionHandler( program, false, false );
		saveAsActionHandler = new SaveActionHandler( program, true, false );
		saveCopyAsActionHandler = new SaveActionHandler( program, true, true );
		saveAllActionHandler = new SaveAllActionHandler( program );
		closeActionHandler = new CloseActionHandler( program );
		closeAllActionHandler = new CloseAllActionHandler( program );
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public AssetManager start() {
		program.getActionLibrary().getAction( "new" ).pushAction( newActionHandler );
		program.getActionLibrary().getAction( "open" ).pushAction( openActionHandler );
		program.getActionLibrary().getAction( "save" ).pushAction( saveActionHandler );
		program.getActionLibrary().getAction( "save-as" ).pushAction( saveAsActionHandler );
		program.getActionLibrary().getAction( "copy-as" ).pushAction( saveCopyAsActionHandler );
		program.getActionLibrary().getAction( "save-all" ).pushAction( saveAllActionHandler );
		program.getActionLibrary().getAction( "close" ).pushAction( closeActionHandler );
		program.getActionLibrary().getAction( "close-all" ).pushAction( closeAllActionHandler );
		updateActionState();

		//program.getEventHub().register( ToolEvent.ANY, activeToolWatcher );

		// TODO ((FileScheme)Schemes.getScheme( "file" )).startAssetWatching();

		running = true;

		return this;
	}

	@Override
	public AssetManager stop() {
		running = false;

		// TODO ((FileScheme)Schemes.getScheme( "file" )).stopAssetWatching();

		//program.getEventHub().unregister( ToolEvent.ANY, activeToolWatcher );

		return this;
	}

	public ProgramEventBus getEventBus() {
		return eventBus;
	}

	public Asset getCurrentAsset() {
		return currentAsset;
	}

	public void setCurrentAsset( Asset asset ) {
		if( asset != null && asset.getUri().equals( ProgramGuideType.URI ) ) return;
		program.getTaskManager().submit( new SetCurrentAssetTask( asset ) );
	}

	public void setCurrentAssetAndWait( Asset asset ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new SetCurrentAssetTask( asset ) ).get();
	}

	public List<Asset> getOpenAssets() {
		return new ArrayList<>( openAssets );
	}

	public List<Asset> getModifiedAssets() {
		List<Asset> modifiedAssets = new ArrayList<>();
		for( Asset asset : getOpenAssets() ) {
			if( asset.isModified() && canSaveAsset( asset ) ) modifiedAssets.add( asset );
		}
		return modifiedAssets;
	}

	Set<AssetType> getUserAssetTypes() {
		return assetTypesByTypeKey.values().stream().filter( AssetType::isUserType ).collect( Collectors.toSet() );
	}

	/**
	 * Get the externally modified assets.
	 *
	 * @return The set of externally modified assets
	 */
	public Set<Asset> getExternallyModifiedAssets() {
		Set<Asset> externallyModifiedAssets = new HashSet<Asset>();
		for( Asset asset : getOpenAssets() ) {
			if( asset.isExternallyModified() ) externallyModifiedAssets.add( asset );
		}
		return Collections.unmodifiableSet( externallyModifiedAssets );
	}

	/**
	 * Get a scheme by the scheme name.
	 *
	 * @param name The scheme name
	 * @return The scheme registered to the name
	 */
	public Scheme getScheme( String name ) {
		return schemes.get( name );
	}

	/**
	 * Add a scheme.
	 *
	 * @param scheme The scheme to add
	 */
	public void addScheme( Scheme scheme ) {
		schemes.put( scheme.getName(), scheme );
	}

	/**
	 * Remove a scheme.
	 *
	 * @param name The name of the scheme to remove
	 */
	public void removeScheme( String name ) {
		schemes.remove( name );
	}

	/**
	 * Get the registered schemes.
	 *
	 * @return The set of registered schemes
	 */
	public Collection<Scheme> getSchemes() {
		return Collections.unmodifiableCollection( schemes.values() );
	}

	/**
	 * Get the registered scheme names.
	 *
	 * @return The set of registered scheme names
	 */
	public Set<String> getSchemeNames() {
		return Collections.unmodifiableSet( schemes.keySet() );
	}

	/**
	 * Get an asset type by the asset type key defined in the asset type. This is useful for getting asset types from persisted data.
	 *
	 * @param key The asset type key
	 * @return The asset type associated to the key
	 */
	public AssetType getAssetType( String key ) {
		AssetType type = assetTypesByTypeKey.get( key );
		if( type == null ) log.warn( "Asset type not found: " + key );
		return type;
	}

	/**
	 * Get the set of supported asset types.
	 *
	 * @return The set of supported asset types
	 */
	public Collection<AssetType> getAssetTypes() {
		return Collections.unmodifiableCollection( assetTypesByTypeKey.values() );
	}

	/**
	 * Add an asset type to the set of supported asset types.
	 *
	 * @param type The asset type to add
	 */
	public void addAssetType( AssetType type ) {
		if( type == null ) return;

		synchronized( assetTypesByTypeKey ) {
			if( assetTypesByTypeKey.get( type.getKey() ) != null ) throw new IllegalArgumentException( "AssetType already exists: " + type.getKey() );

			Set<Codec> codecs = type.getCodecs();
			for( Codec codec : codecs ) {
				// Register codec support.
				registerCodec( codec, codec.getSupportedFileNames(), registeredFileNames );
				registerCodec( codec, codec.getSupportedFirstLines(), registeredFirstLines );
				registerCodec( codec, codec.getSupportedMediaTypes(), registeredMediaTypes );
			}

			// Add the asset type to the registered asset types.
			assetTypesByTypeKey.put( type.getKey(), type );

			// Update the actions.
			updateActionState();
		}
	}

	/**
	 * Remove an asset type from the set of supported asset types.
	 *
	 * @param type The asset type to remove
	 */
	public void removeAssetType( AssetType type ) {
		if( type == null ) return;
		synchronized( assetTypesByTypeKey ) {
			if( !assetTypesByTypeKey.containsKey( type.getKey() ) ) return;

			// Remove the asset type from the registered asset types
			assetTypesByTypeKey.remove( type.getKey() );
			for( Map.Entry entry : uriAssetTypes.entrySet() ) {
				if( entry.getValue() == type ) uriAssetTypes.remove( entry.getKey() );
			}
			for( Map.Entry entry : schemeAssetTypes.entrySet() ) {
				if( entry.getValue() == type ) schemeAssetTypes.remove( entry.getKey() );
			}

			Set<Codec> codecs = type.getCodecs();
			for( Codec codec : codecs ) {
				// Unregister codec support.
				unregisterCodec( codec, codec.getSupportedFileNames(), registeredFileNames );
				unregisterCodec( codec, codec.getSupportedFirstLines(), registeredFirstLines );
				unregisterCodec( codec, codec.getSupportedMediaTypes(), registeredMediaTypes );
			}

			// Update the actions.
			updateActionState();
		}
	}

	public void registerUriAssetType( URI uri, AssetType type ) {
		if( assetTypesByTypeKey.get( type.getKey() ) == null ) addAssetType( type );
		uriAssetTypes.put( uri, type );
	}

	public void unregisterUriAssetType( URI uri ) {
		uriAssetTypes.remove( uri );
	}

	public void registerSchemeAssetType( String scheme, AssetType type ) {
		if( assetTypesByTypeKey.get( type.getKey() ) == null ) addAssetType( type );
		schemeAssetTypes.put( scheme, type );
	}

	public void unregisterSchemeAssetType( String scheme ) {
		schemeAssetTypes.remove( scheme );
	}

	/**
	 * This method starts the process of creating a new asset by asset type. The
	 * returned future allows the caller to get the tool created for the new
	 * asset. It is possible that a tool was not created for the asset, in which
	 * case the tool is null.
	 *
	 * @param type The new asset type
	 * @return The future to get the new asset tool
	 */
	public Future<ProgramTool> newAsset( AssetType type ) {
		return newAsset( type, true, true );
	}

	/**
	 * This method starts the process of creating a new asset by asset type. The
	 * returned future allows the caller to get the tool created for the new
	 * asset. It is possible that a tool was not created for the asset, in which
	 * case the tool is null.
	 *
	 * @param type The new asset type
	 * @return The future to get the new asset tool
	 */
	public Future<ProgramTool> newAsset( AssetType type, boolean openTool, boolean setActive ) {
		return newAsset( type, null, openTool, setActive );
	}

	/**
	 * This method starts the process of creating a new asset by asset type. The
	 * returned future allows the caller to get the tool created for the new
	 * asset. It is possible that a tool was not created for the asset, in which
	 * case the tool is null.
	 *
	 * @param type The new asset type
	 * @return The future to get the new asset tool
	 */
	private Future<ProgramTool> newAsset( AssetType type, WorkpaneView view, boolean openTool, boolean setActive ) {
		OpenAssetRequest request = new OpenAssetRequest();
		request.setUri( null );
		request.setType( type );
		request.setView( view );
		request.setOpenTool( openTool );
		request.setSetActive( setActive );
		return program.getTaskManager().submit( new NewOrOpenAssetTask( request ) );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public Future<ProgramTool> openAsset( URI uri ) {
		return openAsset( uri, true, true );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public Future<ProgramTool> openAsset( URI uri, boolean openTool, boolean setActive ) {
		return openAsset( Collections.singletonList( uri ), null, openTool, setActive ).get( 0 );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	private List<Future<ProgramTool>> openAsset( List<URI> uris, WorkpaneView view, boolean openTool, boolean setActive ) {
		List<Future<ProgramTool>> futures = new ArrayList<>( uris.size() );

		for( URI uri : uris ) {
			OpenAssetRequest request = new OpenAssetRequest();
			request.setUri( uri );
			request.setView( view );
			request.setOpenTool( openTool );
			request.setSetActive( setActive );
			futures.add( program.getTaskManager().submit( new NewOrOpenAssetTask( request ) ) );
			setActive = false;
		}

		return futures;
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void saveAsset( Asset asset ) {
		saveAsset( asset, null, false, false );
	}

	/**
	 * Request that the source asset be saved as the target asset. This method submits a task to the task manager and returns immediately.
	 *
	 * @param source The source asset
	 * @param target The target asset
	 * @implNote This method makes calls to the FX platform.
	 */
	public void saveAsAsset( Asset source, Asset target ) {
		saveAsset( source, target, true, false );
	}

	//	/**
	//	 * Request that the source asset be saved as the target asset and wait
	//	 * until the task is complete. This method submits a task to the task manager
	//	 * and waits for the task to be completed.
	//	 *
	//	 * @param source The source asset
	//	 * @param target The target asset
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 * @implNote This method makes calls to the FX platform.
	//	 */
	//	public void saveAsAssetAndWait( Asset source, Asset target ) throws ExecutionException, InterruptedException {
	//		save( source, target, true, false );
	//	}

	/**
	 * Request that the source asset be saved as a copy to the target asset. This method submits a task to the task manager and returns immediately.
	 *
	 * @param source The source asset
	 * @param target The target asset
	 * @implNote This method makes calls to the FX platform.
	 */
	public void copyAsAsset( Asset source, Asset target ) {
		saveAsset( source, target, false, true );
	}

	//	/**
	//	 * Request that the source asset be saved as a copy to the target asset
	//	 * and wait until the task is complete. This method submits a task to the task
	//	 * manager and waits for the task to be completed.
	//	 *
	//	 * @param source The source asset
	//	 * @param target The target asset
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 * @implNote This method makes calls to the FX platform.
	//	 */
	//	public void saveCopyAsAssetAndWait( Asset source, Asset target ) throws ExecutionException, InterruptedException {
	//		save( source, target, false, true );
	//	}

	/**
	 * Save the asset, prompting the user if necessary.
	 *
	 * @param asset The asset to be saved
	 * @param saveAsAsset The asset to save as
	 * @param saveAs The save as flag
	 * @param copy The copy as flag
	 * @implNote This method makes calls to the FX platform.
	 */
	private void saveAsset( Asset asset, Asset saveAsAsset, boolean saveAs, boolean copy ) {
		if( asset.isNew() || (saveAs && saveAsAsset == null) ) {
			Codec codec = asset.getCodec();
			if( codec == null ) codec = asset.getType().getDefaultCodec();

			// NOTE This logic is very file oriented. It may need to move to the file scheme.
			FileChooser chooser = new FileChooser();
			Map<Codec, FileChooser.ExtensionFilter> codecFilters = generateCodecFilters( asset.getType() );
			chooser.getExtensionFilters().addAll( codecFilters.values() );
			chooser.setSelectedExtensionFilter( codecFilters.get( codec ) );
			chooser.setInitialDirectory( getFileChooserFolder() );
			chooser.setInitialFileName( "asset" + (codec == null ? "" : "." + codec.getDefaultExtension()) );

			File file = chooser.showSaveDialog( program.getWorkspaceManager().getActiveStage() );
			if( file == null ) return;

			File parent = file.isDirectory() ? file : file.getParentFile();
			getSettings().set( CURRENT_FOLDER_SETTING_KEY, parent.toString() );

			// If the user specified a codec use it to set the codec and asset type
			AssetType type = asset.getType();
			Map<FileChooser.ExtensionFilter, Codec> filterCodecs = MapUtil.mirror( codecFilters );
			Codec selectedCodec = filterCodecs.get( chooser.getSelectedExtensionFilter() );
			if( selectedCodec != null ) type = selectedCodec.getAssetType();

			// If the file extension is not already supported use the default extension from the codec
			if( !file.exists() && selectedCodec != null && !selectedCodec.isSupportedExtension( file.getName() ) ) {
				file = new File( file.getParent(), file.getName() + "." + selectedCodec.getDefaultExtension() );
			}

			// Resolve the URI
			URI uri = UriUtil.resolve( file.toString() );

			// Create the target asset
			try {
				saveAsAsset = createAsset( type, uri );
				saveAsAsset.setSettings( getAssetSettings( saveAsAsset ) );
				saveAsAsset.getSettings().copyFrom( asset.getSettings() );
				if( selectedCodec != null ) saveAsAsset.setCodec( selectedCodec );
			} catch( AssetException exception ) {
				program.getNoticeManager().error( exception );
			}
		}

		if( saveAsAsset != null ) {
			if( copy ) asset = saveAsAsset.copyFrom( asset );
			asset.setUri( saveAsAsset.getUri() );
			asset.setCodec( saveAsAsset.getCodec() );
			asset.setScheme( getScheme( saveAsAsset.getUri().getScheme() ) );
		}

		saveAssets( asset );
	}

	private Map<Codec, FileChooser.ExtensionFilter> generateCodecFilters( AssetType type ) {
		Map<Codec, FileChooser.ExtensionFilter> codecFilters = new HashMap<>();
		for( Codec codec : type.getCodecs() ) {
			codecFilters.put( codec, generateFilter( codec ) );
		}
		return codecFilters;
	}

	private FileChooser.ExtensionFilter generateFilter( Codec codec ) {
		List<String> extensions = new ArrayList<>();
		StringBuilder desc = new StringBuilder();
		for( String ext : codec.getSupportedExtensions() ) {
			extensions.add( "*." + ext );
			desc.append( "," ).append( ext );
		}
		String name = codec.getName() + " (" + desc.toString().substring( 1 ) + ")";

		return new FileChooser.ExtensionFilter( name, extensions );
	}

	/**
	 * Close the asset, prompting the user if necessary.
	 *
	 * @param asset The asset to be closed
	 * @implNote This method makes calls to the FX platform.
	 */
	public void close( Asset asset ) {
		if( asset.isModified() && canSaveAsset( asset ) ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
			alert.setTitle( program.rb().text( "asset", "close-save-title" ) );
			alert.setHeaderText( program.rb().text( "workarea", "close-save-message" ) );
			alert.setContentText( program.rb().text( "asset", "close-save-prompt" ) );
			alert.getButtonTypes().addAll( ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );

			Stage stage = program.getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() != ButtonType.YES ) return;
		}

		saveAsset( asset, null, false, false );
	}

	public Asset createAsset( Object descriptor ) throws AssetException {
		if( descriptor instanceof URI ) {
			return (createAsset( (URI)descriptor ));
		} else if( descriptor instanceof File ) {
			return (createAsset( ((File)descriptor).toURI() ));
		} else {
			return (createAsset( descriptor.toString() ));
		}
	}

	/**
	 * Create an asset from a string. This asset is considered to be an old asset. See {@link Asset#isNew()}
	 *
	 * @param string A asset string
	 * @return A new asset based on the specified string.
	 */
	public Asset createAsset( String string ) throws AssetException {
		if( string == null ) return null;

		URI uri = UriUtil.resolve( string );

		if( uri == null ) {
			String title = program.rb().text( "asset", "assets" );
			String message = program.rb().text( "program", "asset-unable-to-resolve" );
			program.getNoticeManager().warning( title, message, string );
			return null;
		}

		return createAsset( uri );
	}

	/**
	 * Create an asset from a URI. This asset is considered to be an old asset. See {@link Asset#isNew()}
	 *
	 * @param uri The URI to create an asset from
	 * @return The asset created from the URI
	 */
	public Asset createAsset( URI uri ) throws AssetException {
		return doCreateAsset( null, uri );
	}

	/**
	 * Create an asset from a file. This asset is considered to be an old asset. See {@link Asset#isNew()}
	 *
	 * @param file The file to create an asset from
	 * @return The asset created from the file
	 */
	public Asset createAsset( File file ) throws AssetException {
		return doCreateAsset( null, file.toURI() );
	}

	/**
	 * Create an asset from an asset type. This asset is considered to be a new asset. See {@link Asset#isNew()}
	 *
	 * @param type The asset type to create an asset from
	 * @return The asset created from the asset type
	 */
	public Asset createAsset( AssetType type ) throws AssetException {
		return doCreateAsset( type, null );
	}

	/**
	 * Create an asset from an asset type and uri.
	 *
	 * @param type The asset type
	 * @param uri The asset uri
	 * @return The created asset
	 */
	public Asset createAsset( AssetType type, URI uri ) throws AssetException {
		return doCreateAsset( type, uri );
	}

	/**
	 * Create assets from an array of descriptors. Descriptors are preferred in the following order: URI, File, String, Object
	 *
	 * @param descriptors The descriptors from which to create assets
	 * @return The list of assets created from the descriptors
	 */
	public Collection<Asset> createAssets( Object... descriptors ) throws AssetException {
		return createAssets( List.of( descriptors ) );
	}

	/**
	 * Create assets from a collection of descriptors. Descriptors are preferred in the following order: URI, File, String, Object
	 *
	 * @param descriptors The descriptors from which to create assets
	 * @return The list of assets created from the descriptors
	 */
	public Collection<Asset> createAssets( Collection<?> descriptors ) throws AssetException {
		List<Asset> assets = new ArrayList<>( descriptors.size() );

		for( Object descriptor : descriptors ) {
			assets.add( createAsset( descriptor ) );
		}

		return assets;
	}

	/**
	 * Request that the specified assets be opened. This method submits a task to the task manager and returns immediately.
	 *
	 * @param assets The assets to open
	 */
	public void openAssets( Asset... assets ) throws AssetException {
		openAssets( List.of( assets ) );
	}

	/**
	 * Request that the specified assets be opened. This method submits a task to the task manager and returns immediately.
	 *
	 * @param assets The assets to open
	 */
	public void openAssets( Collection<Asset> assets ) throws AssetException {
		program.getTaskManager().submit( new OpenAssetTask( removeOpenAssets( assets ) ) );
	}

	/**
	 * Request that the specified assets be opened and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param assets The assets to open
	 * @throws ExecutionException If there was an exception opening the asset
	 * @throws InterruptedException If the process of opening the asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void openAssetsAndWait( Asset... assets ) throws ExecutionException, InterruptedException {
		openAssetsAndWait( List.of( assets ) );
	}

	/**
	 * Request that the specified assets be opened and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param assets The assets to open
	 * @throws ExecutionException If there was an exception opening an asset
	 * @throws InterruptedException If the process of opening an asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void openAssetsAndWait( Collection<Asset> assets ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new OpenAssetTask( removeOpenAssets( assets ) ) ).get();
	}

	/**
	 * Request that the specified assets be loaded. This method submits a task to the task manager and returns immediately.
	 *
	 * @param assets The assets to load
	 */
	public Future<Collection<Asset>> loadAssets( Asset... assets ) {
		return loadAssets( List.of( assets ) );
	}

	/**
	 * Request that the specified assets be loaded. This method submits a task to the task manager and returns immediately.
	 *
	 * @param assets The assets to load
	 */
	public Future<Collection<Asset>> loadAssets( Collection<Asset> assets ) {
		return program.getTaskManager().submit( new LoadAssetTask( assets ) );
	}

	/**
	 * Request that the specified assets be loaded and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param assets The assets to load
	 * @throws ExecutionException If there was an exception loading the asset
	 * @throws InterruptedException If the process of loading the asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void loadAssetsAndWait( Asset... assets ) throws ExecutionException, InterruptedException {
		loadAssetsAndWait( List.of( assets ) );
	}

	/**
	 * Request that the specified assets be loaded and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param assets The assets to load
	 * @throws ExecutionException If there was an exception loading the assets
	 * @throws InterruptedException If the process of loading the assets was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void loadAssetsAndWait( Collection<Asset> assets ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new LoadAssetTask( assets ) ).get();
	}

	/**
	 * Request that the specified assets be saved. This method submits a task to the task manager and returns immediately.
	 *
	 * @param asset The asset to save
	 */
	public void saveAssets( Asset asset ) {
		saveAssets( Collections.singletonList( asset ) );
	}

	/**
	 * Request that the specified assets be saved. This method submits a task to the task manager and returns immediately.
	 *
	 * @param assets The assets to save
	 */
	public void saveAssets( Collection<Asset> assets ) {
		program.getTaskManager().submit( new SaveAssetTask( assets ) );
	}

	/**
	 * Request that the specified assets be saved and wait until the task is complete. This method submits a task to the task manager and waits for the task to
	 * be completed.
	 *
	 * @param asset The asset to save
	 * @throws ExecutionException If there was an exception saving the asset
	 * @throws InterruptedException If the process of saving the asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void saveAssetsAndWait( Asset asset ) throws ExecutionException, InterruptedException {
		saveAssetsAndWait( Collections.singletonList( asset ) );
	}

	/**
	 * Request that the specified assets be saved and wait until the task is complete. This method submits a task to the task manager and waits for the task to
	 * be completed.
	 *
	 * @param assets The assets to save
	 * @throws ExecutionException If there was an exception saving the assets
	 * @throws InterruptedException If the process of saving the assets was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void saveAssetsAndWait( Collection<Asset> assets ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new SaveAssetTask( assets ) ).get();
	}

	/**
	 * Request that the specified assets be closed. This method submits a task to the task manager and returns immediately.
	 *
	 * @param asset The asset to close.
	 */
	public void closeAssets( Asset asset ) {
		closeAssets( Collections.singletonList( asset ) );
	}

	/**
	 * Request that the specified assets be closed. This method submits a task to the task manager and returns immediately.
	 *
	 * @param assets The assets to close.
	 */
	public void closeAssets( Collection<Asset> assets ) {
		program.getTaskManager().submit( new CloseAssetTask( assets ) );
	}

	/**
	 * Request that the specified assets be closed and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param asset The assets to close.
	 * @throws ExecutionException If there was an exception closing the asset
	 * @throws InterruptedException If the process of closing the asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void closeAssetsAndWait( Asset asset ) throws ExecutionException, InterruptedException {
		closeAssetsAndWait( Collections.singletonList( asset ) );
	}

	/**
	 * Request that the specified assets be closed and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param assets The assets to close.
	 * @throws ExecutionException If there was an exception closing the assets
	 * @throws InterruptedException If the process of closing the assets was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void closeAssetsAndWait( Collection<Asset> assets ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new CloseAssetTask( assets ) ).get();
	}

	/**
	 * Get a collection of the supported codecs.
	 *
	 * @return
	 */
	public Collection<Codec> getCodecs() {
		Set<Codec> codecs = new HashSet<>();

		for( AssetType type : assetTypesByTypeKey.values() ) {
			codecs.addAll( type.getCodecs() );
		}

		return Collections.unmodifiableCollection( codecs );
	}

	private Settings getSettings() {
		return program.getSettingsManager().getSettings( ManagerSettings.ASSET );
	}

	/**
	 * Determine the asset type for the given asset. The asset URI is used to find the asset type in the following order: <ol> <li>Lookup the asset
	 * type by the full URI</li> <li>Lookup the asset type by the URI scheme</li>
	 * <li>Find all the codecs that match the URI</li> <li>Sort the codecs by priority, select the highest</li> <li>Use the asset type associated to the
	 * codec</li> </ol>
	 *
	 * @param asset The asset for which to resolve the asset type
	 * @return
	 */
	AssetType autoDetectAssetType( Asset asset ) {
		URI uri = asset.getUri();
		AssetType type = null;

		// Look for asset types assigned to specific codecs
		List<Codec> codecs = new ArrayList<>( autoDetectCodecs( asset ) );
		codecs.sort( new CodecPriorityComparator().reversed() );
		Codec codec = codecs.size() == 0 ? null : codecs.get( 0 );
		if( type == null && codec != null ) type = codec.getAssetType();

		// Look for asset type assigned to specific URIs
		if( type == null && uri != null ) type = findMatchingUriAssetType( uri );

		// Look for asset types assigned to specific schemes
		if( type == null && uri != null ) type = schemeAssetTypes.get( uri.getScheme() );

		// Assign values to asset
		if( codec != null ) asset.setCodec( codec );
		if( type != null ) asset.setType( type );

		return type;
	}

	private AssetType findMatchingUriAssetType( URI uri ) {
		return uriAssetTypes.get( UriUtil.removeQueryAndFragment( uri ) );
	}

	/**
	 * Determine the codec for the given asset by checking the file name, the
	 * first line, and the content type for a match with a supported asset
	 * type. When calling this method the asset needs to already be open so
	 * that the information needed to determine the correct codec is defined in
	 * the asset.
	 * <p>
	 * Note: This method uses a URLConnection object to get the first line and
	 * content type of the asset. This means that the calling thread will be
	 * blocked during the IO operations used in URLConnection if the first line
	 * or the content type is needed to determine the asset type.
	 *
	 * @param asset The asset for which to find codecs
	 * @return The set of codecs that match the asset
	 */
	Set<Codec> autoDetectCodecs( Asset asset ) {
		Set<Codec> codecs = new HashSet<>();
		Collection<AssetType> assetTypes = getAssetTypes();

		// First option: Determine codec by media type
		String mediaType = getMediaType( asset );
		if( mediaType != null ) {
			for( AssetType assetType : assetTypes ) {
				Codec codec = assetType.getCodecByMediaType( mediaType );
				if( codec != null ) codecs.add( codec );
			}
		}

		// Second option: Determine codec by extension
		String fileName = asset.getFileName();
		if( fileName != null ) {
			for( AssetType assetType : assetTypes ) {
				Codec codec = assetType.getCodecByExtension( fileName );
				if( codec != null ) codecs.add( codec );
			}
		}

		// Third option: Determine codec by file name
		if( fileName != null ) {
			for( AssetType assetType : assetTypes ) {
				Codec codec = assetType.getCodecByFileName( fileName );
				if( codec != null ) codecs.add( codec );
			}
		}

		// Fouth option: Determine codec by first line
		String firstLine = getFirstLine( asset );
		if( firstLine != null ) {
			for( AssetType assetType : assetTypes ) {
				Codec codec = assetType.getCodecByFirstLine( firstLine );
				if( codec != null ) codecs.add( codec );
			}
		}

		return codecs;
	}

	private Collection<Asset> removeOpenAssets( Collection<Asset> assets ) {
		Collection<Asset> filteredAssets = new ArrayList<>( assets );
		for( Asset asset : openAssets ) {
			filteredAssets.remove( asset );
		}
		return filteredAssets;
	}

	// FIXME Need to check if callers really need to know if it is open or identified
	private boolean isManagedAssetOpen( Asset asset ) {
		return openAssets.contains( asset );
	}

	private void updateActionState() {
		newActionHandler.updateEnabled();
		openActionHandler.updateEnabled();
		saveActionHandler.updateEnabled();
		saveAsActionHandler.updateEnabled();
		saveAllActionHandler.updateEnabled();
		closeAllActionHandler.updateEnabled();
	}

	private void registerCodec( Codec codec, Set<String> values, Map<String, Set<Codec>> registrations ) {
		if( values == null ) return;

		for( String value : values ) {
			Set<Codec> registeredCodecs = registrations.computeIfAbsent( value, k -> new CopyOnWriteArraySet<Codec>() );
			registeredCodecs.add( codec );
		}
	}

	private void unregisterCodec( Codec codec, Set<String> values, Map<String, Set<Codec>> registrations ) {
		if( values == null ) return;

		for( String fileName : values ) {
			Set<Codec> registeredCodecs = registrations.get( fileName );
			if( registeredCodecs == null ) continue;
			registeredCodecs.remove( codec );
		}
	}

	/**
	 * Determine if all of the assets can be saved.
	 *
	 * @param assets The set of assets to check
	 * @return True if all of the assets can be saved
	 */
	private boolean canSaveAllAssets( Collection<Asset> assets ) {
		return assets.stream().mapToInt( a -> canSaveAsset( a ) ? 0 : 1 ).sum() == 0;
	}

	/**
	 * Determine if any of the assets can be saved.
	 *
	 * @param assets The set of assets to check
	 * @return True if any of the assets can be saved
	 */
	private boolean canSaveAnyAssets( Collection<Asset> assets ) {
		return assets.stream().mapToInt( a -> canSaveAsset( a ) ? 1 : 0 ).sum() > 0;
	}

	/**
	 * Determine if the asset can be saved. The asset can be saved if the URI is
	 * null or if the URI scheme and codec can both save assets.
	 *
	 * @param asset The asset to check
	 * @return True if the asset can be saved, false otherwise.
	 */
	private boolean canSaveAsset( Asset asset ) {
		if( asset == null || !asset.isModified() ) return false;
		if( asset.isNew() ) return true;

		// Check supported schemes.
		Scheme scheme = getScheme( asset.getUri().getScheme() );
		if( scheme == null ) return false;

		boolean result = false;
		try {
			Codec codec = asset.getCodec();
			result = scheme.canSave( asset ) && (codec == null || codec.canSave());
		} catch( AssetException exception ) {
			log.error( "Error checking if asset can be saved", exception );
		}

		return result;
	}

	/**
	 * Create an asset from an asset type and/or a URI. The asset is considered to be a new asset if the URI is null. Otherwise, the asset is
	 * considered an old asset. See {@link Asset#isNew()}
	 *
	 * @param type The asset type of the asset
	 * @param uri The URI of the asset
	 * @return The asset created from the asset type and URI
	 */
	private synchronized Asset doCreateAsset( AssetType type, URI uri ) throws AssetException {
		if( uri == null ) uri = URI.create( "asset:" + IdGenerator.getId() );
		uri = UriUtil.removeQueryAndFragment( uri );

		Asset asset = identifiedAssets.get( uri );
		if( asset == null ) {
			asset = new Asset( uri, type );
			identifiedAssets.put( uri, asset );
			Scheme scheme = getScheme( uri.getScheme() );
			asset.setScheme( scheme );
			scheme.init( asset );
			log.trace( "Asset create: " + asset + "[" + System.identityHashCode( asset ) + "] uri=" + uri );
		} else {
			log.trace( "Asset exists: " + asset + "[" + System.identityHashCode( asset ) + "] uri=" + uri );
		}

		return asset;
	}

	private boolean doOpenAsset( Asset asset ) throws AssetException {
		if( isManagedAssetOpen( asset ) ) return true;

		// Determine the asset type.
		AssetType type = asset.getType();
		if( type == null ) type = autoDetectAssetType( asset );
		if( type == null ) throw new AssetException( asset, "Asset type could not be determined: " + asset );

		// Determine the codec.
		Codec codec = asset.getCodec();
		if( codec == null ) {
			codec = asset.getType().getDefaultCodec();
			asset.setCodec( codec );
		}
		log.trace( "Asset codec: " + codec );

		// Create the asset settings
		asset.setSettings( getAssetSettings( asset ) );
		log.trace( "Asset settings: " + asset.getSettings().getPath() );

		// Initialize the asset.
		if( !type.assetInit( program, asset ) ) return false;
		log.trace( "Asset initialized with default values." );

		// If the asset is new get user input from the asset type.
		if( asset.isNew() ) {
			if( !type.assetUser( program, asset ) ) return false;
			log.trace( "Asset initialized with user values." );
		}

		// Open the asset.
		asset.open( this );

		// Add the asset to the list of open assets.
		openAssets.add( asset );

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.OPENED, asset ) );
		log.trace( "Asset opened: " + asset );

		updateActionState();
		return true;
	}

	private boolean doLoadAsset( Asset asset ) throws AssetException {
		if( asset == null ) return false;

		if( !asset.isOpen() ) doOpenAsset( asset );

		if( !asset.exists() ) return false;

		// Load the asset.
		boolean previouslyLoaded = asset.isLoaded();
		asset.load( this );

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.LOADED, asset ) );
		log.trace( "Asset loaded: " + asset );

		updateActionState();
		return true;
	}

	private boolean doSaveAsset( Asset asset ) throws AssetException {
		if( asset == null || !isManagedAssetOpen( asset ) ) return false;

		asset.save( this );
		identifiedAssets.put( asset.getUri(), asset );

		// Create the asset settings
		// TODO If the asset is changing URI the settings need to be moved
		asset.setSettings( getAssetSettings( asset ) );

		// TODO Update the asset type.

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.SAVED, asset ) );
		log.trace( "Asset saved: " + asset );

		updateActionState();
		return true;
	}

	private boolean doCloseAsset( Asset asset ) throws AssetException {
		if( asset == null ) return false;
		if( !isManagedAssetOpen( asset ) ) return false;

		asset.close( this );
		openAssets.remove( asset );
		identifiedAssets.remove( asset.getUri() );

		if( openAssets.size() == 0 ) doSetCurrentAsset( null );

		// TODO Delete the asset settings?
		// Should the settings be removed? Or left for later?
		//		Settings settings = asset.getSettings();
		//		if( settings != null ) settings.delete();

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.CLOSED, asset ) );
		log.trace( "Asset closed: " + asset );

		updateActionState();
		return true;
	}

	private boolean doSetCurrentAsset( Asset asset ) {
		synchronized( currentAssetLock ) {
			//log.warn( "Current asset: " + currentAsset + " new asset: " + asset );
			Asset previous = currentAsset;

			// "Disconnect" the old current asset.
			if( currentAsset != null ) {
				currentAsset.getEventBus().dispatch( new AssetEvent( this, AssetEvent.DEACTIVATED, currentAsset ) );
				currentAsset.getEventBus().unregister( AssetEvent.ANY, currentAssetWatcher );
			}

			// Change current asset.
			currentAsset = asset;

			// "Connect" the new current asset.
			if( currentAsset != null ) {
				currentAsset.getEventBus().register( AssetEvent.ANY, currentAssetWatcher );
				currentAsset.getEventBus().dispatch( new AssetEvent( this, AssetEvent.ACTIVATED, currentAsset ) );
			}

			// Notify program of current asset change.
			getEventBus().dispatch( new AssetSwitchedEvent( this, AssetSwitchedEvent.SWITCHED, previous, currentAsset ) );
			log.trace( "Asset select: " + asset );
		}

		updateActionState();
		return true;
	}

	private Settings getAssetSettings( Asset asset ) {
		return getAssetSettings( asset.getUri() );
	}

	private Settings getAssetSettings( URI uri ) {
		return program.getSettingsManager().getSettings( ProgramSettings.ASSET, IdGenerator.getId( uri.toString() ) );
	}

	//	/**
	//	 * @param asset
	//	 * @return
	//	 * @deprecated Instead use Scheme.getConnection( Asset )
	//	 */
	//	@Deprecated
	//	private URLConnection getConnection( Asset asset ) {
	//		URI uri = asset.getUri();
	//		Scheme scheme = getScheme( uri.getScheme() );
	//		if( scheme == null ) return null;
	//
	//		try {
	//			// FIXME Should not convert to URL to get a connection
	//      // TODO Should use scheme to get a connection
	//			//return uri.toURL().openConnection();
	//
	//			// It should come from the scheme
	//			//return scheme.openConnection( asset );
	//		} catch( Exception exception ) {
	//			log.warn( "Error opening asset connection", asset );
	//			log.warn( "Error opening asset connection", exception );
	//		}
	//
	//		return null;
	//	}

	private String getMediaType( Asset asset ) {
		String mediaType = asset.getResource( Asset.MEDIA_TYPE_ASSET_KEY );

		if( mediaType == null ) {
			URLConnection connection = asset.getScheme().getConnection( asset );
			if( connection != null ) {
				try {
					mediaType = TextUtil.cleanNull( connection.getContentType() );
					asset.putResource( Asset.MEDIA_TYPE_ASSET_KEY, mediaType );
					connection.getInputStream().close();
				} catch( IOException exception ) {
					log.warn( "Error closing asset connection", exception );
				}
			}
		}

		return mediaType;
	}

	private String getFirstLine( Asset asset ) {
		// Load the first line from the asset.
		String firstLine = null;

		URLConnection connection = asset.getScheme().getConnection( asset );
		if( connection != null ) {
			try {
				String encoding = asset.getEncoding();
				if( encoding == null ) encoding = connection.getContentEncoding();
				firstLine = readFirstLine( connection.getInputStream(), encoding );
				connection.getInputStream().close();
			} catch( IOException exception ) {
				log.warn( "Error closing asset connection", exception );
			}
		}

		return firstLine;
	}

	private String readFirstLine( InputStream input, String encoding ) throws IOException {
		if( input == null ) return null;

		byte[] buffer = new byte[ FIRST_LINE_LIMIT ];
		LimitedInputStream boundedInput = new LimitedInputStream( input, FIRST_LINE_LIMIT );
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		int read;
		int count = 0;
		while( (read = input.read( buffer )) > -1 ) {
			// Search for line termination.
			boolean eol = false;
			for( int index = 0; index < read; index++ ) {
				int data = buffer[ index ];
				if( data == 10 || data == 13 ) {
					read = index;
					eol = true;
					break;
				}
			}

			// Write the buffer.
			output.write( buffer, 0, read );
			count += read;

			// If a line break was encountered stop.
			if( eol ) break;
		}

		if( encoding == null ) encoding = ProgramDefaults.ENCODING;
		return TextUtil.cleanNull( new String( output.toByteArray(), encoding ) );
	}

	private File getFileChooserFolder() {
		File folder = new File( getSettings().get( CURRENT_FOLDER_SETTING_KEY, System.getProperty( "user.dir" ) ) );
		if( !folder.exists() || !folder.isDirectory() ) folder = new File( System.getProperty( "user.dir" ) );
		return folder;
	}

	private class NewOrOpenAssetTask extends Task<ProgramTool> {

		private OpenAssetRequest request;

		public NewOrOpenAssetTask( OpenAssetRequest request ) {
			this.request = request;
		}

		@Override
		public ProgramTool call() throws Exception {
			// Create and configure the asset
			Codec codec = request.getCodec();
			Asset asset = createAsset( request.getType(), request.getUri() );
			if( codec != null ) asset.setCodec( codec );

			// Open the asset
			openAssetsAndWait( asset );
			if( !asset.isOpen() || !isManagedAssetOpen( asset ) ) return null;

			// Create the tool if needed
			ProgramTool tool = request.isOpenTool() ? program.getToolManager().openTool( new OpenToolRequest( request ).setAsset( asset ) ) : null;

			// Start loading the asset after the tool has been created
			if( !asset.isLoaded() ) loadAssets( asset );

			return tool;
		}

	}

	private class NewActionHandler extends Action {

		private NewActionHandler( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return getUserAssetTypes().size() > 0;
		}

		@Override
		public void handle( ActionEvent event ) {
			Collection<AssetType> types = getUserAssetTypes();

			AssetType type = null;
			if( types.size() == 1 ) {
				type = types.iterator().next();
			} else {
				// TODO Re-enable AssetManager.NewActionHandler.handle()
				//					String title = program.getResourceBundle().getString( BundleKey.LABELS, "new" );
				//					AssetTypePanel panel = new AssetTypePanel( AssetManager.this );
				//
				//					int result = program.notify( title, panel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
				//
				//					if( result == JOptionPane.OK_OPTION ) type = panel.getAssetType();
			}

			newAsset( type );
		}

	}

	private class OpenActionHandler extends Action {

		private boolean isHandling;

		private OpenActionHandler( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return !isHandling && getUserAssetTypes().size() > 0;
		}

		@Override
		public void handle( ActionEvent event ) {
			// Disable the action while the dialog is open
			isHandling = true;
			updateEnabled();

			// NOTE This logic is very file oriented. It may need to move to the file scheme.
			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory( getFileChooserFolder() );
			File file = chooser.showOpenDialog( getProgram().getWorkspaceManager().getActiveStage() );

			if( file != null ) {
				File parent = file.isDirectory() ? file : file.getParentFile();
				getSettings().set( CURRENT_FOLDER_SETTING_KEY, parent.toString() );
				openAsset( file.toURI() );
			}

			isHandling = false;
			updateActionState();
		}

	}

	private class SaveActionHandler extends Action {

		private boolean saveAs;

		private boolean copy;

		private SaveActionHandler( Program program, boolean saveAs, boolean copy ) {
			super( program );
			this.saveAs = saveAs;
			this.copy = copy;
		}

		@Override
		public boolean isEnabled() {
			return canSaveAsset( getCurrentAsset() );
		}

		@Override
		public void handle( ActionEvent event ) {
			saveAsset( getCurrentAsset() );
		}

	}

	private class SaveAllActionHandler extends Action {

		private SaveAllActionHandler( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return canSaveAnyAssets( getModifiedAssets() );
		}

		@Override
		public void handle( ActionEvent event ) {
			try {
				saveAssets( getModifiedAssets() );
			} catch( Exception exception ) {
				program.getNoticeManager().error( exception );
			}
		}

	}

	private class CloseActionHandler extends Action {

		private CloseActionHandler( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return openAssets.size() > 0;
		}

		@Override
		public void handle( ActionEvent event ) {
			try {
				closeAssets( getCurrentAsset() );
			} catch( Exception exception ) {
				program.getNoticeManager().error( exception );
			}
		}

	}

	private class CloseAllActionHandler extends Action {

		private CloseAllActionHandler( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return canSaveAsset( getCurrentAsset() );
		}

		@Override
		public void handle( ActionEvent event ) {
			try {
				closeAssets( openAssets );
			} catch( Exception exception ) {
				program.getNoticeManager().error( exception );
			}
		}

	}

	private abstract class AssetTask extends ProgramTask<Collection<Asset>> {

		private Collection<Asset> assets;

		private AssetTask( Collection<Asset> assets ) {
			super( program );
			this.assets = assets;
		}

		@Override
		public Collection<Asset> call() {
			List<Asset> result = new ArrayList<>();
			Map<Throwable, Asset> throwables = new HashMap<>();
			if( assets == null ) {
				try {
					doOperation( null );
				} catch( Throwable throwable ) {
					throwables.put( throwable, NULL_ASSET );
				}
			} else {
				for( Asset asset : assets ) {
					try {
						if( doOperation( asset ) ) result.add( asset );
					} catch( Throwable throwable ) {
						throwables.put( throwable, asset );
					}
				}
			}

			if( throwables.size() != 0 ) {
				for( Throwable throwable : throwables.keySet() ) {
					String errorName = throwable.getClass().getSimpleName();
					String taskName = getClass().getSimpleName();
					String title = program.rb().text( "asset", "assets" );
					String message = program.rb().text( "program", "task-error-message", errorName, taskName );
					if( TestUtil.isTest() ) throwable.printStackTrace( System.err );
					program.getNoticeManager().warning( title, message, throwable );
				}

				//throw new RuntimeException( messages.toString().trim() );
			}

			return result;
		}

		abstract boolean doOperation( Asset asset ) throws AssetException;

		@Override
		public String toString() {
			if( assets == null || assets.size() == 0 ) return super.toString() + ": none";
			return super.toString() + ": " + assets.iterator().next().toString();
		}

	}

	private class OpenAssetTask extends AssetTask {

		private OpenAssetTask( Collection<Asset> assets ) {
			super( assets );
		}

		@Override
		public boolean doOperation( Asset asset ) throws AssetException {
			return doOpenAsset( asset );
		}

	}

	private class LoadAssetTask extends AssetTask {

		private LoadAssetTask( Collection<Asset> assets ) {
			super( assets );
		}

		@Override
		public boolean doOperation( Asset asset ) throws AssetException {
			return doLoadAsset( asset );
		}

	}

	private class SaveAssetTask extends AssetTask {

		private SaveAssetTask( Collection<Asset> assets ) {
			super( assets );
		}

		@Override
		public boolean doOperation( Asset asset ) throws AssetException {
			return doSaveAsset( asset );
		}

	}

	private class CloseAssetTask extends AssetTask {

		private CloseAssetTask( Collection<Asset> assets ) {
			super( assets );
		}

		@Override
		public boolean doOperation( Asset asset ) throws AssetException {
			return doCloseAsset( asset );
		}

	}

	private class SetCurrentAssetTask extends AssetTask {

		private SetCurrentAssetTask( Asset asset ) {
			// A null collection will call the operation with a null value
			super( asset == null ? null : Set.of( asset ) );
		}

		@Override
		public boolean doOperation( Asset asset ) {
			return doSetCurrentAsset( asset );
		}

	}

	private class CurrentAssetWatcher implements EventHandler<AssetEvent> {

		@Override
		public void handle( AssetEvent event ) {
			//log.warn( "Asset node " + event.getEventType() + ": " + event.getAsset() );
			if( event.getEventType() == AssetEvent.MODIFIED ) updateActionState();
			if( event.getEventType() == AssetEvent.UNMODIFIED ) updateActionState();
		}

	}

}
