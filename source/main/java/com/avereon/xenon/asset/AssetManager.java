package com.avereon.xenon.asset;

import com.avereon.event.EventHandler;
import com.avereon.product.Rb;
import com.avereon.settings.Settings;
import com.avereon.skill.Controllable;
import com.avereon.util.*;
import com.avereon.xenon.*;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.asset.type.ProgramAssetNewType;
import com.avereon.xenon.asset.type.ProgramAssetType;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.scheme.FileScheme;
import com.avereon.xenon.scheme.NewScheme;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.throwable.NoToolRegisteredException;
import com.avereon.xenon.throwable.SchemeNotRegisteredException;
import com.avereon.xenon.tool.AssetTool;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.zarra.event.FxEventHub;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@CustomLog
public class AssetManager implements Controllable<AssetManager> {

	private static final long DEFAULT_AUTOSAVE_MAX_TRIGGER_LIMIT = 5000;

	private static final long DEFAULT_AUTOSAVE_MIN_TRIGGER_LIMIT = 100;

	private static final String CURRENT_FILE_FOLDER_SETTING_KEY = "current-file-folder";

	private final Xenon program;

	private volatile Asset currentAsset;

	private final Set<Asset> openAssets;

	private final Map<URI, Asset> identifiedAssets;

	private final Map<String, Scheme> schemes;

	private final Map<String, AssetType> assetTypes;

	private final Map<Codec.Pattern, Map<String, Set<Codec>>> registeredCodecs;

	private final DelayedAction autosave;

	private final FxEventHub eventBus;

	private final NewActionHandler newActionHandler;

	private final OpenActionHandler openActionHandler;

	private final ReloadActionHandler reloadActionHandler;

	private final SaveActionHandler saveActionHandler;

	private final SaveActionHandler saveAsActionHandler;

	private final SaveAllActionHandler saveAllActionHandler;

	private final RenameActionHandler renameActionHandler;

	private final CloseActionHandler closeActionHandler;

	private final CloseAllActionHandler closeAllActionHandler;

	private final CurrentAssetWatcher currentAssetWatcher;

	private final GeneralAssetWatcher generalAssetWatcher;

	private final Object currentAssetLock = new Object();

	private final Map<URI, URI> aliases;

	private boolean running;

	public AssetManager( Xenon program ) {
		this.program = program;
		openAssets = new CopyOnWriteArraySet<>();
		identifiedAssets = new ConcurrentHashMap<>();
		schemes = new ConcurrentHashMap<>();
		assetTypes = new ConcurrentHashMap<>();
		registeredCodecs = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();

		// FIXME This is pretty dangerous for a couple of reasons
		// 1. It saves all assets, not just the current one
		// 2. In the event there was an error loading an asset, it can save the asset in a bad state
		// ?. Maybe this should be changed to save assets that submit themselves for autosave?
		autosave = new DelayedAction( program.getTaskManager().getExecutor(), this::saveAll );
		autosave.setMinTriggerLimit( program.getSettings().get( "autosave-trigger-min", Long.class, DEFAULT_AUTOSAVE_MIN_TRIGGER_LIMIT ) );
		autosave.setMaxTriggerLimit( program.getSettings().get( "autosave-trigger-max", Long.class, DEFAULT_AUTOSAVE_MAX_TRIGGER_LIMIT ) );

		eventBus = new FxEventHub();
		eventBus.parent( program.getFxEventHub() );
		currentAssetWatcher = new CurrentAssetWatcher();
		generalAssetWatcher = new GeneralAssetWatcher();

		newActionHandler = new NewActionHandler( program );
		openActionHandler = new OpenActionHandler( program );
		reloadActionHandler = new ReloadActionHandler( program );
		saveActionHandler = new SaveActionHandler( program, false );
		saveAsActionHandler = new SaveActionHandler( program, true );
		saveAllActionHandler = new SaveAllActionHandler( program );
		renameActionHandler = new RenameActionHandler( program );
		closeActionHandler = new CloseActionHandler( program );
		closeAllActionHandler = new CloseAllActionHandler( program );
	}

	public final Xenon getProgram() {
		return program;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public AssetManager start() {
		program.getActionLibrary().getAction( "new" ).pushAction( newActionHandler );
		program.getActionLibrary().getAction( "open" ).pushAction( openActionHandler );
		program.getActionLibrary().getAction( "reload" ).pushAction( reloadActionHandler );
		program.getActionLibrary().getAction( "save" ).pushAction( saveActionHandler );
		program.getActionLibrary().getAction( "save-as" ).pushAction( saveAsActionHandler );
		program.getActionLibrary().getAction( "save-all" ).pushAction( saveAllActionHandler );
		program.getActionLibrary().getAction( "rename" ).pushAction( renameActionHandler );
		program.getActionLibrary().getAction( "close" ).pushAction( closeActionHandler );
		program.getActionLibrary().getAction( "close-all" ).pushAction( closeAllActionHandler );
		updateActionState();

		//program.getEventHub().register( ToolEvent.ANY, activeToolWatcher );

		program.getSettings().register( "autosave-trigger-min", e -> autosave.setMinTriggerLimit( Long.parseLong( String.valueOf( e.getNewValue() ) ) ) );
		program.getSettings().register( "autosave-trigger-max", e -> autosave.setMaxTriggerLimit( Long.parseLong( String.valueOf( e.getNewValue() ) ) ) );

		running = true;

		return this;
	}

	@Override
	public AssetManager stop() {
		running = false;

		//program.getEventHub().unregister( ToolEvent.ANY, activeToolWatcher );

		return this;
	}

	public FxEventHub getEventBus() {
		return eventBus;
	}

	public Path getCurrentFileFolder() {
		// Determine the current folder
		// The current folder string is in URI format
		String currentFolderString = getProgram().getSettings().get( AssetManager.CURRENT_FILE_FOLDER_SETTING_KEY );
		log.atConfig().log( "Stored current folder: %s", currentFolderString );
		URI currentFolderUri = URI.create( currentFolderString );
		Path currentFolder = FileUtil.findValidFolder( currentFolderUri.toString() );
		//if( currentFolder == null ) currentFolder = FileSystems.getDefault().getPath( System.getProperty( "user.dir" ) );
		log.atConfig().log( "Result current folder: %s", currentFolderString );
		setCurrentFileFolder( currentFolder.toUri() );
		return currentFolder;
	}

	public void setCurrentFileFolder( Asset asset ) {
		setCurrentFileFolder( asset.getUri() );
	}

	private void setCurrentFileFolder( URI uri ) {
		if( !FileScheme.ID.equals( uri.getScheme() ) ) return;
		// Current folder value is store in URI format
		getProgram().getSettings().set( AssetManager.CURRENT_FILE_FOLDER_SETTING_KEY, uri );
	}

	public Asset getCurrentAsset() {
		return currentAsset;
	}

	public void setCurrentAsset( Asset asset ) {
		program.getTaskManager().submit( new SetCurrentAssetTask( asset ) );
	}

	public void setCurrentAssetAndWait( Asset asset ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new SetCurrentAssetTask( asset ) ).get();
	}

