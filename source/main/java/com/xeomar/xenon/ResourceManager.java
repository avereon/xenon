package com.xeomar.xenon;

import com.xeomar.util.IdGenerator;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.node.NodeEvent;
import com.xeomar.xenon.node.NodeListener;
import com.xeomar.xenon.resource.*;
import com.xeomar.xenon.resource.event.*;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.util.Controllable;
import com.xeomar.xenon.util.UriUtil;
import com.xeomar.xenon.workarea.WorkpaneView;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

// FIXME Add Configurable interface to this class
public class ResourceManager implements Controllable<ResourceManager> {

	public static final String CURRENT_DIRECTORY_SETTING_KEY = "current-folder";

	// Linux defines this limit in BINPRM_BUF_SIZE
	private static final int FIRST_LINE_LIMIT = 128;

	private static Logger log = LogUtil.get( ResourceManager.class );

	private Program program;

	private volatile Resource currentResource;

	private final Set<Resource> openResources;

	private final Map<String, Scheme> schemes;

	private final Map<String, ResourceType> resourceTypes;

	private final Map<String, ResourceType> uriResourceTypes;

	private final Map<String, ResourceType> schemeResourceTypes;

	private final Map<String, Set<Codec>> registeredFileNames;

	private final Map<String, Set<Codec>> registeredFirstLines;

	private final Map<String, Set<Codec>> registeredMediaTypes;

	private NewActionHandler newActionHandler;

	private OpenActionHandler openActionHandler;

	private SaveActionHandler saveActionHandler;

	private SaveActionHandler saveAsActionHandler;

	private SaveActionHandler saveCopyAsActionHandler;

	private SaveAllActionHandler saveAllActionHandler;

	private CloseActionHandler closeActionHandler;

	private CloseAllActionHandler closeAllActionHandler;

	private CurrentResourceWatcher currentResourceWatcher = new CurrentResourceWatcher();

	private ModifiedResourceWatcher modifiedResourceWatcher = new ModifiedResourceWatcher();

	private final Object restoreLock = new Object();

	private final Object currentResourceLock = new Object();

	public ResourceManager( Program program ) {
		this.program = program;
		openResources = new CopyOnWriteArraySet<>();
		schemes = new ConcurrentHashMap<>();
		resourceTypes = new ConcurrentHashMap<>();
		uriResourceTypes = new ConcurrentHashMap<>();
		schemeResourceTypes = new ConcurrentHashMap<>();
		registeredFileNames = new ConcurrentHashMap<>();
		registeredFirstLines = new ConcurrentHashMap<>();
		registeredMediaTypes = new ConcurrentHashMap<>();

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
	public ResourceManager start() {
		//		((FileScheme)Schemes.getScheme( "file" )).startResourceWatching();

		program.getActionLibrary().getAction( "new" ).pushAction( newActionHandler );
		program.getActionLibrary().getAction( "open" ).pushAction( openActionHandler );
		program.getActionLibrary().getAction( "save" ).pushAction( saveActionHandler );
		program.getActionLibrary().getAction( "save-as" ).pushAction( saveAsActionHandler );
		program.getActionLibrary().getAction( "copy-as" ).pushAction( saveCopyAsActionHandler );
		program.getActionLibrary().getAction( "save-all" ).pushAction( saveAllActionHandler );
		program.getActionLibrary().getAction( "close" ).pushAction( closeActionHandler );
		program.getActionLibrary().getAction( "close-all" ).pushAction( closeAllActionHandler );

		updateActionState();
		return this;
	}

	@Override
	public ResourceManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public boolean isRunning() {
		// TODO Return a real value for ResourceManager.isRunning()
		return false;
	}

	@Override
	public ResourceManager restart() {
		stop();
		start();
		return this;
	}

	@Override
	public ResourceManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return awaitStart( timeout, unit );
	}

	@Override
	public ResourceManager stop() {
		//		((FileScheme)Schemes.getScheme( "file" )).stopResourceWatching();
		return this;
	}

	@Override
	public ResourceManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	public Resource getCurrentResource() {
		return currentResource;
	}

	public void setCurrentResource( Resource resource ) {
		program.getExecutor().submit( new SetCurrentResourceTask( resource ) );
	}

	public void setCurrentResourceAndWait( Resource resource ) throws ExecutionException, InterruptedException {
		program.getExecutor().submit( new SetCurrentResourceTask( resource ) ).get();
	}

	public List<Resource> getOpenResources() {
		return new ArrayList<>( openResources );
	}

	public List<Resource> getModifiedResources() {
		List<Resource> modifiedResources = new ArrayList<>();
		for( Resource resource : getOpenResources() ) {
			if( resource.isModified() && canSaveResource( resource ) ) modifiedResources.add( resource );
		}
		return modifiedResources;
	}

	Set<ResourceType> getUserResourceTypes() {
		Set<ResourceType> userResourceTypes = new HashSet<>();

		for( ResourceType type : resourceTypes.values() ) {
			if( type.isUserType() ) userResourceTypes.add( type );
		}

		return userResourceTypes;
	}

