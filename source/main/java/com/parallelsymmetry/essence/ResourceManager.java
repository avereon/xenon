package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.*;
import com.parallelsymmetry.essence.resource.event.ResourceOpenedEvent;
import com.parallelsymmetry.essence.scheme.Schemes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ResourceManager {

	public static final String CURRENT_DIRECTORY_SETTING_KEY = "current.folder";

	// Linux defines this limit in BINPRM_BUF_SIZE
	private static final int FIRST_LINE_LIMIT = 128;

	private static Logger log = LoggerFactory.getLogger( ResourceManager.class );

	private Program program;

	private volatile Resource currentResource;

	private Set<Resource> openResources;

	private Map<String, ResourceType> resourceTypes;

	private Map<String, Set<Codec>> registeredFileNames;

	private Map<String, Set<Codec>> registeredFirstLines;

	private Map<String, Set<Codec>> registeredMediaTypes;

	//	private NewActionHandler newActionHandler = new NewActionHandler();
	//
	//	private OpenActionHandler openActionHandler = new OpenActionHandler();
	//
	//	private SaveActionHandler saveActionHandler = new SaveActionHandler( false, false );
	//
	//	private SaveActionHandler saveAsActionHandler = new SaveActionHandler( true, false );
	//
	//	private SaveActionHandler saveCopyAsActionHandler = new SaveActionHandler( true, true );
	//
	//	private SaveAllActionHandler saveAllActionHandler = new SaveAllActionHandler();
	//
	//	private CloseActionHandler closeActionHandler = new CloseActionHandler();
	//
	//	private CloseAllActionHandler closeAllActionHandler = new CloseAllActionHandler();
	//
	//	private CurrentResourceWatcher currentResourceWatcher = new CurrentResourceWatcher();

	private Object restoreLock = new Object();

	private Object currentResourceLock = new Object();

	public ResourceManager( Program program ) {
		this.program = program;
		openResources = new CopyOnWriteArraySet<>();
		resourceTypes = new ConcurrentHashMap<>();
		registeredFileNames = new ConcurrentHashMap<>();
		registeredFirstLines = new ConcurrentHashMap<>();
		registeredMediaTypes = new ConcurrentHashMap<>();
	}

	public void start() {
		//		((FileScheme)Schemes.getScheme( "file" )).startResourceWatching();
		//
		//		program.getActionLibrary().getAction( "new" ).pushHandler( newActionHandler );
		//		program.getActionLibrary().getAction( "open" ).pushHandler( openActionHandler );
		//		program.getActionLibrary().getAction( "save" ).pushHandler( saveActionHandler );
		//		program.getActionLibrary().getAction( "save.as" ).pushHandler( saveAsActionHandler );
		//		program.getActionLibrary().getAction( "save.copy.as" ).pushHandler( saveCopyAsActionHandler );
		//		program.getActionLibrary().getAction( "save.all" ).pushHandler( saveAllActionHandler );
		//		program.getActionLibrary().getAction( "close" ).pushHandler( closeActionHandler );
		//		program.getActionLibrary().getAction( "close.all" ).pushHandler( closeAllActionHandler );
		//
		//		updateActionState();
	}

	public boolean isRunning() {
		// TODO Return a real value for ResourceManager.isRunning()
		return false;
	}

	//	public void stop() {
	//		((FileScheme)Schemes.getScheme( "file" )).stopResourceWatching();
	//	}

	public Resource getCurrentResource() {
		return currentResource;
	}

	//	public void setCurrentResource( Resource resource ) {
	//		program.getTaskManager().submit( new SetCurrentResourceTask( resource ) );
	//	}
	//
	//	public void setCurrentResourceAndWait( Resource resource ) throws ExecutionException, InterruptedException {
	//		program.getTaskManager().invoke( new SetCurrentResourceTask( resource ) );
	//	}
	//
	//	public List<Resource> getOpenResources() {
	//		return new ArrayList<Resource>( openResources );
	//	}
	//
	//	public List<Resource> getModifiedResources() {
	//		List<Resource> modifiedResources = new ArrayList<Resource>();
	//		for( Resource resource : getOpenResources() ) {
	//			if( resource.isModified() && canSaveResource( resource ) ) modifiedResources.add( resource );
	//		}
	//		return modifiedResources;
	//	}
	//
	//	/**
	//	 * Get the externally modified resources.
	//	 *
	//	 * @return
	//	 */
	//	public List<Resource> getExternallyModifiedResources() {
	//		List<Resource> externallyModifiedResources = new ArrayList<Resource>();
	//		for( Resource resource : getOpenResources() ) {
	//			if( resource.isExternallyModified() ) externallyModifiedResources.add( resource );
	//		}
	//		return externallyModifiedResources;
	//	}
	//
	//	/**
	//	 * Add a resource type to the set of supported resource types.
	//	 *
	//	 * @param type
	//	 */
	//	public void addResourceType( ResourceType type ) {
	//		if( type == null ) return;
	//		synchronized( resourceTypes ) {
	//			if( resourceTypes.get( type.getKey() ) != null ) throw new IllegalArgumentException( "ResourceType already exists: " + type.getKey() );
	//
	//			Set<Codec> codecs = type.getCodecs();
	//			for( Codec codec : codecs ) {
	//				// Register codec support.
	//				registerCodec( codec, codec.getSupportedFileNames(), registeredFileNames );
	//				registerCodec( codec, codec.getSupportedFirstLines(), registeredFirstLines );
	//				registerCodec( codec, codec.getSupportedContentTypes(), registeredMimeTypes );
	//			}
	//
	//			// Add the resource type to the registered resource types.
	//			resourceTypes.put( type.getKey(), type );
	//
	//			// Update the actions.
	//			updateActionState();
	//		}
	//	}
	//
	//	/**
	//	 * Remove a resource type from the supported resource types.
	//	 *
	//	 * @param type
	//	 */
	//	public void removeResourceType( ResourceType type ) {
	//		if( type == null ) return;
	//		synchronized( resourceTypes ) {
	//			if( !resourceTypes.containsKey( type.getKey() ) ) return;
	//
	//			// Remove the resource type from the registered resource types.
	//			resourceTypes.remove( type.getKey() );
	//
	//			Set<Codec> codecs = type.getCodecs();
	//			for( Codec codec : codecs ) {
	//				// Unregister codec support.
	//				unregisterCodec( codec, codec.getSupportedFileNames(), registeredFileNames );
	//				unregisterCodec( codec, codec.getSupportedFirstLines(), registeredFirstLines );
	//				unregisterCodec( codec, codec.getSupportedContentTypes(), registeredMimeTypes );
	//			}
	//
	//			// Update the actions.
	//			updateActionState();
	//		}
	//	}
	//
	//	/**
	//	 * Get a resource type by the resource type key defined in the resource type.
	//	 * This is useful for getting resource types from persisted data.
	//	 *
	//	 * @param key
	//	 * @return
	//	 */
	//	public ResourceType getResourceType( String key ) {
	//		ResourceType type = resourceTypes.get( key );
	//		if( type == null ) Log.write( Log.WARN, "Resource type not found: " + key );
	//		return type;
	//	}
	//
	//	/**
	//	 * Get a collection of the supported resource types.
	//	 *
	//	 * @return
	//	 */
	//	public Collection<ResourceType> getResourceTypes() {
	//		return Collections.unmodifiableCollection( resourceTypes.values() );
	//	}
	//
	//	/**
	//	 * Create a resource from a string.
	//	 *
	//	 * @param string
	//	 * @return A new resource based on the specified string.
	//	 */
	//	public Resource createResource( String string ) {
	//		if( string == null ) return null;
	//
	//		URI uri = UriUtil.resolve( string );
	//
	//		if( uri == null ) {
	//			Log.write( Log.WARN, "Resource not created: ", string );
	//			return null;
	//		}
	//
	//		return createResource( uri );
	//	}

	public Resource createResource( URI uri ) {
		return createResource( null, uri );
	}

	public Resource createResource( File file ) {
		return createResource( null, file.toURI() );
	}

	public Resource createResource( ResourceType type ) {
		return createResource( type, null );
	}

	public Resource createResource( ResourceType type, URI uri ) {
		Resource resource = new Resource( type, uri );

		resource = findOpenResource( resource );

		if( uri != null ) {
			try {
				resource.getScheme().init( resource );
			} catch( ResourceException exception ) {
				log.error( "Error initializing resource scheme", exception );
			}
		}

		return resource;
	}

	//	/**
	//	 * Create resources from an array of descriptors. Descriptors are preferred in
	//	 * the following order: URI, File, String, Object
	//	 *
	//	 * @param descriptors
	//	 * @return
	//	 */
	//	public List<Resource> createResources( Object... descriptors ) {
	//		return createResources( Arrays.asList( descriptors ) );
	//	}
	//
	//	/**
	//	 * Create resources from a collection of descriptors. Descriptors are
	//	 * preferred in the following order: URI, File, String, Object
	//	 *
	//	 * @param descriptors
	//	 * @return
	//	 */
	//	public List<Resource> createResources( Collection<? extends Object> descriptors ) {
	//		List<Resource> resources = new ArrayList<Resource>( descriptors.size() );
	//
	//		for( Object descriptor : descriptors ) {
	//			if( descriptor instanceof URI ) {
	//				resources.add( createResource( (URI)descriptor ) );
	//			} else if( descriptor instanceof File ) {
	//				resources.add( createResource( ((File)descriptor).toURI() ) );
	//			} else {
	//				resources.add( createResource( descriptor.toString() ) );
	//			}
	//		}
	//
	//		return resources;
	//	}
	//
	//	public void open( Resource resource ) {
	//		open( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	public void open( Resource resource, boolean createEditor ) {
	//		open( Arrays.asList( new Resource[]{ resource } ), createEditor );
	//	}
	//
	//	public void open( Collection<Resource> resources ) {
	//		open( resources, true );
	//	}
	//
	//	public void open( Collection<Resource> resources, ToolView toolview ) {
	//		open( resources, toolview, true );
	//	}
	//
	//	public void open( Collection<Resource> resources, boolean createEditors ) {
	//		open( resources, null, createEditors );
	//	}
	//
	//	public void open( Collection<Resource> resources, ToolView toolview, boolean createEditors ) {
	//		program.getTaskManager().submit( new OpenActionTask( resources, null, toolview, createEditors ) );
	//	}

	/**
	 * Request that the specified resources be opened. This method submits a task
	 * to the task manager and returns immediately.
	 *
	 * @param resource
	 */
	public void openResources( Resource resource ) throws ResourceException {
		openResources( Arrays.asList( new Resource[]{ resource } ) );
	}

	/**
	 * Request that the specified resources be opened. This method submits a task
	 * to the task manager and returns immediately.
	 *
	 * @param resources
	 */
	public void openResources( Collection<Resource> resources ) throws ResourceException {
		program.getExecutor().submit( new OpenResourceTask( removeOpenResources( resources ) ) );
	}

	//	/**
	//	 * Request that the specified resources be opened and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should not be called from the event dispatch thread.
	//	 *
	//	 * @param resource
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void openResourcesAndWait( Resource resource ) throws ResourceException, InterruptedException {
	//		openResourcesAndWait( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be opened and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should not be called from the event dispatch thread.
	//	 *
	//	 * @param resources
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void openResourcesAndWait( Collection<Resource> resources ) throws ResourceException, InterruptedException {
	//		program.getTaskManager().invoke( new OpenResourceTask( removeOpenResources( resources ) ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be loaded. This method submits a task
	//	 * to the task manager and returns immediately.
	//	 *
	//	 * @param resource
	//	 */
	//	public void loadResources( Resource resource ) {
	//		loadResources( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be loaded. This method submits a task
	//	 * to the task manager and returns immediately.
	//	 *
	//	 * @param resources
	//	 */
	//	public void loadResources( Collection<Resource> resources ) {
	//		program.getTaskManager().submit( new LoadResourceTask( resources ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be loaded and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should not be called from the event dispatch thread.
	//	 *
	//	 * @param resource
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void loadResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
	//		loadResourcesAndWait( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be loaded and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should not be called from the event dispatch thread.
	//	 *
	//	 * @param resources
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void loadResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
	//		program.getTaskManager().invoke( new LoadResourceTask( resources ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be saved. This method submits a task
	//	 * to the task manager and returns immediately.
	//	 *
	//	 * @param resource
	//	 */
	//	public void saveResources( Resource resource ) {
	//		saveResources( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be saved. This method submits a task
	//	 * to the task manager and returns immediately.
	//	 *
	//	 * @param resources
	//	 */
	//	public void saveResources( Collection<Resource> resources ) {
	//		program.getTaskManager().submit( new SaveResourceTask( resources ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be saved and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should never be called from the event dispatch thread.
	//	 *
	//	 * @param resource
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void saveResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
	//		saveResourcesAndWait( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be saved and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should never be called from the event dispatch thread.
	//	 *
	//	 * @param resources
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void saveResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
	//		program.getTaskManager().invoke( new SaveResourceTask( resources ) );
	//	}
	//
	//	/**
	//	 * Request that the source resource be saved as the target resource. This
	//	 * method submits a task to the task manager and returns immediately.
	//	 *
	//	 * @param source
	//	 * @param target
	//	 */
	//	public void saveAsResource( Resource source, Resource target ) {
	//		SaveAsResourceTask task = new SaveAsResourceTask( source, target );
	//		program.getTaskManager().submit( task );
	//	}
	//
	//	/**
	//	 * Request that the source resource be saved as the target resource and wait
	//	 * until the task is complete. This method submits a task to the task manager
	//	 * and waits for the task to be completed.
	//	 * <p>
	//	 * Note: This method should never be called from the event dispatch thread.
	//	 *
	//	 * @param source
	//	 * @param target
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void saveAsResourceAndWait( Resource source, Resource target ) throws ExecutionException, InterruptedException {
	//		SaveAsResourceTask task = new SaveAsResourceTask( source, target );
	//		program.getTaskManager().invoke( task );
	//	}
	//
	//	/**
	//	 * Request that the source resource be saved as a copy to the target resource.
	//	 * This method submits a task to the task manager and returns immediately.
	//	 *
	//	 * @param source
	//	 * @param target
	//	 */
	//	public void saveCopyAsResource( Resource source, Resource target ) {
	//		SaveCopyResourceTask task = new SaveCopyResourceTask( source, target );
	//		program.getTaskManager().submit( task );
	//	}
	//
	//	/**
	//	 * Request that the source resource be saved as a copy to the target resource
	//	 * and wait until the task is complete. This method submits a task to the task
	//	 * manager and waits for the task to be completed.
	//	 * <p>
	//	 * Note: This method should never be called from the event dispatch thread.
	//	 *
	//	 * @param source
	//	 * @param target
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void saveCopyAsResourceAndWait( Resource source, Resource target ) throws ExecutionException, InterruptedException {
	//		SaveCopyResourceTask task = new SaveCopyResourceTask( source, target );
	//		program.getTaskManager().invoke( task );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be closed. This method submits a task
	//	 * to the task manager and returns immediately.
	//	 *
	//	 * @param resource The resources to close.
	//	 */
	//	public void closeResources( Resource resource ) {
	//		closeResources( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be closed. This method submits a task
	//	 * to the task manager and returns immediately.
	//	 *
	//	 * @param resources The resources to close.
	//	 */
	//	public void closeResources( Collection<Resource> resources ) {
	//		program.getTaskManager().submit( new CloseResourceTask( resources ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be closed and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should not be called from the event dispatch thread.
	//	 *
	//	 * @param resource The resources to close.
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void closeResourcesAndWait( Resource resource ) throws ExecutionException, InterruptedException {
	//		closeResourcesAndWait( Arrays.asList( new Resource[]{ resource } ) );
	//	}
	//
	//	/**
	//	 * Request that the specified resources be closed and wait until the task is
	//	 * complete. This method submits a task to the task manager and waits for the
	//	 * task to be completed.
	//	 * <p>
	//	 * Note: This method should not be called from the event dispatch thread.
	//	 *
	//	 * @param resources The resources to close.
	//	 * @throws ExecutionException
	//	 * @throws InterruptedException
	//	 */
	//	public void closeResourcesAndWait( Collection<Resource> resources ) throws ExecutionException, InterruptedException {
	//		program.getTaskManager().invoke( new CloseResourceTask( resources ) );
	//	}

		private void persistOpenResources() {
		// FIXME Reimplement as a Configurable class
//			Settings settings = program.getSettings().getNode( ProgramSettingsPath.OPEN_RESOURCES );
//
//			synchronized( restoreLock ) {
//				settings.removeNode();
//
//				for( Resource resource : openResources ) {
//					URI uri = resource.getUri();
//					if( uri == null ) continue;
//					String uriString = uri.toASCIIString();
//					settings.put( "resource-" + HashUtil.hash( uriString ), uriString );
//				}
//			}
		}

	//	public void restoreOpenResources() {
	//		// Clear the existing open resources set.
	//		openResources.clear();
	//
	//		// Collect the resources to be opened.
	//		Set<Resource> resources = new HashSet<Resource>();
	//		Settings settings = program.getSettings().getNode( ProgramSettingsPath.OPEN_RESOURCES );
	//		synchronized( restoreLock ) {
	//			for( String key : settings.getKeys() ) {
	//				try {
	//					String uri = settings.get( key, null );
	//					if( uri != null ) resources.add( createResource( new URI( uri ) ) );
	//				} catch( URISyntaxException exception ) {
	//					continue;
	//				}
	//			}
	//		}
	//
	//		// Open and load the resources.
	//		try {
	//			openResourcesAndWait( resources );
	//			loadResources( resources );
	//		} catch( ResourceException exception ) {
	//			Log.write( Log.WARN, exception );
	//			return;
	//		} catch( InterruptedException exception ) {
	//			return;
	//		}
	//	}

		/**
		 * Get a collection of the supported codecs.
		 *
		 * @return
		 */
		public Collection<Codec> getCodecs() {
			Set<Codec> codecs = new HashSet<Codec>();

			for( ResourceType type : resourceTypes.values() ) {
				codecs.addAll( type.getCodecs() );
			}

			return Collections.unmodifiableCollection( codecs );
		}

		// FIXME Resources should also implement Configurable
	//	public Settings getResourceSettings( Resource resource ) {
	//		if( resource.getUri() == null ) return null;
	//		String name = HashUtil.hash( resource.getUri().toString() );
	//		return program.getSettings().getNode( ProgramSettingsPath.RESOURCE_SETTINGS ).getNode( name );
	//	}

	private Resource findOpenResource( Resource resource ) {
		for( Resource open : openResources ) {
			if( open.equals( resource ) ) return open;
		}
		return resource;
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

	//	private void updateActionState() {
	//		newActionHandler.setEnabled( resourceTypes.size() > 0 );
	//		openActionHandler.setEnabled( resourceTypes.size() > 0 );
	//		saveAllActionHandler.setEnabled( getModifiedResources().size() > 0 );
	//	}

	private void registerCodec( Codec codec, Set<String> values, Map<String, Set<Codec>> registrations ) {
		if( values == null ) return;

		for( String value : values ) {
			Set<Codec> registeredCodecs = registrations.get( value );
			if( registeredCodecs == null ) {
				registeredCodecs = new CopyOnWriteArraySet<Codec>();
				registrations.put( value, registeredCodecs );
			}
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

	private boolean doOpenResource( Resource resource ) throws ResourceException {
		// Determine the resource type.
		ResourceType type = resource.getType();
		if( type == null ) type = autoDetectResourceType( resource );
		log.trace( "Resource type: " + type );

		if( type == null ) throw new ResourceException( resource, "Resource type could not be determined: " + resource );

		// Determine the codec.
		Codec codec = resource.getCodec();
		if( codec == null ) {
			codec = resource.getType().getDefaultCodec();
			resource.setCodec( codec );
		}
		log.trace( "Resource codec: " + codec );

		// Initialize the resource.
		if( !type.resourceDefault( program, resource ) ) return false;
		log.trace( "Resource initialized with default values." );

		// If the resource URI is null get user input from the resource type.
		if( resource.getUri() == null ) {
			if( !type.resourceDialog( program, resource ) ) return false;
			if( isResourceOpen( resource ) ) return true;
			log.trace( "Resource initialized with user values." );
		}

		// Open the resource.
		resource.open();

		// Add the resource to the list of open resources.
		openResources.add( resource );
		persistOpenResources();

		log.trace( "Resource opened: " + resource );

		program.fireEvent( new ResourceOpenedEvent( getClass(), resource ) );

		return true;
	}

	//	private boolean doLoadResource( Resource resource ) throws ResourceException {
	//		if( resource == null ) return false;
	//
	//		if( !resource.isOpen() ) doOpenResource( resource );
	//
	//		boolean previouslyLoaded = resource.isLoaded();
	//
	//		// Load the resource.
	//		if( resource.exists() ) {
	//			resource.load( this );
	//			resource.setModified( false );
	//		}
	//
	//		if( !previouslyLoaded ) resource.addDataListener( new ModifiedEventWatcher() );
	//
	//		Log.write( Log.TRACE, "Resource loaded: " + resource );
	//
	//		program.getEventBus().submit( new ResourceLoadedEvent( getClass(), resource ) );
	//
	//		if( !previouslyLoaded ) {
	//			resource.setReady();
	//		} else {
	//			resource.refresh();
	//		}
	//
	//		return true;
	//	}
	//
	//	private boolean doSaveResource( Resource resource, Resource saveAsResource, boolean saveAs, boolean copy ) throws ResourceException {
	//		if( resource == null ) return false;
	//		if( !isResourceOpen( resource ) ) return false;
	//
	//		URI uri = resource.getUri();
	//		Codec codec = resource.getCodec();
	//
	//		if( uri == null || (saveAs && saveAsResource == null) ) {
	//			Settings settings = program.getSettings().getNode( ProgramSettingsPath.RESOURCE_MANAGER );
	//			String currentDirectory = settings.get( CURRENT_DIRECTORY_SETTING_KEY, System.getProperty( "user.dir" ) );
	//
	//			JFileChooser chooser = new JFileChooser();
	//			chooser.setMultiSelectionEnabled( true );
	//			chooser.setCurrentDirectory( new File( currentDirectory ) );
	//			chooser.setAcceptAllFileFilterUsed( false );
	//			Codec resourceCodec = resource.getCodec();
	//
	//			// If there is not a codec associated with the resource choose the default.
	//			if( resourceCodec == null ) resourceCodec = resource.getType().getDefaultCodec();
	//
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
	//		}
	//
	//		if( saveAsResource != null ) {
	//			if( copy ) {
	//				saveAsResource.fill( resource );
	//				resource = saveAsResource;
	//			} else {
	//				resource.setUri( saveAsResource.getUri() );
	//				resource.setCodec( saveAsResource.getCodec() );
	//			}
	//		}
	//
	//		resource.save( this );
	//		persistOpenResources();
	//
	//		// Note: The resource watcher will log that the resource was unmodified.
	//		resource.setModified( false );
	//
	//		// TODO Update the resource type.
	//
	//		Log.write( Log.TRACE, "Resource saved: " + resource );
	//
	//		program.getEventBus().submit( new ResourceSavedEvent( getClass(), resource ) );
	//
	//		return true;
	//	}
	//
	//	private boolean doCloseResource( Resource resource ) throws ResourceException {
	//		if( resource == null ) return false;
	//		if( !isResourceOpen( resource ) ) return false;
	//
	//		boolean result = false;
	//		if( resource.isModified() && canSaveResource( resource ) ) {
	//			String title = Bundles.getString( BundleKey.ACTIONS, "close" );
	//			String question = Bundles.getString( BundleKey.MESSAGES, "resource.close.save.question" );
	//
	//			int choice = program.notify( title, question, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
	//
	//			if( choice == JOptionPane.YES_OPTION ) {
	//				result = doSaveResource( resource, null, false, false );
	//			} else if( choice == JOptionPane.CANCEL_OPTION ) {
	//				return false;
	//			}
	//		}
	//
	//		resource.close( this );
	//		openResources.remove( resource );
	//		persistOpenResources();
	//
	//		if( openResources.size() == 0 ) doSetCurrentResource( null );
	//
	//		Settings settings = getResourceSettings( resource );
	//		if( settings != null ) settings.removeNode();
	//
	//		Log.write( Log.TRACE, "Resource closed: " + resource );
	//
	//		program.getEventBus().submit( new ResourceClosedEvent( getClass(), resource ) );
	//
	//		return result;
	//	}
	//
	//	private boolean doSetCurrentResource( Resource resource ) {
	//		synchronized( currentResourceLock ) {
	//			Resource previous = currentResource;
	//
	//			// "Disconnect" the old current resource.
	//			if( currentResource != null ) currentResource.removeResourceListener( currentResourceWatcher );
	//
	//			// Change current resource.
	//			currentResource = resource;
	//
	//			// "Connect" the new current resource.
	//			if( currentResource == null ) {
	//				saveActionHandler.setEnabled( false );
	//				saveAsActionHandler.setEnabled( false );
	//				saveCopyAsActionHandler.setEnabled( false );
	//				closeActionHandler.setEnabled( false );
	//			} else {
	//				boolean canSave = canSaveResource( resource );
	//				saveActionHandler.setEnabled( currentResource.isModified() && canSave );
	//				saveAsActionHandler.setEnabled( canSave );
	//				saveCopyAsActionHandler.setEnabled( canSave );
	//				closeActionHandler.setEnabled( true );
	//				currentResource.addResourceListener( currentResourceWatcher );
	//			}
	//
	//			closeAllActionHandler.setEnabled( openResources.size() > 0 );
	//
	//			Log.write( Log.TRACE, "Resource select: " + (resource == null ? "null" : resource) );
	//
	//			// Notify program of current resource change.
	//			program.getEventBus().submit( new CurrentResourceChangedEvent( getClass(), previous, currentResource ) );
	//		}
	//
	//		return true;
	//	}

	/**
	 * Determine the resource type for the given resource from the resource URI.
	 *
	 * @param resource
	 * @return
	 */
	private ResourceType autoDetectResourceType( Resource resource ) {
		// Look for resource types assigned to specific schemes.
		URI uri = resource.getUri();
		if( uri != null ) {
			Scheme scheme = Schemes.getScheme( uri.getScheme() );
			ResourceType type = Schemes.getResourceType( scheme );
			if( type != null ) {
				resource.setType( type );
				return type;
			}
		}

		// Look for resource types assigned to specific codecs.
		List<Codec> codecs = new ArrayList<Codec>( autoDetectCodecs( resource ) );

		// Sort the codecs.
		Collections.sort( codecs, new CodecPriorityComparator() );
		Collections.reverse( codecs );

		Codec codec = codecs.size() == 0 ? null : codecs.get( 0 );
		if( codec == null ) return null;

		ResourceType type = codec.getResourceType();

		resource.setCodec( codec );
		resource.setType( type );

		return type;
	}

	/**
	 * Determine the codec for the given resource by checking the file name, the
	 * first line, and the content type for a match with a supported resource
	 * type. When calling this method the resource needs to already be open so
	 * that the information needed to determine the correct codec is defined in
	 * the resource.
	 * <p>
	 * Note: This method uses a URLConnection object to get the first line and
	 * content type of the resource. This means that the calling thread will be
	 * blocked during the IO operations used in URLConnection if the first line or
	 * the content type is needed to determine the resource type.
	 *
	 * @param resource
	 * @return
	 */
	private Set<Codec> autoDetectCodecs( Resource resource ) {
		URLConnection connection = null;
		Set<Codec> codecs = new HashSet<Codec>();
		//		Collection<ResourceType> resourceTypes = getResourceTypes();
		//
		//		// First option: Determine codec by file name.
		//		String fileName = null;
		//		String path = resource.getUri().getPath();
		//		if( path != null ) fileName = path.substring( path.lastIndexOf( '/' ) + 1 );
		//		if( !StringUtils.isEmpty( fileName ) ) {
		//			for( ResourceType check : resourceTypes ) {
		//				Codec codec = check.getCodecByFileName( fileName );
		//				if( codec != null && !codecs.contains( codec ) ) codecs.add( codec );
		//			}
		//		}
		//
		//		// Second option: Determine codec by first line.
		//		try {
		//			// Load the first line from the resource.
		//			String firstLine = null;
		//
		//			if( connection == null ) connection = getConnection( resource.getUri() );
		//			if( connection != null ) firstLine = getFirstLine( connection.getInputStream(), connection.getContentEncoding() );
		//
		//			if( !TextUtil.isEmpty( firstLine ) ) {
		//				for( ResourceType check : resourceTypes ) {
		//					Codec codec = check.getCodecByFirstLine( firstLine );
		//					if( codec != null && !codecs.contains( codec ) ) codecs.add( codec );
		//				}
		//			}
		//		} catch( IOException exception ) {
		//			// It is not important that the resource cannot be loaded when determining codec type.
		//		} finally {
		//			try {
		//				if( connection != null ) connection.getInputStream().close();
		//			} catch( IOException exception ) {
		//				Log.write( exception );
		//			}
		//		}
		//
		//		// Third option: Determine codec by content type.
		//		String contentType = null;
		//		if( connection == null ) connection = getConnection( resource.getUri() );
		//		if( connection != null ) contentType = connection.getContentType();
		//
		//		// Store the content type in the resource.
		//		if( !TextUtil.isEmpty( contentType ) ) {
		//			resource.putResource( Resource.CONTENT_TYPE_RESOURCE_KEY, contentType );
		//			for( ResourceType check : resourceTypes ) {
		//				Codec codec = check.getCodecByContentType( contentType );
		//				if( codec != null && !codecs.contains( codec ) ) codecs.add( codec );
		//			}
		//		}

		return codecs;
	}

	private URLConnection getConnection( URI uri ) {
		if( !Schemes.getSchemeNames().contains( uri.getScheme() ) ) return null;

		try {
			return uri.toURL().openConnection();
		} catch( Exception exception ) {
			log.warn( "Error opening URL connection", uri );
			log.warn( "Error opening URL connection", exception );
		}

		return null;
	}

	//	private String getFirstLine( InputStream input, String encoding ) throws IOException {
	//		if( input == null ) return null;
	//		if( encoding == null ) encoding = TextUtil.DEFAULT_ENCODING;
	//
	//		byte[] buffer = new byte[ FIRST_LINE_LIMIT ];
	//		ByteArrayOutputStream output = new ByteArrayOutputStream();
	//
	//		int count = 0;
	//		int read = -1;
	//		while( (read = input.read( buffer )) > -1 && count < FIRST_LINE_LIMIT ) {
	//			// Search for line termination.
	//			boolean eol = false;
	//			for( int index = 0; index < read; index++ ) {
	//				int data = buffer[ index ];
	//				if( data == 10 || data == 13 ) {
	//					read = index;
	//					eol = true;
	//					break;
	//				}
	//			}
	//
	//			// Write the buffer.
	//			output.write( buffer, 0, read );
	//			count += read;
	//
	//			// If a line break was encountered stop.
	//			if( eol ) break;
	//		}
	//
	//		return new String( output.toByteArray(), encoding );
	//	}
	//
	//	// FIXME ResourceManager.createResourceEditor() should be run on the EDT.
	//	private void createResourceEditor( Resource resource, ToolView toolview ) {
	//		Tool tool = program.getToolManager().getEditTool( resource );
	//		if( tool == null ) return;
	//
	//		if( toolview == null ) {
	//			program.getActiveWorkPane().addTool( tool, true );
	//		} else {
	//			toolview.getWorkPane().addTool( tool, toolview, true );
	//		}
	//	}
	//
	//	/**
	//	 * Determine if the resource can be saved. The resource can be saved if the
	//	 * URI is null or if the URI scheme and codec can both save resources.
	//	 *
	//	 * @param resource
	//	 * @return True if the resource can be saved, false otherwise.
	//	 */
	//	private boolean canSaveResource( Resource resource ) {
	//		// Check the URI.
	//		URI uri = resource.getUri();
	//		if( uri == null ) return true;
	//
	//		// Check supported schemes.
	//		Scheme scheme = Schemes.getScheme( uri.getScheme() );
	//		if( scheme == null ) return false;
	//
	//		boolean result = false;
	//		try {
	//			Codec codec = resource.getCodec();
	//			result = scheme.canSave( resource ) && (codec == null || codec.canSave());
	//		} catch( ResourceException exception ) {
	//			Log.write( exception );
	//		}
	//
	//		return result;
	//	}
	//
	//	private class OpenActionTask extends Task<Void> {
	//
	//		private Collection<Resource> resources;
	//
	//		private Codec codec;
	//
	//		private ToolView toolview;
	//
	//		private boolean createEditor;
	//
	//		public OpenActionTask( Collection<Resource> resources, Codec codec, ToolView toolview, boolean createEditor ) {
	//			this.resources = resources;
	//			this.codec = codec;
	//			this.toolview = toolview;
	//			this.createEditor = createEditor;
	//		}
	//
	//		@Override
	//		public Void execute() throws Exception {
	//			for( Resource resource : resources ) {
	//				Log.write( Log.TRACE, "Open resource: ", resource.getUri() );
	//
	//				boolean createEditor = this.createEditor || !isResourceOpen( resource );
	//
	//				try {
	//					if( codec != null ) resource.setCodec( codec );
	//					openResourcesAndWait( resource );
	//					if( !resource.isOpen() ) continue;
	//				} catch( Exception exception ) {
	//					program.error( exception );
	//					continue;
	//				}
	//
	//				if( !resource.isLoaded() ) loadResourcesAndWait( resource );
	//				if( createEditor ) createResourceEditor( resource, toolview );
	//				setCurrentResource( resource );
	//			}
	//
	//			return null;
	//		}
	//
	//	}
	//
	//	private class NewActionHandler extends XActionHandler {
	//
	//		@Override
	//		public void actionPerformed( ActionEvent event ) {
	//			Collection<ResourceType> types = getResourceTypes();
	//
	//			ResourceType type = null;
	//			if( types.size() == 1 ) {
	//				type = types.iterator().next();
	//			} else {
	//				String title = Bundles.getString( BundleKey.LABELS, "new" );
	//				ResourceTypePanel panel = new ResourceTypePanel( ResourceManager.this );
	//
	//				int result = program.notify( title, panel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
	//
	//				if( result == JOptionPane.OK_OPTION ) type = panel.getResourceType();
	//			}
	//
	//			program.getTaskManager().submit( new LoadResource( type ) );
	//		}
	//
	//		private class LoadResource extends Task<Resource> {
	//
	//			private ResourceType type;
	//
	//			public LoadResource( ResourceType type ) {
	//				this.type = type;
	//			}
	//
	//			@Override
	//			public Resource execute() throws Exception {
	//				if( type == null ) return null;
	//
	//				Resource resource = null;
	//				try {
	//					resource = createResource( type );
	//					openResourcesAndWait( resource );
	//					resource = findOpenResource( resource );
	//					if( !resource.isOpen() ) return null;
	//				} catch( Exception exception ) {
	//					program.error( exception );
	//					return null;
	//				}
	//				resource.setModified( true );
	//
	//				if( !resource.isLoaded() ) loadResourcesAndWait( resource );
	//				createResourceEditor( resource, null );
	//				setCurrentResource( resource );
	//
	//				return resource;
	//			}
	//
	//		}
	//
	//	}
	//
	//	private class OpenActionHandler extends XActionHandler {
	//
	//		private ResourceTool tool;
	//
	//		@Override
	//		public void actionPerformed( ActionEvent event ) {
	//			tool = (ResourceTool)program.getToolManager().getWorkTool( ResourceTool.class );
	//			if( tool == null ) return;
	//
	//			tool.addActionListener( new ResourceToolOpenHandler() );
	//			program.getActiveWorkPane().addTool( tool, true );
	//
	//			setEnabled( false );
	//		}
	//
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
	//	}
	//
	//	private class SaveActionHandler extends XActionHandler {
	//
	//		private Resource resource;
	//
	//		private boolean saveAs;
	//
	//		private boolean copy;
	//
	//		private ResourceTool tool;
	//
	//		public SaveActionHandler( boolean saveAs, boolean copy ) {
	//			this.saveAs = saveAs;
	//			this.copy = copy;
	//		}
	//
	//		@Override
	//		public void actionPerformed( ActionEvent event ) {
	//			resource = getCurrentResource();
	//
	//			if( saveAs ) {
	//				// Ask the user for the new resource location.
	//				tool = (ResourceTool)program.getToolManager().getWorkTool( ResourceTool.class );
	//				if( tool == null ) return;
	//
	//				tool.addActionListener( new ResourceToolSaveHandler() );
	//				program.getActiveWorkPane().addTool( tool, true );
	//			} else {
	//				saveResources( resource );
	//			}
	//		}
	//
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
	//
	//	}
	//
	//	private class SaveAllActionHandler extends XActionHandler {
	//
	//		@Override
	//		public void actionPerformed( ActionEvent event ) {
	//			try {
	//				saveResources( getModifiedResources() );
	//			} catch( Exception exception ) {
	//				program.error( exception );
	//			}
	//		}
	//
	//	}
	//
	//	private class CloseActionHandler extends XActionHandler {
	//
	//		@Override
	//		public void actionPerformed( ActionEvent event ) {
	//			try {
	//				closeResources( getCurrentResource() );
	//			} catch( Exception exception ) {
	//				program.error( exception );
	//			}
	//		}
	//
	//	}
	//
	//	private class CloseAllActionHandler extends XActionHandler {
	//
	//		@Override
	//		public void actionPerformed( ActionEvent event ) {
	//			try {
	//				closeResources( openResources );
	//			} catch( Exception exception ) {
	//				program.error( exception );
	//			}
	//		}
	//
	//	}

	private abstract class ResourceTask extends ProgramTask<Collection<Resource>> {

		private Collection<Resource> resources;

		public ResourceTask( Resource resource ) {
			super( program );
			this.resources = Arrays.asList( new Resource[]{ resource } );
		}

		public ResourceTask( Resource... resources ) {
			super( program );
			this.resources = Arrays.asList( resources );
		}

		public ResourceTask( Collection<Resource> resources ) {
			super( program );
			this.resources = resources;
		}

		@Override
		public Collection<Resource> execute() throws Exception {
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
				// FIXME Log the error information
				//				String title = ProductUtil.getString( getProgram(), BundleKey.LABELS, "resources" );
				//				String message = ProductUtil.getString( getProgram(), BundleKey.MESSAGES, "resource.exception", messages.toString() );
				//				//program.error( title, message );
				//				log.warn( "Error executing resource task", message );
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

		public OpenResourceTask( Collection<Resource> resources ) {
			super( resources );
		}

		@Override
		public boolean doOperation( Resource resource ) throws ResourceException {
			return doOpenResource( resource );
		}

	}

	//	private class LoadResourceTask extends ResourceTask {
	//
	//		public LoadResourceTask( Resource... resources ) {
	//			super( resources );
	//		}
	//
	//		public LoadResourceTask( Collection<Resource> resources ) {
	//			super( resources );
	//		}
	//
	//		@Override
	//		public boolean doOperation( Resource resource ) throws ResourceException {
	//			return doLoadResource( resource );
	//		}
	//
	//	}
	//
	//	private class SaveResourceTask extends ResourceTask {
	//
	//		public SaveResourceTask( Resource resource ) {
	//			super( resource );
	//		}
	//
	//		public SaveResourceTask( Resource... resources ) {
	//			super( resources );
	//		}
	//
	//		public SaveResourceTask( Collection<Resource> resources ) {
	//			super( resources );
	//		}
	//
	//		@Override
	//		public boolean doOperation( Resource resource ) throws ResourceException {
	//			Log.write( "Save resource..." );
	//
	//			return doSaveResource( resource, null, false, false );
	//		}
	//
	//	}
	//
	//	private class SaveAsResourceTask extends ResourceTask {
	//
	//		private Resource target;
	//
	//		public SaveAsResourceTask( Resource source, Resource target ) {
	//			super( source );
	//			this.target = target;
	//		}
	//
	//		@Override
	//		public boolean doOperation( Resource resource ) throws ResourceException {
	//			return doSaveResource( resource, target, true, false );
	//		}
	//
	//	}
	//
	//	private class SaveCopyResourceTask extends ResourceTask {
	//
	//		private Resource target;
	//
	//		public SaveCopyResourceTask( Resource source, Resource target ) {
	//			super( source );
	//			this.target = target;
	//		}
	//
	//		@Override
	//		public boolean doOperation( Resource resource ) throws ResourceException {
	//			return doSaveResource( resource, target, true, true );
	//		}
	//
	//	}
	//
	//	private class CloseResourceTask extends ResourceTask {
	//
	//		public CloseResourceTask( Collection<Resource> resources ) {
	//			super( resources );
	//		}
	//
	//		@Override
	//		public boolean doOperation( Resource resource ) throws ResourceException {
	//			return doCloseResource( resource );
	//		}
	//
	//	}
	//
	//	private class SetCurrentResourceTask extends ResourceTask {
	//
	//		public SetCurrentResourceTask( Resource resource ) {
	//			super( resource );
	//		}
	//
	//		@Override
	//		public boolean doOperation( Resource resource ) throws ResourceException {
	//			return doSetCurrentResource( resource );
	//		}
	//
	//	}
	//
	//	private class CurrentResourceWatcher extends ResourceAdapter {
	//
	//		@Override
	//		public void resourceModified( ResourceEvent event ) {
	//			Resource resource = event.getResource();
	//			Log.write( Log.TRACE, "Resource modified: " + resource );
	//			saveActionHandler.setEnabled( canSaveResource( resource ) );
	//		}
	//
	//		@Override
	//		public void resourceUnmodified( ResourceEvent event ) {
	//			Resource resource = event.getResource();
	//			saveActionHandler.setEnabled( false );
	//			Log.write( Log.TRACE, "Resource unmodified: " + resource );
	//		}
	//
	//	}
	//
	//	private class ModifiedEventWatcher extends DataAdapter {
	//
	//		@Override
	//		public void metaAttributeChanged( MetaAttributeEvent event ) {
	//			updateActionState();
	//			Log.write( Log.DEBUG, "Data metadata changed: " + event.getSender() + ": " + event.getAttributeName() + ": " + event.getNewValue() );
	//		}
	//
	//		@Override
	//		public void dataAttributeChanged( DataAttributeEvent event ) {
	//			Log.write( Log.DEBUG, "Data attribute changed: " + event.getSender() + ": " + event.getAttributeName() + ": " + event.getNewValue() );
	//		}
	//
	//		@Override
	//		public void childInserted( DataChildEvent event ) {
	//			Log.write( Log.DEBUG, "Data child inserted: " + event.getChild() );
	//		}
	//
	//		@Override
	//		public void childRemoved( DataChildEvent event ) {
	//			Log.write( Log.DEBUG, "Data child removed: " + event.getChild() );
	//		}
	//
	//	}

}