	public Set<Asset> getOpenAssets() {
		return new HashSet<>( openAssets );
	}

	public Set<Asset> getModifiedAssets() {
		return getOpenAssets().stream().filter( Asset::isModified ).collect( Collectors.toSet() );
	}

	Set<AssetType> getUserAssetTypes() {
		return assetTypes.values().stream().filter( AssetType::isUserType ).collect( Collectors.toSet() );
	}

	/**
	 * Get the externally modified assets.
	 *
	 * @return The set of externally modified assets
	 */
	public Set<Asset> getExternallyModifiedAssets() {
		return getOpenAssets().stream().filter( Asset::isExternallyModified ).collect( Collectors.toSet() );
	}

	/**
	 * Get a scheme by the scheme name.
	 *
	 * @param name The scheme name
	 * @return The scheme registered to the name
	 */
	public Scheme getScheme( String name ) {
		if( name == null ) name = FileScheme.ID;
		return schemes.get( name );
	}

	private void resolveScheme( Asset asset ) throws AssetException {
		resolveScheme( asset, asset.getUri().getScheme() );
	}

	private void resolveScheme( Asset asset, String name ) throws AssetException {
		Scheme scheme = getScheme( name );
		if( scheme == null ) throw new AssetException( asset, new SchemeNotRegisteredException( name ) );
		asset.setScheme( scheme );
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
	 * Get an asset type by the asset type key defined in the asset type. This is
	 * useful for getting asset types from persisted data.
	 *
	 * @param key The asset type key
	 * @return The asset type associated to the key
	 */
	public AssetType getAssetType( String key ) {
		if( key == null ) return null;
		AssetType type = assetTypes.get( key );
		if( type == null ) log.atWarning().log( "Asset type not found: %s", key );
		return type;
	}

	/**
	 * Get the set of supported asset types.
	 *
	 * @return The set of supported asset types
	 */
	public Collection<AssetType> getAssetTypes() {
		return Collections.unmodifiableCollection( assetTypes.values() );
	}

	/**
	 * Add an asset type to the set of supported asset types.
	 *
	 * @param type The asset type to add
	 */
	public void addAssetType( AssetType type ) {
		if( type == null ) return;

		synchronized( assetTypes ) {
			if( assetTypes.get( type.getKey() ) != null ) throw new IllegalArgumentException( "AssetType already exists: " + type.getKey() );

			// Register codecs
			for( Codec codec : type.getCodecs() ) {
				registerCodecs( Codec.Pattern.URI, codec );
				registerCodecs( Codec.Pattern.MEDIATYPE, codec );
				registerCodecs( Codec.Pattern.EXTENSION, codec );
				registerCodecs( Codec.Pattern.FILENAME, codec );
				registerCodecs( Codec.Pattern.SCHEME, codec );
				registerCodecs( Codec.Pattern.FIRSTLINE, codec );
			}

			// Add the asset type to the registered asset types.
			assetTypes.put( type.getKey(), type );

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
		synchronized( assetTypes ) {
			if( !assetTypes.containsKey( type.getKey() ) ) return;

			// Remove the asset type from the registered asset types
			type = assetTypes.remove( type.getKey() );

			for( Codec codec : type.getCodecs() ) {
				// Unregister codecs
				unregisterCodecs( Codec.Pattern.URI, codec );
				unregisterCodecs( Codec.Pattern.MEDIATYPE, codec );
				unregisterCodecs( Codec.Pattern.EXTENSION, codec );
				unregisterCodecs( Codec.Pattern.FILENAME, codec );
				unregisterCodecs( Codec.Pattern.SCHEME, codec );
				unregisterCodecs( Codec.Pattern.FIRSTLINE, codec );
			}

			// Update the actions.
			updateActionState();
		}
	}

	public Future<ProgramTool> newAsset( String key ) {
		return newAsset( key, null );
	}

	public Future<ProgramTool> newAsset( String key, Object model ) {
		return newAsset( getAssetType( key ), model, null, true, true );
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
		return newAsset( type, null, null, openTool, setActive );
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
	private Future<ProgramTool> newAsset( AssetType type, Object model, WorkpaneView view, boolean openTool, boolean setActive ) {
		OpenAssetRequest request = new OpenAssetRequest();
		request.setUri( null );
		request.setType( type );
		request.setModel( model );
		request.setView( view );
		request.setOpenTool( openTool );
		request.setSetActive( setActive );
		return program.getTaskManager().submit( new NewOrOpenAssetTask( request ) );
	}

	public Future<ProgramTool> openAsset( URI uri ) {
		return openAsset( uri, true, true );
	}

	public Future<ProgramTool> openAsset( URI uri, Object model ) {
		return openAsset( uri, model, null, null, null, true, true );
	}

	public Future<ProgramTool> openAsset( URI uri, Workpane pane ) {
		return openAsset( uri, null, pane, null, null, true, true );
	}

	public Future<ProgramTool> openAsset( URI uri, Class<? extends ProgramTool> toolClass ) {
		return openAsset( uri, null, null, null, toolClass, true, true );
	}

	public Future<ProgramTool> openAsset( URI uri, boolean openTool, boolean setActive ) {
		return openAsset( uri, null, null, null, null, openTool, setActive );
	}

	public Future<ProgramTool> openAsset( URI uri, Workpane pane, boolean openTool, boolean setActive ) {
		return openAsset( uri, null, pane, null, null, openTool, setActive );
	}

	public Future<ProgramTool> openAsset( URI uri, WorkpaneView view ) {
		return openAsset( uri, null, null, view, null, true, true );
	}

	public Future<ProgramTool> openAsset( URI uri, WorkpaneView view, Side side ) {
		if( side != null ) view = view.getWorkpane().split( view, side );
		return openAsset( uri, null, null, view, null, true, true );
	}

	public Set<Future<ProgramTool>> openDependencyAssets( Set<URI> uris, Workpane pane ) {
		return uris.stream().map( uri -> openAsset( uri, null, pane, null, null, true, false ) ).collect( Collectors.toSet() );
	}

	private Future<ProgramTool> openAsset( URI uri, Object model, Workpane pane, WorkpaneView view, Class<? extends ProgramTool> toolClass, boolean openTool, boolean setActive ) {
		OpenAssetRequest request = new OpenAssetRequest();
		request.setUri( uri );
		request.setPane( pane );
		request.setView( view );
		request.setOpenTool( openTool );
		request.setSetActive( setActive );
		request.setModel( model );
		request.setToolClass( toolClass );
		return program.getTaskManager().submit( new NewOrOpenAssetTask( request ) );
	}

	public Future<ProgramTool> openAsset( Asset asset ) {
		return openAsset( asset, null, null, null );
	}

	public Future<ProgramTool> openAsset( Asset asset, Class<? extends ProgramTool> toolClass ) {
		return openAsset( asset, null, null, toolClass );
	}

	public Future<ProgramTool> openAsset( Asset asset, WorkpaneView view ) {
		return openAsset( asset, view, null, null );
	}

	public Future<ProgramTool> openAsset( Asset asset, WorkpaneView view, Side side ) {
		return openAsset( asset, view, side, null );
	}

	public Future<ProgramTool> openAsset( Asset asset, WorkpaneView view, Side side, Class<? extends ProgramTool> toolClass ) {
		if( side != null ) view = view.getWorkpane().split( view, side );
		OpenAssetRequest request = new OpenAssetRequest();
		request.setAsset( asset );
		request.setView( view );
		request.setOpenTool( true );
		request.setSetActive( true );
		request.setToolClass( toolClass );
		return program.getTaskManager().submit( new NewOrOpenAssetTask( request ) );
	}

	public void reloadAsset( Asset asset ) {
		if( !asset.isLoaded() ) return;
		reloadAssets( asset );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void saveAsset( Asset asset ) {
		doSaveOrRenameAsset( asset, null, false, false );
	}

	/**
	 * Request that the source asset be saved as the target asset. This method
	 * submits a task to the task manager and returns immediately.
	 *
	 * @param source The source asset
	 * @param target The target asset
	 * @implNote This method makes calls to the FX platform.
	 */
	public void saveAsAsset( Asset source, Asset target ) {
		doSaveOrRenameAsset( source, target, true, false );
	}

	/**
	 * Request that the source asset be renamed as the target asset. This method
	 * submits a task to the task manager and returns immediately.
	 *
	 * @param source The source asset
	 * @param target The target asset
	 * @implNote This method makes calls to the FX platform.
	 */
	public void renameAsset( Asset source, Asset target ) {
		doSaveOrRenameAsset( source, target, false, true );
	}

	/**
	 * Close the asset, prompting the user if necessary.
	 *
	 * @param asset The asset to be closed
	 * @implNote This method makes calls to the FX platform.
	 */
	public void close( Asset asset ) {
		if( asset.isModified() && canSaveAsset( asset ) ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
			alert.setTitle( Rb.text( RbKey.ASSET, "close-save-title" ) );
			alert.setHeaderText( Rb.text( RbKey.ASSET, "close-save-message" ) );
			alert.setContentText( Rb.text( RbKey.ASSET, "close-save-prompt" ) );

			Stage stage = program.getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() == ButtonType.YES ) saveAsset( asset );
			if( result.isEmpty() || result.get() == ButtonType.CANCEL ) return;
		}

		closeAssets( asset );
	}

	public Asset createAsset( Object descriptor ) throws AssetException {
		if( descriptor instanceof URI ) {
			return (createAsset( (URI)descriptor ));
		} else if( descriptor instanceof File ) {
			return (createAsset( ((File)descriptor).toURI() ));
		} else if( descriptor instanceof Path ) {
			return (createAsset( ((Path)descriptor).toUri() ));
		} else {
			return (createAsset( descriptor.toString() ));
		}
	}

	/**
	 * Create an asset from a string. This asset is considered to be an old asset. See {@link Asset#isNew()}
	 *
	 * @param string The asset string
	 * @return A new asset based on the specified string.
	 */
	public Asset createAsset( String string ) throws AssetException {
		if( string == null ) return null;

		URI uri = UriUtil.resolve( string );

		if( uri == null ) {
			String title = Rb.text( "asset", "assets" );
			String message = Rb.text( "program", "asset-unable-to-resolve" );
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
	@Deprecated
	public Asset createAsset( File file ) throws AssetException {
		return doCreateAsset( null, file.toURI() );
	}

	/**
	 * Create an asset from a path. This asset is considered to be an old asset. See {@link Asset#isNew()}
	 *
	 * @param path The path to create an asset from
	 * @return The asset created from the path
	 */
	public Asset createAsset( Path path ) throws AssetException {
		return doCreateAsset( null, path.toUri() );
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

	public Asset createAsset( AssetType type, String uri ) throws AssetException {
		return doCreateAsset( type, UriUtil.resolve( uri ) );
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
		program.getTaskManager().submit( new OpenAssetTask( removeAlreadyOpenAssets( assets ) ) );
	}

	/**
	 * Request that the specified assets be opened and wait until the task is complete. This method submits a task to the task manager and waits for the task
	 * to be completed.
	 *
	 * @param asset The asset to open
	 * @throws ExecutionException If there was an exception opening the asset
	 * @throws InterruptedException If the process of opening the asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void openAssetsAndWait( Asset asset, long time, TimeUnit unit ) throws ExecutionException, InterruptedException, TimeoutException {
		openAssetsAndWait( List.of( asset ), time, unit );
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
	public void openAssetsAndWait( Collection<Asset> assets, long time, TimeUnit unit ) throws ExecutionException, InterruptedException, TimeoutException {
		program.getTaskManager().submit( new OpenAssetTask( removeAlreadyOpenAssets( assets ) ) ).get( time, unit );
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
	 * Request that the specified assets be reloaded. This method submits a task
	 * to the task manager and returns immediately.
	 *
	 * @param asset The asset to reload
	 */
	public void reloadAssets( Asset asset ) {
		reloadAssets( Collections.singletonList( asset ) );
	}

	/**
	 * Request that the specified assets be reloaded. This method submits a task
	 * to the task manager and returns immediately.
	 *
	 * @param assets The assets to reload
	 */
	public void reloadAssets( Collection<Asset> assets ) {
		program.getTaskManager().submit( new ReloadAssetTask( assets ) );
	}

	/**
	 * Request that the specified assets be reloaded and wait until the task is
	 * complete. This method submits a task to the task manager and waits for the
	 * task to be completed.
	 *
	 * @param asset The asset to reload
	 * @throws ExecutionException If there was an exception reloading the asset
	 * @throws InterruptedException If the process of reloading the asset was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void reloadAssetsAndWait( Asset asset ) throws ExecutionException, InterruptedException {
		reloadAssetsAndWait( Collections.singletonList( asset ) );
	}

	/**
	 * Request that the specified assets be reloaded and wait until the task is
	 * complete. This method submits a task to the task manager and waits for the
	 * task to be completed.
	 *
	 * @param assets The assets to reload
	 * @throws ExecutionException If there was an exception reloading the assets
	 * @throws InterruptedException If the process of reloading the assets was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void reloadAssetsAndWait( Collection<Asset> assets ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new ReloadAssetTask( assets ) ).get();
	}

	/**
	 * Request that all modified assets be saved. This method submits a task to
	 * the task manager and returns immediately.
	 */
	public void saveAll() {
		saveAssets( getModifiedAssets() );
		autosave.reset();
	}

	/**
	 * Request that the specified assets be saved. This method submits a task to
	 * the task manager and returns immediately.
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
	 * Request that the specified assets be deleted. This method submits a task to
	 * the task manager and returns immediately.
	 *
	 * @param assets The assets to close.
	 */
	public void deleteAssets( Collection<Asset> assets ) {
		program.getTaskManager().submit( new DeleteAssetTask( assets ) );
	}

	/**
	 * Request that the specified assets be deleted and wait until the task is
	 * complete. This method submits a task to the task manager and waits for the
	 * task to be completed.
	 *
	 * @param assets The assets to delete.
	 * @throws ExecutionException If there was an exception deleting the assets
	 * @throws InterruptedException If the process of deleting the assets was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void deleteAssetsAndWait( Collection<Asset> assets ) throws ExecutionException, InterruptedException {
		program.getTaskManager().submit( new DeleteAssetTask( assets ) ).get();
	}

	/**
	 * Get a collection of the supported codecs.
	 *
	 * @return A collection of all supported codecs
	 */
	public Collection<Codec> getCodecs() {
		return assetTypes.values().stream().flatMap( t -> t.getCodecs().stream() ).collect( Collectors.toUnmodifiableSet() );
	}

	public Asset getParent( Asset asset ) throws AssetException {
		if( !UriUtil.hasParent( asset.getUri() ) ) return Asset.NONE;
		Asset parent = asset.getParent();
		if( parent == null ) parent = createAsset( UriUtil.getParent( asset.getUri() ) ).add( asset );
		return parent;
	}

	public Asset resolve( Asset asset, String name ) throws AssetException {
		if( !asset.isFolder() ) return asset;
		if( name == null ) return asset;
		return createAsset( asset.getUri().resolve( name.replace( " ", "%20" ) ) );
	}

	private Settings getSettings() {
		return program.getSettingsManager().getSettings( ManagerSettings.ASSET );
	}

	/**
	 * Determine the asset type for the given asset. The asset URI is used to find
	 * the asset type in the following order:
	 * <ol>
	 *   <li>Lookup the asset type by the full URI</li>
	 *   <li>Lookup the asset type by the URI scheme</li>
	 *   <li>Find all the codecs that match the URI</li>
	 *   <li>Sort the codecs by priority, select the highest</li>
	 *   <li>Use the asset type associated to the codec</li>
	 * </ol>
	 *
	 * @param asset The asset for which to resolve the asset type
	 * @return The auto detected asset type
	 */
	public AssetType autoDetectAssetType( Asset asset ) {
		AssetType type = null;

		// Look for asset types assigned to specific codecs
		List<Codec> codecs = new ArrayList<>( autoDetectCodecs( asset ) );
		codecs.sort( new CodecPriorityComparator().reversed() );
		Codec codec = codecs.isEmpty() ? null : codecs.getFirst();
		if( codec != null ) type = codec.getAssetType();

		// Assign values to asset
		if( codec != null ) asset.setCodec( codec );
		if( type != null ) asset.setType( type );

		return type;
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
	public Set<Codec> autoDetectCodecs( Asset asset ) {
		String uri = UriUtil.removeQueryAndFragment( asset.getUri() ).toString();
		String fileName = asset.getFileName();
		// FIXME Only query media type if there are supported codecs to compare with
		String mediaType = asset.getScheme().getMediaType( asset );
		// FIXME Only query first line if there are supported codecs to compare with
		String firstLine = asset.getScheme().getFirstLine( asset );

		Set<Codec> codecs = new HashSet<>();
		for( AssetType assetType : getAssetTypes() ) {
			codecs.addAll( assetType.getSupportedCodecs( Codec.Pattern.URI, uri ) );
			codecs.addAll( assetType.getSupportedCodecs( Codec.Pattern.SCHEME, uri ) );
			codecs.addAll( assetType.getSupportedCodecs( Codec.Pattern.MEDIATYPE, mediaType ) );
			codecs.addAll( assetType.getSupportedCodecs( Codec.Pattern.EXTENSION, fileName ) );
			codecs.addAll( assetType.getSupportedCodecs( Codec.Pattern.FILENAME, fileName ) );
			codecs.addAll( assetType.getSupportedCodecs( Codec.Pattern.FIRSTLINE, firstLine ) );
		}
		return codecs;
	}

	private Collection<Asset> removeAlreadyOpenAssets( Collection<Asset> assets ) {
		Collection<Asset> filteredAssets = new ArrayList<>( assets );
		for( Asset asset : openAssets ) {
			filteredAssets.remove( asset );
		}
		return filteredAssets;
	}

	private boolean isManagedAssetOpen( Asset asset ) {
		boolean isAssetOpen = asset.isOpen();
		boolean isInOpenAssets = openAssets.contains( asset );

		// This is a double check to ensure things are consistent
		if( isAssetOpen != isInOpenAssets ) log.atWarn().log( "Asset open: %s, %s", isAssetOpen, isInOpenAssets );

		return isAssetOpen;
	}

	private void updateActionState() {
		newActionHandler.updateEnabled();
		openActionHandler.updateEnabled();
		reloadActionHandler.updateEnabled();
		saveActionHandler.updateEnabled();
		saveAsActionHandler.updateEnabled();
		saveAllActionHandler.updateEnabled();
		renameActionHandler.updateEnabled();
		closeActionHandler.updateEnabled();
		closeAllActionHandler.updateEnabled();
	}

	private void registerCodecs( Codec.Pattern patternType, Codec codec ) {
		Set<String> patterns = codec.getSupported( patternType );
		Map<String, Set<Codec>> codecs = registeredCodecs.computeIfAbsent( patternType, ( k ) -> new ConcurrentHashMap<>() );
		patterns.forEach( pattern -> codecs.computeIfAbsent( pattern, k -> new CopyOnWriteArraySet<>() ).add( codec ) );
	}

	private void unregisterCodecs( Codec.Pattern patternType, Codec codec ) {
		Set<String> patterns = codec.getSupported( patternType );
		Map<String, Set<Codec>> codecs = registeredCodecs.getOrDefault( patternType, new HashMap<>() );
		patterns.forEach( pattern -> codecs.getOrDefault( pattern, new HashSet<>() ).remove( codec ) );
	}

	/**
	 * Determine if the asset can be reloaded. The asset can be reloaded if the
	 * asset is not new and is already loaded.
	 *
	 * @param asset The asset to check
	 * @return True if the asset can be reloaded, false otherwise.
	 */
	private boolean canReloadAsset( Asset asset ) {
		if( asset == null || asset.isNew() ) return false;
		return asset.isLoaded();
	}

	/**
	 * Determine if all the assets can be saved.
	 *
	 * @param assets The set of assets to check
	 * @return True if all the assets can be saved
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
		if( asset == null ) return false;

		if( asset.isNew() ) return true;
		if( !asset.isModified() ) return false;

		// Check supported schemes.
		Scheme scheme = getScheme( asset.getUri().getScheme() );
		if( scheme == null ) return false;

		boolean result = false;
		try {
			Codec codec = asset.getCodec();
			result = scheme.canSave( asset ) && (codec == null || codec.canSave());
		} catch( AssetException exception ) {
			log.atSevere().withCause( exception ).log( "Error checking if asset can be saved" );
		}

		return result;
	}

	/**
	 * Determine if the asset can be renamed. The asset can be renamed if the
	 * asset is not new and is open.
	 *
	 * @param asset The asset to check
	 * @return True if the asset can be renamed, false otherwise.
	 */
	boolean canRenameAsset( Asset asset ) {
		return asset != null && !asset.isNew() && asset.isOpen();
	}

	public void registerAssetAlias( URI alias, URI uri ) {
		aliases.put( alias, uri );
	}

	public void unregisterAssetAlias( URI alias ) {
		aliases.remove( alias );
	}

	private URI resolveAssetAlias( URI uri ) {
		URI resolved = aliases.get( uri );
		if( resolved == null ) return uri;
		return resolved;
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
		if( uri == null ) uri = URI.create( NewScheme.ID + ":" + IdGenerator.getId() );

		uri = resolveAssetAlias( uri );

		// Many assets use query parameters and fragments in the URI,
		// so we need to clean up the URI before using it
		uri = uriCleanup( uri );

		Asset asset = identifiedAssets.get( uri );
		if( asset == null ) {
			asset = new Asset( type, uri );
			resolveScheme( asset );
			identifiedAssets.put( uri, asset );
			asset.setIcon( asset.isFolder() ? "folder" : "file" );
			log.atDebug().log( "Asset create: %s", asset );
		} else {
			log.atDebug().log( "Asset exists: %s", asset );
		}

		return asset;
	}

	private boolean doOpenAsset( Asset asset ) throws AssetException {
		if( isManagedAssetOpen( asset ) ) return true;

		// Determine the asset type
		AssetType type = asset.getType();
		if( type == null ) type = autoDetectAssetType( asset );

		if( type == null ) {
			log.atWarn().log( "Asset type not found: " + asset.getMediaType() );
			String title = Rb.text( RbKey.LABEL, "asset" );
			String message = Rb.text( RbKey.ASSET, "asset-type-not-supported", asset.getFileName() );
			Notice notice = new Notice( title, message ).setType( Notice.Type.WARN );
			getProgram().getNoticeManager().addNotice( notice );
			return false;
		}

		// Determine the codec
		Codec codec = asset.getCodec();
		if( codec == null ) {
			codec = asset.getType().getDefaultCodec();
			asset.setCodec( codec );
		}
		log.atFiner().log( "Asset codec: %s", codec );

		// Initialize the asset
		if( !type.callAssetOpen( program, asset ) ) return false;
		log.atFiner().log( "Asset initialized with default values." );

		// Register the general asset listener
		asset.register( AssetEvent.ANY, generalAssetWatcher );

		// Open the asset
		asset.open( this );

		// Add the asset to the list of open assets
		openAssets.add( asset );

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.OPENED, asset ) );
		log.atDebug().log( "Asset opened: %s", asset );

		updateActionState();
		return true;
	}

	private boolean doLoadAsset( Asset asset ) throws AssetException {
		if( asset == null ) return false;

		if( !asset.isNew() && !asset.exists() ) {
			log.atWarn().log( "Asset not found: " + asset );
			return false;
		}

		if( !asset.isOpen() ) doOpenAsset( asset );
		if( !asset.getScheme().canLoad( asset ) ) return false;

		// Load the asset
		log.atTrace().log( "Loading asset " + asset.getUri() );
		asset.load( this );
		getEventBus().dispatch( new AssetEvent( this, AssetEvent.LOADED, asset ) );
		log.atInfo().log( "Loaded: %s", asset );

		updateActionState();
		return true;
	}

	private boolean doReloadAsset( Asset asset ) throws AssetException {
		if( asset == null || !asset.isLoaded() ) return false;

		asset.load( this );
		getEventBus().dispatch( new AssetEvent( this, AssetEvent.LOADED, asset ) );
		log.atFiner().log( "Asset reloaded: %s", asset );

		updateActionState();
		return true;
	}

	private boolean doSaveAsset( Asset asset ) throws AssetException {
		if( asset == null || !isManagedAssetOpen( asset ) || !asset.isSafeToSave() ) return false;

		if( !asset.getScheme().canSave( asset ) ) return false;

		asset.save( this );
		identifiedAssets.put( asset.getUri(), asset );

		// TODO If the asset is changing URI the settings need to be moved

		// TODO Update the asset type.

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.SAVED, asset ) );
		log.atInfo().log( "Saved: %s", asset );

		updateActionState();
		return true;
	}

	private boolean doCloseAsset( Asset asset ) throws AssetException {
		if( asset == null ) return false;
		if( !isManagedAssetOpen( asset ) ) return false;

		// Close the asset
		asset.close( this );

		// Unregister the general asset listener
		asset.unregister( AssetEvent.ANY, generalAssetWatcher );

		// Remove the asset from the list of open assets
		openAssets.remove( asset );
		identifiedAssets.remove( asset.getUri() );

		if( openAssets.isEmpty() ) doSetCurrentAsset( null );

		// TODO Delete the asset settings?
		// Should the settings be removed? Or left for later?
		// Recommended not to delete the asset settings.
		// Maybe have a settings cleanup task and/or user actions

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.CLOSED, asset ) );
		log.atDebug().log( "Asset closed: %s", asset );

		updateActionState();
		return true;
	}

	private boolean doDeleteAsset( Asset asset ) throws AssetException {
		if( asset == null ) return false;
		if( asset.isOpen() ) doCloseAsset( asset );

		// Delete the asset
		asset.delete();

		getEventBus().dispatch( new AssetEvent( this, AssetEvent.DELETED, asset ) );
		log.atDebug().log( "Asset deleted: %s", asset );

		updateActionState();
		return true;
	}

	/**
	 * Save the asset, prompting the user if necessary.
	 *
	 * @param source The asset to be saved
	 * @param target The asset to save as
	 * @param saveAs The save as flag
	 * @param rename The rename flag
	 * @implNote This method makes calls to the FX platform.
	 */
	private void doSaveOrRenameAsset( Asset source, Asset target, boolean saveAs, boolean rename ) {
		try {
			boolean needsTargetAsset = source.isNew() || ((saveAs || rename) && target == null);
			if( needsTargetAsset ) {
				askForTargetAsset( source, saveAs, rename );
			} else {
				saveAssets( source );
			}
		} catch( AssetException exception ) {
			log.atSevere().withCause( exception ).log();
		}
	}

	private String generateFilename() {
		return "asset" + (currentAsset == null ? "" : "." + currentAsset.getCodec().getDefaultExtension());
	}

	private void askForTargetAsset( Asset source, boolean saveAs, boolean rename ) throws AssetException {
		Codec codec = source.getCodec();
		if( codec == null ) codec = source.getType().getDefaultCodec();

		// Determine the asset path
		Path folder = source.isNew() ? getCurrentFileFolder() : Path.of( getParent( source ).getUri() );
		String filename = source.isNew() ? generateFilename() : source.getFileName();
		Path assetPath = folder.resolve( filename );

		// Build a URI to open the asset tool
		String uriString = ProgramAssetType.URI + "?mode=" + AssetTool.Mode.SAVE + "&uri=" + assetPath.toUri();
		log.atTrace().log( "save asset uri=%s", URI.create( uriString ) );

		final Asset finalAsset = source;
		final Codec finalCodec = codec;
		program.getTaskManager().submit( Task.of( () -> {
			try {
				Map<Codec, AssetFilter> filters = generateAssetFilters( finalAsset.getType() );
				AssetTool tool = (AssetTool)openAsset( URI.create( uriString ) ).get();
				tool.getFilters().addAll( 0, filters.values() );
				tool.setSelectedFilter( filters.get( finalCodec ) );
				tool.setSaveActionConsumer( target -> doAfterAssetTool( tool, filters, source, target, saveAs, rename ) );
			} catch( Exception exception ) {
				log.atWarn().withCause( exception ).log();
			}
		} ) );
	}

	private void doAfterAssetTool( AssetTool tool, Map<Codec, AssetFilter> filters, Asset source, Asset target, boolean saveAs, boolean rename ) {
		try {
			Asset folder = target.isFolder() ? target : getParent( target );

			// Store the current folder in the settings
			setCurrentFileFolder( folder );

			// If the user specified a codec use it to set the codec and asset type
			Map<AssetFilter, Codec> filterCodecs = MapUtil.mirror( filters );
			Codec selectedCodec = filterCodecs.get( tool.getSelectedFilter() );

			// If the extension is not already supported use the default extension from the codec
			if( !target.exists() && selectedCodec != null && !selectedCodec.isSupported( Codec.Pattern.EXTENSION, target.getFileName() ) ) {
				target = resolve( folder, target.getFileName() + "." + selectedCodec.getDefaultExtension() );
			}

			// Update the target asset
			if( selectedCodec != null ) target.setCodec( selectedCodec );

			if( source.isNew() || saveAs ) {
				doSaveAsAsset( source, target );
			} else if( rename ) {
				doRenameAsset( source, target );
			}
		} catch( AssetException exception ) {
			log.atError( exception ).log();
		}
	}

	private void doSaveAsAsset( Asset source, Asset target ) throws AssetException {
		if( source == null || target == null ) return;

		copySettings( source, target, false );

		// Use the scheme to save the source to the target
		target.getScheme().saveAs( source, target );

		if( source.isNew() ) closeAssets( source );
		openAsset( target.getUri() );
	}

	private void doRenameAsset( Asset source, Asset target ) throws AssetException {
		if( source == null || target == null ) return;

		copySettings( source, target, true );

		// Use the scheme to rename the source to the target
		target.getScheme().rename( source, target );

		openAsset( target.getUri() );
		closeAssets( source );
	}

	private void copySettings( Asset source, Asset target, boolean delete ) {
		Settings sourceSettings = getProgram().getSettingsManager().getAssetSettings( source );
		Settings targetSettings = getProgram().getSettingsManager().getAssetSettings( target );
		targetSettings.copyFrom( sourceSettings );
		if( delete ) sourceSettings.delete();
	}

	private Map<Codec, AssetFilter> generateAssetFilters( AssetType type ) {
		Map<Codec, AssetFilter> filters = new HashMap<>();
		type.getCodecs().forEach( c -> filters.put( c, new CodecAssetFilter( c ) ) );
		return filters;
	}

	URI uriCleanup( URI uri ) {
		return UriUtil.removeQueryAndFragment( uri ).normalize();
	}

	private boolean doSetCurrentAsset( Asset asset ) {
		synchronized( currentAssetLock ) {
			//log.log( Log.WARN,  "Current asset: " + currentAsset + " new asset: " + asset );
			Asset previous = currentAsset;

			// "Disconnect" the old current asset
			if( currentAsset != null ) {
				currentAsset.getEventHub().dispatch( new AssetEvent( this, AssetEvent.DEACTIVATED, currentAsset ) );
				currentAsset.getEventHub().unregister( AssetEvent.ANY, currentAssetWatcher );
			}

			// Change current asset
			currentAsset = asset;

			// "Connect" the new current asset
			if( currentAsset != null ) {
				currentAsset.getEventHub().register( AssetEvent.ANY, currentAssetWatcher );
				currentAsset.getEventHub().dispatch( new AssetEvent( this, AssetEvent.ACTIVATED, currentAsset ) );
			}

			// Notify program of current asset change
			getEventBus().dispatch( new AssetSwitchedEvent( this, AssetSwitchedEvent.SWITCHED, previous, currentAsset ) );
			log.atFiner().log( "Asset select: %s", asset );
		}

		updateActionState();
		return true;
	}

	private class NewOrOpenAssetTask extends Task<ProgramTool> {

		private final OpenAssetRequest request;

		public NewOrOpenAssetTask( OpenAssetRequest request ) {
			this.request = request;
		}

		@Override
		public ProgramTool call() throws AssetException, ExecutionException, TimeoutException, InterruptedException {
			// Create and configure the asset
			if( request.getAsset() == null ) request.setAsset( createAsset( request.getType(), request.getUri() ) );
			Asset asset = request.getAsset();
			Object model = request.getModel();
			Codec codec = request.getCodec();
			if( model != null ) asset.setModel( model );
			if( codec != null ) asset.setCodec( codec );

			// Open the asset
			openAssetsAndWait( asset, 5, TimeUnit.SECONDS );
			if( !isManagedAssetOpen( asset ) ) return null;

			// Create the tool if needed
			ProgramTool tool;
			try {
				// If the asset is new get user input from the asset type
				if( asset.isNew() ) {
					if( !asset.getType().callAssetNew( program, asset ) ) return null;
					log.atFiner().log( "Asset initialized with user values." );

					// The asset type may have changed the URI so resolve the scheme again
					resolveScheme( asset );
				}

				tool = request.isOpenTool() ? program.getToolManager().openTool( request ) : null;
			} catch( NoToolRegisteredException exception ) {
				log.atConfig().log( "No tool registered for: %s", asset );
				String title = Rb.text( "program", "no-tool-for-asset-title" );
				String message = Rb.text( "program", "no-tool-for-asset-message", asset.getUri().toString() );
				program.getNoticeManager().warning( title, message, asset.getName() );
				return null;
			}

			// Start loading the asset after the tool has been created
			if( !asset.isLoaded() ) loadAssets( asset );

			return tool;
		}

	}

	private class NewActionHandler extends ProgramAction {

		private NewActionHandler( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return !getUserAssetTypes().isEmpty();
		}

		@Override
		public void handle( ActionEvent event ) {
			Collection<AssetType> types = getUserAssetTypes();

			if( types.size() == 1 ) {
				newAsset( types.iterator().next() );
			} else {
				openAsset( ProgramAssetNewType.URI );
			}
		}

	}

	private class OpenActionHandler extends ProgramAction {

		private boolean isHandling;

		private OpenActionHandler( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return !isHandling && !getUserAssetTypes().isEmpty();
		}

		@Override
		public void handle( ActionEvent event ) {
			// Disable the action while the dialog is open
			isHandling = true;
			updateEnabled();

			openAsset( ProgramAssetType.OPEN_URI );

			isHandling = false;
			updateActionState();
		}

	}

	private class ReloadActionHandler extends ProgramAction {

		protected ReloadActionHandler( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return canReloadAsset( getCurrentAsset() );
		}

		@Override
		public void handle( ActionEvent event ) {
			reloadAsset( getCurrentAsset() );
		}

	}

	private class SaveActionHandler extends ProgramAction {

		private final boolean saveAs;

		private SaveActionHandler( Xenon program, boolean saveAs ) {
			super( program );
			this.saveAs = saveAs;
		}

		@Override
		public boolean isEnabled() {
			return (saveAs && getCurrentAsset() != null) || canSaveAsset( getCurrentAsset() );
		}

		@Override
		public void handle( ActionEvent event ) {
			saveAsAsset( getCurrentAsset(), null );
		}

	}

	private class SaveAllActionHandler extends ProgramAction {

		private SaveAllActionHandler( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return canSaveAnyAssets( getModifiedAssets() );
		}

		@Override
		public void handle( ActionEvent event ) {
			try {
				autosave.trigger();
			} catch( Exception exception ) {
				log.atSevere().withCause( exception ).log();
			}
		}

	}

	private class RenameActionHandler extends ProgramAction {

		private RenameActionHandler( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return canRenameAsset( getCurrentAsset() );
		}

		@Override
		public void handle( ActionEvent event ) {
			renameAsset( getCurrentAsset(), null );
		}

	}

	private class CloseActionHandler extends ProgramAction {

		private CloseActionHandler( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return openAssets.stream().anyMatch( a -> a.getType().isUserType() );
		}

		@Override
		public void handle( ActionEvent event ) {
			try {
				closeAssets( getCurrentAsset() );
			} catch( Exception exception ) {
				log.atSevere().withCause( exception ).log();
			}
		}

	}

	private class CloseAllActionHandler extends ProgramAction {

		private CloseAllActionHandler( Xenon program ) {
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
				log.atSevere().withCause( exception ).log();
			}
		}

	}