	/**
	 * Get the externally modified resources.
	 *
	 * @return The set of externally modified resources
	 */
	public Set<Resource> getExternallyModifiedResources() {
		Set<Resource> externallyModifiedResources = new HashSet<Resource>();
		for( Resource resource : getOpenResources() ) {
			if( resource.isExternallyModified() ) externallyModifiedResources.add( resource );
		}
		return Collections.unmodifiableSet( externallyModifiedResources );
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
	 * Get a resource type by the resource type key defined in the resource type. This is useful for getting resource types from persisted data.
	 *
	 * @param key The resource type key
	 * @return The resource type associated to the key
	 */
	public ResourceType getResourceType( String key ) {
		ResourceType type = resourceTypes.get( key );
		if( type == null ) log.warn( "Resource type not found: " + key );
		return type;
	}

	/**
	 * Get the set of supported resource types.
	 *
	 * @return The set of supported resource types
	 */
	public Collection<ResourceType> getResourceTypes() {
		return Collections.unmodifiableCollection( resourceTypes.values() );
	}

	/**
	 * Add a resource type to the set of supported resource types.
	 *
	 * @param type The resource type to add
	 */
	public void addResourceType( ResourceType type ) {
		if( type == null ) return;

		synchronized( resourceTypes ) {
			if( resourceTypes.get( type.getKey() ) != null ) throw new IllegalArgumentException( "ResourceType already exists: " + type.getKey() );

			Set<Codec> codecs = type.getCodecs();
			for( Codec codec : codecs ) {
				// Register codec support.
				registerCodec( codec, codec.getSupportedFileNames(), registeredFileNames );
				registerCodec( codec, codec.getSupportedFirstLines(), registeredFirstLines );
				registerCodec( codec, codec.getSupportedMediaTypes(), registeredMediaTypes );
			}

			// Add the resource type to the registered resource types.
			resourceTypes.put( type.getKey(), type );

			// Update the actions.
			updateActionState();
		}
	}

	/**
	 * Remove a resource type from the set of supported resource types.
	 *
	 * @param type The resource type to remove
	 */
	public void removeResourceType( ResourceType type ) {
		if( type == null ) return;
		synchronized( resourceTypes ) {
			if( !resourceTypes.containsKey( type.getKey() ) ) return;

			// Remove the resource type from the registered resource types
			resourceTypes.remove( type.getKey() );
			for( Map.Entry entry : uriResourceTypes.entrySet() ) {
				if( entry.getValue() == type ) uriResourceTypes.remove( entry.getKey() );
			}
			for( Map.Entry entry : schemeResourceTypes.entrySet() ) {
				if( entry.getValue() == type ) schemeResourceTypes.remove( entry.getKey() );
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

	public void registerUriResourceType( String uri, ResourceType type ) {
		if( resourceTypes.get( type.getKey() ) == null ) addResourceType( type );
		uriResourceTypes.put( uri, type );
	}

	public void unregisterUriResourceType( String uri ) {
		uriResourceTypes.remove( uri );
	}

	public void registerSchemeResourceType( String scheme, ResourceType type ) {
		if( resourceTypes.get( type.getKey() ) == null ) addResourceType( type );
		schemeResourceTypes.put( scheme, type );
	}

	public void unregisterSchemeResourceType( String scheme ) {
		schemeResourceTypes.remove( scheme );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Resource resource ) {
		open( Collections.singletonList( resource ) );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Resource resource, boolean openTool ) {
		open( Collections.singletonList( resource ), openTool );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Resource resource, boolean openTool, boolean setActive ) {
		open( Collections.singletonList( resource ), null, openTool, setActive );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Collection<Resource> resources ) {
		open( resources, true );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Collection<Resource> resources, WorkpaneView view ) {
		open( resources, view, true );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Collection<Resource> resources, boolean openTool ) {
		open( resources, null, openTool );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Collection<Resource> resources, WorkpaneView view, boolean openTool ) {
		open( resources, view, openTool, true );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void open( Collection<Resource> resources, WorkpaneView view, boolean openTool, boolean setActive ) {
		program.getExecutor().submit( new OpenActionTask( resources, null, view, openTool, setActive ) );
	}

	/**
	 * @implNote This method makes calls to the FX platform.
	 */
	public void save( Resource resource ) {
		save( resource, null, false, false );
	}

	/**
	 * Request that the source resource be saved as the target resource. This method submits a task to the task manager and returns immediately.
	 *
	 * @param source The source resource
	 * @param target The target resource
	 * @implNote This method makes calls to the FX platform.
	 */
	public void saveAsResource( Resource source, Resource target ) {
		save( source, target, true, false );
	}

	//	/**
	//	 * Request that the source resource be saved as the target resource and wait
	//	 * until the task is complete. This method submits a task to the task manager
	//	 * and waits for the task to be completed.
	//	 *
	//	 * @param source The source resource
	//	 * @param target The target resource
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 * @implNote This method makes calls to the FX platform.
	//	 */
	//	public void saveAsResourceAndWait( Resource source, Resource target ) throws ExecutionException, InterruptedException {
	//		save( source, target, true, false );
	//	}

	/**
	 * Request that the source resource be saved as a copy to the target resource. This method submits a task to the task manager and returns immediately.
	 *
	 * @param source The source resource
	 * @param target The target resource
	 * @implNote This method makes calls to the FX platform.
	 */
	public void copyAsResource( Resource source, Resource target ) {
		save( source, target, false, true );
	}

	//	/**
	//	 * Request that the source resource be saved as a copy to the target resource
	//	 * and wait until the task is complete. This method submits a task to the task
	//	 * manager and waits for the task to be completed.
	//	 *
	//	 * @param source The source resource
	//	 * @param target The target resource
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 * @implNote This method makes calls to the FX platform.
	//	 */
	//	public void saveCopyAsResourceAndWait( Resource source, Resource target ) throws ExecutionException, InterruptedException {
	//		save( source, target, false, true );
	//	}

	/**
	 * Save the resource, prompting the user if necessary.
	 *
	 * @param resource The resource to be saved
	 * @param saveAsResource The resource to save as
	 * @param saveAs The save as flag
	 * @param copy The copy as flag
	 * @implNote This method makes calls to the FX platform.
	 */
	private void save( Resource resource, Resource saveAsResource, boolean saveAs, boolean copy ) {
		URI uri = resource.getUri();
		Codec codec = resource.getCodec();

		if( uri == null || (saveAs && saveAsResource == null) ) {
			//			ProgramConfigurationBuilder settings = program.getSettings().getNode( ProgramSettingsPath.RESOURCE_MANAGER );
			//			String currentDirectory = settings.get( CURRENT_DIRECTORY_SETTING_KEY, System.getProperty( "user.dir" ) );
			//
			//			JFileChooser chooser = new JFileChooser();
			//			chooser.setMultiSelectionEnabled( true );
			//			chooser.setCurrentDirectory( new File( currentDirectory ) );
			//			chooser.setAcceptAllFileFilterUsed( false );

			Codec resourceCodec = resource.getCodec();

			// If there is not a codec associated with the resource choose the default.
			if( resourceCodec == null ) resourceCodec = resource.getType().getDefaultCodec();

			//			// Add supported resource types.
			//			FileFilter selectedFilter = null;
			//			for( String pattern : registeredFileNames.keySet() ) {
			//				Set<Codec> registration = registeredFileNames.get( pattern );
			//				for( Codec option : registration ) {
			//					if( !option.canSave() ) continue;
			//					FileFilter filter = new FoldersAndCodecFilter( option );
			//					chooser.addChoosableFileFilter( filter );
			//					if( option.equals( resourceCodec ) ) selectedFilter = filter;
			//				}
			//			}
			//
			//			if( selectedFilter == null ) {
			//				program.error( Bundles.getString( BundleKey.MESSAGES, "resource.save.no.codec" ) );
			//				return false;
			//			}
			//			chooser.setFileFilter( selectedFilter );
			//
			//			File file = null;
			//			int result = JOptionPane.YES_OPTION;
			//			do {
			//				int choice = chooser.showSaveDialog( program.getActiveFrame() );
			//				if( choice == JFileChooser.CANCEL_OPTION ) return false;
			//
			//				CodecFileFilter filter = (CodecFileFilter)chooser.getFileFilter();
			//
			//				file = chooser.getSelectedFile();
			//				codec = filter.getCodec();
			//
			//				// If the file is not already supported use the default extension from the codec.
			//				if( !file.exists() && !codec.isSupportedFileName( file.getName() ) ) {
			//					file = new File( file.getParent(), file.getName() + "." + codec.getDefaultExtension() );
			//				}
			//
			//				// If the file already exists verify with the user.
			//				if( file.exists() ) {
			//					String title = Bundles.getString( BundleKey.LABELS, "file.already.exists" );
			//					String message = MessageFormat.format( Bundles.getString( BundleKey.MESSAGES, "file.already.exists" ), file );
			//					result = program.notify( title, message, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
			//					if( result == JOptionPane.CANCEL_OPTION ) return false;
			//				}
			//			} while( file.exists() && result != JOptionPane.YES_OPTION );
			//
			//			uri = file.toURI();
			//
			//			// TODO Set the settings for the resource
			//			resource.loadSettings( program.getSettingsManager().getResourceSettings( this ) );
			//
			//			if( saveAs ) {
			//				saveAsResource = new Resource( resource.getType(), uri );
			//				saveAsResource.setCodec( codec );
			//				saveAsResource.open();
			//			} else {
			//				resource.setUri( uri );
			//				resource.setCodec( codec );
			//			}
			//
			//			File parent = file.isFile() ? file.getParentFile() : file;
			//			settings.put( CURRENT_DIRECTORY_SETTING_KEY, parent.toString() );
		}

		if( saveAsResource != null ) {
			if( copy ) {
				saveAsResource.copyFrom( resource );
				resource = saveAsResource;
			} else {
				resource.setUri( saveAsResource.getUri() );
				resource.setCodec( saveAsResource.getCodec() );
			}
		}

		saveResources( resource );
	}

	/**
	 * Close the resource, prompting the user if necessary.
	 *
	 * @param resource The resource to be closed
	 * @implNote This method makes calls to the FX platform.
	 */
	public void close( Resource resource ) {
		if( resource.isModified() && canSaveResource( resource ) ) {
			Alert dialog = new Alert( Alert.AlertType.CONFIRMATION );
			dialog.initOwner( program.getWorkspaceManager().getActiveWorkspace().getStage() );
			dialog.setTitle( program.getResourceBundle().getString( "resource", "close-save-title" ) );
			dialog.setHeaderText( program.getResourceBundle().getString( "workarea", "close-save-message" ) );
			dialog.setContentText( program.getResourceBundle().getString( "resource", "close-save-prompt" ) );
			dialog.getButtonTypes().addAll( ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );

			Optional<ButtonType> choice = dialog.showAndWait();

			if( choice.isPresent() && choice.get() != ButtonType.YES ) return;
		}

		save( resource, null, false, false );
	}

	/**
	 * Create a resource from a string. This resource is considered to be an old resource. See {@link Resource#isNew()}
	 *
	 * @param string A resource string
	 * @return A new resource based on the specified string.
	 */
	public Resource createResource( String string ) {
		if( string == null ) return null;

		URI uri = UriUtil.resolve( string );

		if( uri == null ) {
			log.warn( "Cannot resolve resource URI: {}", string );
			return null;
		}

		return createResource( uri );
	}

	/**
	 * Create a resource from a URI. This resource is considered to be an old resource. See {@link Resource#isNew()}
	 *
	 * @param uri The URI to create a resource from
	 * @return The resource created from the URI
	 */
	public Resource createResource( URI uri ) {
		return doCreateResource( null, uri );
	}

	/**
	 * Create a resource from a file. This resource is considered to be an old resource. See {@link Resource#isNew()}
	 *
	 * @param file The file to create a resource from
	 * @return The resource created from the file
	 */
	public Resource createResource( File file ) {
		return doCreateResource( null, file.toURI() );
	}

	/**
	 * Create a resource from a resource type. This resource is considered to be a new resource. See {@link Resource#isNew()}
	 *
	 * @param type The resource type to create a resource from
	 * @return The resource created from the resource type
	 */
	public Resource createResource( ResourceType type ) {
		return doCreateResource( type, null );
	}

	/**
	 * Create resources from an array of descriptors. Descriptors are preferred in the following order: URI, File, String, Object
	 *
	 * @param descriptors The descriptors from which to create resources
	 * @return The list of resources created from the descriptors
	 */
	public List<Resource> createResources( Object... descriptors ) {
		return createResources( Arrays.asList( descriptors ) );
	}

	/**
	 * Create resources from a collection of descriptors. Descriptors are preferred in the following order: URI, File, String, Object
	 *
	 * @param descriptors The descriptors from which to create resources
	 * @return The list of resources created from the descriptors
	 */
	public List<Resource> createResources( Collection<? extends Object> descriptors ) {
		List<Resource> resources = new ArrayList<>( descriptors.size() );

		for( Object descriptor : descriptors ) {
			if( descriptor instanceof URI ) {
				resources.add( createResource( (URI)descriptor ) );
			} else if( descriptor instanceof File ) {
				resources.add( createResource( ((File)descriptor).toURI() ) );
			} else {
				resources.add( createResource( descriptor.toString() ) );
			}
		}

		return resources;
	}

	/**
	 * Request that the specified resources be opened. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resource The resource to open
	 */
	public void openResources( Resource resource ) throws ResourceException {
		openResources( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be opened. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resources The resources to open
	 */
	public void openResources( Collection<Resource> resources ) throws ResourceException {
		program.getExecutor().submit( new OpenResourceTask( removeOpenResources( resources ) ) );
	}

	/**
	 * Request that the specified resources be opened and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resource The resource to open
	 * @throws ExecutionException If there was an exception opening the resource
	 * @throws InterruptedException If the process of opening the resource was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void openResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
		openResourcesAndWait( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be opened and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resources The resources to open
	 * @throws ExecutionException If there was an exception opening a resource
	 * @throws InterruptedException If the process of opening a resource was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void openResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
		program.getExecutor().submit( new OpenResourceTask( removeOpenResources( resources ) ) ).get();
	}

	/**
	 * Request that the specified resources be loaded. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resource The resource to load
	 */
	public void loadResources( Resource resource ) {
		loadResources( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be loaded. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resources The resources to load
	 */
	public void loadResources( Collection<Resource> resources ) {
		program.getExecutor().submit( new LoadResourceTask( resources ) );
	}

	/**
	 * Request that the specified resources be loaded and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resource The resource to load
	 * @throws ExecutionException If there was an exception loading the resource
	 * @throws InterruptedException If the process of loading the resource was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void loadResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
		loadResourcesAndWait( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be loaded and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resources The resources to load
	 * @throws ExecutionException If there was an exception loading the resources
	 * @throws InterruptedException If the process of loading the resources was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void loadResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
		program.getExecutor().submit( new LoadResourceTask( resources ) ).get();
	}

	/**
	 * Request that the specified resources be saved. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resource The resource to save
	 */
	public void saveResources( Resource resource ) {
		saveResources( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be saved. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resources The resources to save
	 */
	public void saveResources( Collection<Resource> resources ) {
		program.getExecutor().submit( new SaveResourceTask( resources ) );
	}

	/**
	 * Request that the specified resources be saved and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resource The resource to save
	 * @throws ExecutionException If there was an exception saving the resource
	 * @throws InterruptedException If the process of saving the resource was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void saveResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
		saveResourcesAndWait( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be saved and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resources The resources to save
	 * @throws ExecutionException If there was an exception saving the resources
	 * @throws InterruptedException If the process of saving the resources was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void saveResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
		program.getExecutor().submit( new SaveResourceTask( resources ) ).get();
	}

	/**
	 * Request that the specified resources be closed. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resource The resource to close.
	 */
	public void closeResources( Resource resource ) {
		closeResources( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be closed. This method submits a task to the task manager and returns immediately.
	 *
	 * @param resources The resources to close.
	 */
	public void closeResources( Collection<Resource> resources ) {
		program.getExecutor().submit( new CloseResourceTask( resources ) );
	}

	/**
	 * Request that the specified resources be closed and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resource The resources to close.
	 * @throws ExecutionException If there was an exception closing the resource
	 * @throws InterruptedException If the process of closing the resource was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void closeResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
		closeResourcesAndWait( Collections.singletonList( resource ) );
	}

	/**
	 * Request that the specified resources be closed and wait until the task is complete. This method submits a task to the task manager and waits for the task to be completed.
	 *
	 * @param resources The resources to close.
	 * @throws ExecutionException If there was an exception closing the resources
	 * @throws InterruptedException If the process of closing the resources was interrupted
	 * @implNote Do not call from a UI thread
	 */
	public void closeResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
		program.getExecutor().submit( new CloseResourceTask( resources ) ).get();
	}

	/**
	 * Get a collection of the supported codecs.
	 *
	 * @return
	 */
	public Collection<Codec> getCodecs() {
		Set<Codec> codecs = new HashSet<>();

		for( ResourceType type : resourceTypes.values() ) {
			codecs.addAll( type.getCodecs() );
		}

		return Collections.unmodifiableCollection( codecs );
	}

	/**
	 * Determine the resource type for the given resource. The resource URI is used to find the resource type in the following order: <ol> <li>Lookup the resource type by the full URI</li> <li>Lookup the resource type by the URI scheme</li>
	 * <li>Find all the codecs that match the URI</li> <li>Sort the codecs by priority, select the highest</li> <li>Use the resource type associated to the codec</li> </ol>
	 *
	 * @param resource The resource for which to resolve the resource type
	 * @return
	 */
	ResourceType autoDetectResourceType( Resource resource ) {
		URI uri = resource.getUri();
		ResourceType type = null;

		// Look for resource type assigned to specific URIs.
		if( uri != null ) type = uriResourceTypes.get( uri.toString() );

		// Look for resource types assigned to specific schemes.
		if( type == null && uri != null ) type = schemeResourceTypes.get( uri.getScheme() );

		// Look for resource types assigned to specific codecs.
		List<Codec> codecs = new ArrayList<>( autoDetectCodecs( resource ) );
		codecs.sort( new CodecPriorityComparator().reversed() );
		Codec codec = codecs.size() == 0 ? null : codecs.get( 0 );
		if( type == null && codec != null ) type = codec.getResourceType();

		if( codec != null ) resource.setCodec( codec );
		if( type != null ) resource.setType( type );

		return type;
	}

	/**
	 * Determine the codec for the given resource by checking the file name, the first line, and the content type for a match with a supported resource type. When calling this method the resource needs to already be open so that the
	 * information needed to determine the correct codec is defined in the resource.
	 * <p>
	 * Note: This method uses a URLConnection object to get the first line and content type of the resource. This means that the calling thread will be blocked during the IO operations used in URLConnection if the first line or the content
	 * type is needed to determine the resource type.
	 *
	 * @param resource The resource for which to find codecs
	 * @return The set of codecs that match the resource
	 */
	Set<Codec> autoDetectCodecs( Resource resource ) {
		Set<Codec> codecs = new HashSet<>();
		Collection<ResourceType> resourceTypes = getResourceTypes();

		// First option: Determine codec by media type.
		String mediaType = getMediaType( resource );
		if( mediaType != null ) {
			for( ResourceType resourceType : resourceTypes ) {
				Codec codec = resourceType.getCodecByMediaType( mediaType );
				if( codec != null ) codecs.add( codec );
			}
		}

		// Second option: Determine codec by file name.
		String fileName = resource.getFileName();
		if( fileName != null ) {
			for( ResourceType resourceType : resourceTypes ) {
				Codec codec = resourceType.getCodecByFileName( fileName );
				if( codec != null ) codecs.add( codec );
			}
		}

		// Third option: Determine codec by first line.
		// Load the first line from the resource.
		String firstLine = getFirstLine( resource );
		if( firstLine != null ) {
			for( ResourceType resourceType : resourceTypes ) {
				Codec codec = resourceType.getCodecByFirstLine( firstLine );
				if( codec != null ) codecs.add( codec );
			}
		}

		return codecs;
	}

	private Collection<Resource> removeOpenResources( Collection<Resource> resources ) {
		Collection<Resource> filteredResources = new ArrayList<>( resources );
		for( Resource resource : openResources ) {
			filteredResources.remove( resource );
		}
		return filteredResources;
	}

	private boolean isResourceOpen( Resource resource ) {
		return openResources.contains( resource );
	}

	private void updateActionState() {
		newActionHandler.setEnabled( getUserResourceTypes().size() > 0 );
		openActionHandler.setEnabled( getUserResourceTypes().size() > 0 );
		// TODO Should open resources and modified resources be scoped to the workarea?
		saveAllActionHandler.setEnabled( getModifiedResources().size() > 0 );
		closeAllActionHandler.setEnabled( openResources.size() > 0 );
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
	 * Determine if the resource can be saved. The resource can be saved if the URI is null or if the URI scheme and codec can both save resources.
	 *
	 * @param resource The resource to check
	 * @return True if the resource can be saved, false otherwise.
	 */
	private boolean canSaveResource( Resource resource ) {
		// Check the URI.
		URI uri = resource.getUri();
		if( uri == null ) return true;

		// Check supported schemes.
		Scheme scheme = getScheme( uri.getScheme() );
		if( scheme == null ) return false;

		boolean result = false;
		try {
			Codec codec = resource.getCodec();
			result = scheme.canSave( resource ) && (codec == null || codec.canSave());
		} catch( ResourceException exception ) {
			log.error( "Error checking if resource can be saved", exception );
		}

		return result;
	}

	/**
	 * Create a resource from a resource type and/or a URI. The resource is considered to be a new resource if the URI is null. Otherwise, the resource is considered an old resource. See {@link Resource#isNew()}
	 *
	 * @param type The resource type of the resource
	 * @param uri The URI of the resource
	 * @return The resource created from the resource type and URI
	 */
	// FIXME Should throw ResourceException
	private Resource doCreateResource( ResourceType type, URI uri ) {
		Resource resource = new Resource( type, uri );

		if( uri != null ) {
			// If the resource is already open, use it instead
			for( Resource open : openResources ) {
				if( open.getUri().equals( uri ) ) return open;
			}

			try {
				Scheme scheme = getScheme( uri.getScheme() );
				resource.setScheme( scheme );
				scheme.init( resource );
			} catch( ResourceException exception ) {
				log.error( "Error initializing resource scheme", exception );
			}
		}

		return resource;
	}

	private boolean doOpenResource( Resource resource ) throws ResourceException {
		if( isResourceOpen( resource ) ) return true;

		// Determine the resource type.
		ResourceType type = resource.getType();
		if( type == null ) type = autoDetectResourceType( resource );
		if( type == null ) throw new ResourceException( resource, "Resource type could not be determined: " + resource );
		log.trace( "Resource type: " + type );

		// Determine the codec.
		Codec codec = resource.getCodec();
		if( codec == null ) {
			codec = resource.getType().getDefaultCodec();
			resource.setCodec( codec );
		}
		log.trace( "Resource codec: " + codec );

		// Create the resource settings
		createResourceSettings( resource );
		log.trace( "Resource settings: " + resource.getSettings().getPath() );

		// Initialize the resource.
		if( !type.resourceDefault( program, resource ) ) return false;
		log.trace( "Resource initialized with default values." );

		// If the resource is new get user input from the resource type.
		if( resource.isNew() ) {
			if( !type.resourceDialog( program, resource ) ) return false;
			log.trace( "Resource initialized with user values." );
		}

		// Open the resource.
		resource.open();

		// Add the resource to the list of open resources.
		openResources.add( resource );

		log.trace( "Resource opened: " + resource );

		program.fireEvent( new ResourceOpenedEvent( getClass(), resource ) );

		return true;
	}

	private boolean doLoadResource( Resource resource ) throws ResourceException {
		if( resource == null ) return false;

		if( !resource.isOpen() ) doOpenResource( resource );

		if( !resource.exists() ) return false;

		// Load the resource.
		boolean previouslyLoaded = resource.isLoaded();
		resource.load( this );
		resource.setModified( false );
		if( !previouslyLoaded ) resource.addNodeListener( modifiedResourceWatcher );
		if( resource.isReady() ) resource.refresh( this );

		program.fireEvent( new ResourceLoadedEvent( getClass(), resource ) );
		log.trace( "Resource refresh: " + resource );

		return true;
	}

	private boolean doSaveResource( Resource resource ) throws ResourceException {
		if( resource == null ) return false;
		if( !isResourceOpen( resource ) ) return false;

		resource.save( this );

		// Create the resource settings
		createResourceSettings( resource );

		// Note: The resource watcher will log that the resource was unmodified.
		resource.setModified( false );

		// TODO Update the resource type.

		log.trace( "Resource saved: " + resource );

		program.fireEvent( new ResourceSavedEvent( getClass(), resource ) );

		return true;
	}

	private boolean doCloseResource( Resource resource ) throws ResourceException {
		if( resource == null ) return false;
		if( !isResourceOpen( resource ) ) return false;

		resource.close( this );
		openResources.remove( resource );
		resource.removeNodeListener( modifiedResourceWatcher );

		if( openResources.size() == 0 ) doSetCurrentResource( null );

		// TODO Delete the resource settings?
		// Should the settings be removed? Or left for later?
		//		Settings settings = resource.getSettings();
		//		if( settings != null ) settings.delete();

		log.trace( "Resource closed: " + resource );

		program.fireEvent( new ResourceClosedEvent( getClass(), resource ) );

		return true;
	}

	// TODO Finish implementing ResourceManager.doSetCurrentResource()

	private boolean doSetCurrentResource( Resource resource ) {
		synchronized( currentResourceLock ) {
			Resource previous = currentResource;

			// "Disconnect" the old current resource.
			if( currentResource != null ) currentResource.removeResourceListener( currentResourceWatcher );

			// Change current resource.
			currentResource = resource;

			// "Connect" the new current resource.
			if( currentResource == null ) {
				saveActionHandler.setEnabled( false );
				saveAsActionHandler.setEnabled( false );
				saveCopyAsActionHandler.setEnabled( false );
				closeActionHandler.setEnabled( false );
			} else {
				boolean canSave = canSaveResource( resource );
				saveActionHandler.setEnabled( currentResource.isModified() && canSave );
				saveAsActionHandler.setEnabled( canSave );
				saveCopyAsActionHandler.setEnabled( canSave );
				closeActionHandler.setEnabled( true );
				currentResource.addResourceListener( currentResourceWatcher );
			}

			updateActionState();

			log.trace( "Resource select: " + (resource == null ? "null" : resource) );

			// Notify program of current resource change.
			program.fireEvent( new CurrentResourceChangedEvent( getClass(), previous, currentResource ) );
		}

		return true;
	}

	private void createResourceSettings( Resource resource ) {
		URI uri = resource.getUri();
		if( uri == null ) return;
		resource.setSettings( program.getSettingsManager().getSettings( ProgramSettings.RESOURCE, IdGenerator.getId( uri.toString() ) ) );
	}

	//	/**
	//	 * @param resource
	//	 * @return
	//	 * @deprecated Instead use Scheme.getConnection( Resource )
	//	 */
	//	@Deprecated
	//	private URLConnection getConnection( Resource resource ) {
	//		URI uri = resource.getUri();
	//		Scheme scheme = getScheme( uri.getScheme() );
	//		if( scheme == null ) return null;
	//
	//		try {
	//			// FIXME Should not convert to URL to get a connection
	//			//return uri.toURL().openConnection();
	//
	//			// It should come from the scheme
	//			//return scheme.openConnection( resource );
	//		} catch( Exception exception ) {
	//			log.warn( "Error opening resource connection", resource );
	//			log.warn( "Error opening resource connection", exception );
	//		}
	//
	//		return null;
	//	}

	private String getMediaType( Resource resource ) {
		String mediaType = resource.getResource( Resource.MEDIA_TYPE_RESOURCE_KEY );

		if( mediaType == null ) {
			URLConnection connection = resource.getScheme().getConnection( resource );
			if( connection != null ) {
				try {
					mediaType = StringUtils.trimToNull( connection.getContentType() );
					resource.putResource( Resource.MEDIA_TYPE_RESOURCE_KEY, mediaType );
					connection.getInputStream().close();
				} catch( IOException exception ) {
					log.warn( "Error closing resource connection", exception );
				}
			}
		}

		return mediaType;
	}

	private String getFirstLine( Resource resource ) {
		// Load the first line from the resource.
		String firstLine = null;

		URLConnection connection = resource.getScheme().getConnection( resource );
		if( connection != null ) {
			try {
				String encoding = resource.getEncoding();
				if( encoding == null ) encoding = connection.getContentEncoding();
				firstLine = readFirstLine( connection.getInputStream(), encoding );
				connection.getInputStream().close();
			} catch( IOException exception ) {
				log.warn( "Error closing resource connection", exception );
			}
		}

		return firstLine;
	}

	private String readFirstLine( InputStream input, String encoding ) throws IOException {
		if( input == null ) return null;

		byte[] buffer = new byte[ FIRST_LINE_LIMIT ];
		BoundedInputStream boundedInput = new BoundedInputStream( input, FIRST_LINE_LIMIT );
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
		return StringUtils.trimToNull( new String( output.toByteArray(), encoding ) );
	}

	// TODO The OpenActionTask class name is not the best...
	// but there are other classes with the expected name
	private class OpenActionTask extends Task<Void> {

		private Collection<Resource> resources;

		private Codec codec;

		private WorkpaneView view;

		private boolean openTool;

		private boolean setActive;

		private OpenActionTask( Collection<Resource> resources, Codec codec, WorkpaneView view, boolean openTool, boolean setActive ) {
			this.resources = resources;
			this.codec = codec;
			this.view = view;
			this.openTool = openTool;
			this.setActive = setActive;
		}

		@Override
		public Void call() throws Exception {
			for( Resource resource : resources ) {
				log.trace( "Open resource: ", resource.getUri() );

				boolean openTool = this.openTool || !isResourceOpen( resource );

				try {
					if( codec != null ) resource.setCodec( codec );

					// Open the resource
					openResourcesAndWait( resource );
					if( !resource.isOpen() ) continue;

					// Start loading the resource, but don't wait
					if( !resource.isLoaded() ) loadResources( resource );
				} catch( Exception exception ) {
					program.getNotifier().error( exception );
					continue;
				}

				if( openTool ) program.getToolManager().openTool( resource, view, setActive );
				setCurrentResource( resource );
			}

			return null;
		}

	}

	private class NewActionHandler extends Action {

		private NewActionHandler( Program program ) {
			super( program );
		}

		@Override
		public void handle( Event event ) {
			Collection<ResourceType> types = getResourceTypes();

			ResourceType type = null;
			if( types.size() == 1 ) {
				type = types.iterator().next();
			} else {
				// TODO Re-enable ResourceManager.NewActionHandler.handle()
				//					String title = program.getResourceBundle().getString( BundleKey.LABELS, "new" );
				//					ResourceTypePanel panel = new ResourceTypePanel( ResourceManager.this );
				//
				//					int result = program.notify( title, panel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
				//
				//					if( result == JOptionPane.OK_OPTION ) type = panel.getResourceType();
			}

			program.getExecutor().submit( new LoadResource( type ) );
		}

		private class LoadResource extends Task<Resource> {

			private ResourceType type;

			private LoadResource( ResourceType type ) {
				this.type = type;
			}

			@Override
			public Resource call() throws Exception {
				if( type == null ) return null;

				Resource resource = null;
				// TODO Re-enable ResourceManager.LoadResource.call()
				//					try {
				//						resource = createResource( type );
				//						openResourcesAndWait( resource );
				//						resource = findOpenResource( resource );
				//						if( !resource.isOpen() ) return null;
				//					} catch( Exception exception ) {
				//						program.error( exception );
				//						return null;
				//					}
				//					resource.setModified( true );
				//
				//					if( !resource.isLoaded() ) loadResourcesAndWait( resource );
				//					createResourceEditor( resource, null );
				//					setCurrentResource( resource );

				return resource;
			}

		}

	}

	private class OpenActionHandler extends Action {

		private OpenActionHandler( Program program ) {
			super( program );
		}

		@Override
		public void handle( Event event ) {
			// TODO Open a file dialog for the user to pick a file
			// In Escape this opened a separate tool. Do I want to do the same?

			// Disable the action while the dialog is open
			setEnabled( false );
		}

		// This class is the action listener of the resource tool used to open files
		//		private class ResourceToolOpenHandler implements ActionListener {
		//
		//			public void actionPerformed( ActionEvent event ) {
		//				tool.removeActionListener( this );
		//				updateActionState();
		//
		//				if( event.getActionCommand() == ResourceTool.CANCEL_SELECTION ) return;
		//
		//				// Open the selected resources.
		//				Codec codec = tool.getSelectedCodec();
		//				List<Resource> resources = Arrays.asList( tool.getSelectedResources() );
		//				program.getTaskManager().submit( new OpenActionTask( resources, codec, program.getActiveWorkPane().getActiveView(), true ) );
		//			}
		//
		//		}
		//
	}

	private class SaveActionHandler extends Action {

		private Resource resource;

		private boolean saveAs;

		private boolean copy;

		private SaveActionHandler( Program program, boolean saveAs, boolean copy ) {
			super( program );
			this.saveAs = saveAs;
			this.copy = copy;
		}

		@Override
		public void handle( Event event ) {
			resource = getCurrentResource();

			if( saveAs ) {
				//				// Ask the user for the new resource location.
				//				tool = (ResourceTool)program.getToolManager().getWorkTool( ResourceTool.class );
				//				if( tool == null ) return;
				//
				//				tool.addActionListener( new ResourceToolSaveHandler() );
				//				program.getActiveWorkPane().addTool( tool, true );
			} else {
				saveResources( resource );
			}
		}

		//		private class ResourceToolSaveHandler implements ActionListener {
		//
		//			@Override
		//			public void actionPerformed( ActionEvent event ) {
		//				tool.removeActionListener( this );
		//
		//				if( event.getActionCommand() == ResourceTool.CANCEL_SELECTION ) return;
		//
		//				// If the user specified a codec use it.
		//				Codec codec = null;
		//				ResourceType type = null;
		//
		//				// Set the codec and resource type.
		//				codec = tool.getSelectedCodec();
		//				if( codec != null ) type = codec.getResourceType();
		//
		//				// Resolve the URI.
		//				URI uri = UriUtil.resolve( tool.getResourcePath() );
		//
		//				// Create the target resource.
		//				Resource target = createResource( type, uri );
		//
		//				if( copy ) {
		//					saveCopyAsResource( resource, target );
		//				} else {
		//					saveAsResource( resource, target );
		//				}
		//			}
		//		}

	}

	private class SaveAllActionHandler extends Action {

		private SaveAllActionHandler( Program program ) {
			super( program );
		}

		@Override
		public void handle( Event event ) {
			try {
				saveResources( getModifiedResources() );
			} catch( Exception exception ) {
				program.getNotifier().error( exception );
			}
		}

	}

	private class CloseActionHandler extends Action {

		private CloseActionHandler( Program program ) {
			super( program );
		}

		@Override
		public void handle( Event event ) {
			try {
				closeResources( getCurrentResource() );
			} catch( Exception exception ) {
				program.getNotifier().error( exception );
			}
		}

	}

	private class CloseAllActionHandler extends Action {

		private CloseAllActionHandler( Program program ) {
			super( program );
		}

		@Override
		public void handle( Event event ) {
			try {
				closeResources( openResources );
			} catch( Exception exception ) {
				program.getNotifier().error( exception );
			}
		}

	}

	private abstract class ResourceTask extends ProgramTask<Collection<Resource>> {

		private Collection<Resource> resources;

		private ResourceTask( Resource resource ) {
			super( program );
			this.resources = Collections.singletonList( resource );
		}

		private ResourceTask( Resource... resources ) {
			super( program );
			this.resources = Arrays.asList( resources );
		}

		private ResourceTask( Collection<Resource> resources ) {
			super( program );
			this.resources = resources;
		}

		@Override
		public Collection<Resource> call() throws Exception {
			List<Resource> result = new ArrayList<Resource>();
			List<Throwable> errors = new ArrayList<Throwable>();
			if( resources != null ) {
				for( Resource resource : resources ) {
					try {
						if( doOperation( resource ) ) result.add( resource );
					} catch( Throwable throwable ) {
						log.warn( "Error executing resource task", throwable );
						errors.add( throwable );
					}
				}
			}

			if( errors.size() != 0 ) {
				StringBuilder messages = new StringBuilder();
				for( Throwable error : errors ) {
					messages.append( error.getClass().getSimpleName() );
					messages.append( ": " );
					messages.append( error.getMessage() );
					messages.append( "\n" );
				}

				String title = program.getResourceBundle().getString( "resource", "resources" );
				String message = program.getResourceBundle().getString( "resource", "resource.exception", messages.toString() );
				program.getNotifier().warning( title, (Object)message );
				log.warn( "Error executing resource task", message );
			}

			return result;
		}

		abstract boolean doOperation( Resource resource ) throws ResourceException;

		@Override
		public String toString() {
			if( resources.size() == 0 ) return super.toString() + ": none";
			return super.toString() + ": " + resources.iterator().next().toString();
		}

	}

	private class OpenResourceTask extends ResourceTask {

		private OpenResourceTask( Collection<Resource> resources ) {
			super( resources );
		}

		@Override
		public boolean doOperation( Resource resource ) throws ResourceException {
			return doOpenResource( resource );
		}

	}

	private class LoadResourceTask extends ResourceTask {

		private LoadResourceTask( Resource... resources ) {
			super( resources );
		}

		private LoadResourceTask( Collection<Resource> resources ) {
			super( resources );
		}

		@Override
		public boolean doOperation( Resource resource ) throws ResourceException {
			return doLoadResource( resource );
		}

	}

	private class SaveResourceTask extends ResourceTask {

		private SaveResourceTask( Resource resource ) {
			super( resource );
		}

		private SaveResourceTask( Resource... resources ) {
			super( resources );
		}

		private SaveResourceTask( Collection<Resource> resources ) {
			super( resources );
		}

		@Override
		public boolean doOperation( Resource resource ) throws ResourceException {
			return doSaveResource( resource );
		}

	}

	private class CloseResourceTask extends ResourceTask {

		private CloseResourceTask( Collection<Resource> resources ) {
			super( resources );
		}

		@Override
		public boolean doOperation( Resource resource ) throws ResourceException {
			return doCloseResource( resource );
		}

	}

	private class SetCurrentResourceTask extends ResourceTask {

		private SetCurrentResourceTask( Resource resource ) {
			super( resource );
		}

		@Override
		public boolean doOperation( Resource resource ) throws ResourceException {
			return doSetCurrentResource( resource );
		}

	}

	private class CurrentResourceWatcher implements ResourceListener {

		@Override
		public void eventOccurred( ResourceEvent event ) {
			switch( event.getType() ) {
				case MODIFIED: {
					Resource resource = event.getResource();
					log.trace( "Resource modified: " + resource );
					saveActionHandler.setEnabled( canSaveResource( resource ) );
					break;
				}
				case UNMODIFIED: {
					Resource resource = event.getResource();
					saveActionHandler.setEnabled( false );
					log.trace( "Resource unmodified: " + resource );
					break;
				}
			}

		}

	}

	private class ModifiedResourceWatcher implements NodeListener {

		@Override
		public void nodeEvent( NodeEvent event ) {
			NodeEvent.Type type = event.getType();
			switch( type ) {
				case FLAG_CHANGED: {
					updateActionState();
					log.debug( "Data flag changed: " + event.getSource() + ": " + event.getKey() + ": " + event.getNewValue() );
					break;
				}
				case VALUE_CHANGED: {
					log.debug( "Data value changed: " + event.getSource() + ": " + event.getKey() + ": " + event.getNewValue() );
					break;
				}
				case CHILD_ADDED: {
					log.debug( "Data child added: " + event.getChild() );
					break;
				}
				case CHILD_REMOVED: {
					log.debug( "Data child removed: " + event.getChild() );
					break;
				}
			}
		}

	}

}