	private abstract class AssetTask extends ProgramTask<Collection<Asset>> {

		private final Collection<Asset> assets;

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
					throwables.put( throwable, Asset.NONE );
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

			if( !throwables.isEmpty() ) {
				for( Throwable throwable : throwables.keySet() ) {
					String errorName = throwable.getClass().getSimpleName();
					String taskName = getClass().getSimpleName();
					String message = Rb.text( "program", "task-error-message", errorName, taskName );
					if( TestUtil.isTest() ) throwable.printStackTrace( System.err );
					log.atWarning().withCause( throwable ).log( message );
				}
			}

			return result;
		}

		abstract boolean doOperation( Asset asset ) throws AssetException;

		@Override
		public String toString() {
			if( assets == null || assets.isEmpty() ) return super.toString() + ": none";
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

	private class ReloadAssetTask extends AssetTask {

		private ReloadAssetTask( Collection<Asset> assets ) {
			super( assets );
		}

		@Override
		public boolean doOperation( Asset asset ) throws AssetException {
			return doReloadAsset( asset );
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

	private class DeleteAssetTask extends AssetTask {

		private DeleteAssetTask( Collection<Asset> assets ) {
			super( assets );
		}

		@Override
		public boolean doOperation( Asset asset ) throws AssetException {
			return doDeleteAsset( asset );
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
			//System.err.println( "asset event=" + event );
			if( event.getEventType() == AssetEvent.MODIFIED ) updateActionState();
			if( event.getEventType() == AssetEvent.UNMODIFIED ) updateActionState();
		}

	}

	private class GeneralAssetWatcher implements EventHandler<AssetEvent> {

		@Override
		public void handle( AssetEvent event ) {
			autosave.request();
		}

	}

}
