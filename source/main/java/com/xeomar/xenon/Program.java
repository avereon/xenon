package com.xeomar.xenon;

import com.xeomar.product.ProductBundle;
import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductEvent;
import com.xeomar.product.ProductEventListener;
import com.xeomar.settings.Settings;
import com.xeomar.util.*;
import com.xeomar.xenon.action.*;
import com.xeomar.xenon.event.ProgramStartedEvent;
import com.xeomar.xenon.event.ProgramStartingEvent;
import com.xeomar.xenon.event.ProgramStoppedEvent;
import com.xeomar.xenon.event.ProgramStoppingEvent;
import com.xeomar.xenon.notice.Notice;
import com.xeomar.xenon.notice.NoticeManager;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceManager;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.resource.type.*;
import com.xeomar.xenon.scheme.FileScheme;
import com.xeomar.xenon.scheme.ProgramScheme;
import com.xeomar.xenon.task.TaskManager;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.tool.ToolInstanceMode;
import com.xeomar.xenon.tool.ToolMetadata;
import com.xeomar.xenon.tool.about.AboutTool;
import com.xeomar.xenon.tool.guide.GuideTool;
import com.xeomar.xenon.tool.notice.NoticeTool;
import com.xeomar.xenon.tool.product.ProductTool;
import com.xeomar.xenon.tool.settings.SettingsTool;
import com.xeomar.xenon.tool.task.TaskTool;
import com.xeomar.xenon.tool.welcome.WelcomeTool;
import com.xeomar.xenon.update.MarketCard;
import com.xeomar.xenon.update.ProductManager;
import com.xeomar.xenon.update.ProgramProductManager;
import com.xeomar.xenon.util.DialogUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Program extends Application implements ProgramProduct {

	public static final String STYLESHEET = "style.css";

	private static final long MANAGER_ACTION_SECONDS = 10;

	private static final String PROGRAM_RELEASE = "product-release";

	private static final String PROGRAM_RELEASE_PRIOR = "product-release-prior";

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	/* This field is used for timing checks */
	@SuppressWarnings( "unused" )
	private static final long programStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

	private com.xeomar.util.Parameters parameters;

	private SplashScreenPane splashScreen;

	private TaskManager taskManager;

	private MarketCard defaultMarket;

	private ProductCard card;

	private Path programHomeFolder;

	private Path programDataFolder;

	private Settings programSettings;

	private ExecMode execMode;

	private IconLibrary iconLibrary;

	private ProductBundle programResourceBundle;

	private ActionLibrary actionLibrary;

	private ProgramServer programServer;

	private SettingsManager settingsManager;

	private ToolManager toolManager;

	private ResourceManager resourceManager;

	private WorkspaceManager workspaceManager;

	private ProductManager productManager;

	private NoticeManager noticeManager;

	private ProgramEventWatcher watcher;

	private ProgramNotifier notifier;

	private Set<ProductEventListener> listeners;

	private ExitAction exitAction;

	private AboutAction aboutAction;

	private RestartAction restartAction;

	private SettingsAction settingsAction;

	private WelcomeAction welcomeAction;

	private NoticeAction noticeAction;

	private ProductAction productAction;

	private UpdateAction updateAction;

	private TaskAction taskAction;

	private Boolean isProgramUpdated;

	public static void main( String[] commands ) {
		launch( commands );
	}

	public Program() {
		// Create program action handlers
		exitAction = new ExitAction( this );
		aboutAction = new AboutAction( this );
		settingsAction = new SettingsAction( this );
		welcomeAction = new WelcomeAction( this );
		noticeAction = new NoticeAction( this );
		productAction = new ProductAction( this );
		updateAction = new UpdateAction( this );
		restartAction = new RestartAction( this );
		taskAction = new TaskAction( this );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();
	}

	// This constructor is specifically available for testing
	// when the application does not need to be started.
	Program( com.xeomar.util.Parameters parameters ) {
		this();
		this.parameters = parameters;
	}

	@Override
	public void init() throws Exception {
		try {
			protectedInit();
		} catch( Throwable throwable ) {
			log.error( "Error initializing program", throwable );
			throw throwable;
		}
	}

	private void protectedInit()  throws Exception {
		time( "init" );

		// NOTE Only do in init() what has to be done before the splash screen can be shown

		// Load the product card
		card = new ProductCard().init( getClass() );
		time( "card" );

		// Initialize the program parameters
		parameters = initProgramParameters();
		time( "parameters" );

		// Print the program header, depends on card and parameters
		printHeader( card, parameters );
		time( "print-header" );

		// Determine the program exec mode, depends on program parameters
		String prefix = getExecModePrefix();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + card.getArtifact(), prefix + card.getName() );

		// Configure logging, depends on parameters and program data folder
		LogUtil.configureLogging( this, parameters, programDataFolder, "program.log" );
		time( "configure-logging" );

		// Configure home folder, depends on logging
		configureHome( parameters );
		time( "configure-home" );

		// Create the product resource bundle
		programResourceBundle = new ProductBundle( getClass(), "/bundles" );
		time( "resource-bundle" );

		// Create the settings manager, depends on program data folder
		settingsManager = new SettingsManager( this ).start();

		// Load the default settings values
		Properties properties = new Properties();
		Map<String, Object> defaultSettingsValues = new HashMap<>();
		properties.load( new InputStreamReader( getClass().getResourceAsStream( "/settings/default.properties" ), TextUtil.CHARSET ) );
		properties.forEach( ( k, v ) -> defaultSettingsValues.put( (String)k, v ) );

		// Create the program settings, depends on settings manager and default settings values
		programSettings = settingsManager.getSettings( ProgramSettings.PROGRAM );
		programSettings.setDefaultValues( defaultSettingsValues );
		time( "settings" );

		// Check for the VERSION CL parameter, depends on program settings
		if( getProgramParameters().isSet( ProgramFlag.VERSION ) ) {
			printVersion( card );
			requestExit( true );
			return;
		}

		// Check for the HELP CL parameter, depends on program settings
		if( getProgramParameters().isSet( ProgramFlag.HELP ) ) {
			printHelp( getProgramParameters().get( ProgramFlag.HELP ) );
			requestExit( true );
			return;
		}

		// Run the peer check before processing commands in case there is a peer already
		if( peerCheck() ) return;
		time( "peer-check" );

		// If there is not a peer, process the commands before processing the updates
		if( processCommands( getProgramParameters() ) ) return;
		time( "process-commands" );

		// Create the task manager, depends on program settings
		// The task manager is created in the init() method so it is available during unit tests
		log.trace( "Starting task manager..." );
		taskManager = configureTaskManager( new TaskManager() ).start();
		taskManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "Task manager started." );
		time( "task-manager" );

		// NOTE The start( Stage ) method is called next on the FX thread
	}

	@Override
	public void start( Stage stage ) throws Exception {
		try {
			protectedStart( stage );
		} catch( Throwable throwable ) {
			log.error( "Error initializing program", throwable );
			throw throwable;
		}
	}

	private void protectedStart( Stage stage ) throws Exception {
		Thread.currentThread().setUncaughtExceptionHandler( new ProgramUncaughtExceptionHandler() );

		// Do not implicitly close the program
		Platform.setImplicitExit( false );

		// Create the icon library
		iconLibrary = new IconLibrary();
		registerIcons();

		// Create the update manager, depends on icon library
		productManager = configureProductManager( new ProgramProductManager( this ) );

		// Process staged updates, depends on product manager
		if( processStagedUpdates() ) return;
		time( "staged-updates" );

		// Show the splash screen
		// NOTE If there is a test failure here it is because tests were run in the same VM
		if( !TestUtil.isTest() ) stage.initStyle( StageStyle.UTILITY );
		splashScreen = new SplashScreenPane( card.getName() ).show( stage );
		time( "splash displayed" );

		// Create the program event watcher, depends on logging
		addEventListener( watcher = new ProgramEventWatcher() );

		// Fire the program starting event, depends on the event watcher
		fireEvent( new ProgramStartingEvent( this ) );
		time( "program-start-event" );

		// Submit the startup task
		taskManager.submit( new Startup() );
	}

	public boolean isRunning() {
		return taskManager.isRunning();
	}

	@Override
	public void stop() throws Exception {
		try {
			protectedStop();
		} catch( Throwable throwable ) {
			log.error( "Error initializing program", throwable );
			throw throwable;
		}
	}

	private void protectedStop() throws Exception {
		taskManager.submit( new Shutdown() ).get();

		// Stop the task manager
		log.trace( "Stopping task manager..." );
		taskManager.stop();
		taskManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "Task manager stopped." );
	}

	public void requestRestart( String... commands ) {
		// Register a shutdown hook to restart the program
		ProgramShutdownHook programShutdownHook = new ProgramShutdownHook( this );
		programShutdownHook.configureForRestart( commands );
		Runtime.getRuntime().addShutdownHook( programShutdownHook );

		// Request the program stop.
		if( !requestExit( true ) ) {
			Runtime.getRuntime().removeShutdownHook( programShutdownHook );
			return;
		}

		// The shutdown hook should restart the program
		log.info( "Restarting..." );
	}

	public void requestUpdate( String... restartCommands ) {
		// Register a shutdown hook to update the program
		ProgramShutdownHook programShutdownHook = new ProgramShutdownHook( this );
		try {
			programShutdownHook.configureForUpdate( restartCommands );
			Runtime.getRuntime().addShutdownHook( programShutdownHook );
		} catch( IOException exception ) {
			String title = getResourceBundle().getString( BundleKey.UPDATE, "updates" );
			String message = getResourceBundle().getString( BundleKey.UPDATE, "update-stage-failure" );
			getNoticeManager().addNotice( new Notice( title, message ) );
			return;
		}

		// Request the program stop.
		if( !requestExit( true ) ) {
			Runtime.getRuntime().removeShutdownHook( programShutdownHook );
			return;
		}

		// The shutdown hook should update the program
		log.info( "Updating..." );
	}

	public boolean requestExit( boolean force ) {
		boolean shutdownVerify = programSettings.get( "shutdown-verify", Boolean.class, true );
		boolean shutdownKeepAlive = programSettings.get( "shutdown-keepalive", Boolean.class, false );

		// If the user desires, prompt to exit the program
		if( !force && shutdownVerify ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( getResourceBundle().getString( "program", "program.close.title" ) );
			alert.setHeaderText( getResourceBundle().getString( "program", "program.close.message" ) );
			alert.setContentText( getResourceBundle().getString( "program", "program.close.prompt" ) );

			Stage stage = getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() != ButtonType.YES ) return false;
		}

		// The workspaceManager can be null if the application is already running as a peer
		if( workspaceManager != null ) workspaceManager.hideWindows();

		if( !TestUtil.isTest() && (force || !shutdownKeepAlive) ) Platform.exit();

		return true;
	}

	public com.xeomar.util.Parameters getProgramParameters() {
		return parameters;
	}

	public MarketCard getMarket() {
		return defaultMarket;
	}

	@Override
	public Program getProgram() {
		return this;
	}

	/**
	 * Get the home folder. If the home folder is null that means that the program is not installed locally and was most likely started with a technology like Java Web Start.
	 *
	 * @return The home folder
	 */
	public Path getHomeFolder() {
		return programHomeFolder;
	}

	public boolean isProgramUpdated() {
		if( isProgramUpdated == null ) isProgramUpdated = calcProgramUpdated();
		return isProgramUpdated;
	}

	@Override
	public ProductCard getCard() {
		return card;
	}

	@Override
	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	@Override
	public ProductBundle getResourceBundle() {
		return programResourceBundle;
	}

	public final Path getDataFolder() {
		return programDataFolder;
	}

	@Deprecated
	public final ProgramNotifier getNotifier() {
		return notifier;
	}

	public final TaskManager getTaskManager() {
		return taskManager;
	}

	public final ExecutorService getExecutor() {
		return taskManager;
	}

	public final IconLibrary getIconLibrary() {
		return iconLibrary;
	}

	public final ActionLibrary getActionLibrary() {
		return actionLibrary;
	}

	public final SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public final ToolManager getToolManager() {
		return toolManager;
	}

	public final ResourceManager getResourceManager() {
		return resourceManager;
	}

	public final WorkspaceManager getWorkspaceManager() {
		return workspaceManager;
	}

	public final ProductManager getProductManager() {
		return productManager;
	}

	public final NoticeManager getNoticeManager() {
		return noticeManager;
	}

	@SuppressWarnings( { "unused", "WeakerAccess" } )
	public void addEventListener( ProductEventListener listener ) {
		this.listeners.add( listener );
	}

	@SuppressWarnings( { "unused", "WeakerAccess" } )
	public void removeEventListener( ProductEventListener listener ) {
		this.listeners.remove( listener );
	}

	public void fireEvent( ProductEvent event ) {
		event.fire( listeners );
	}

	private static void time( String markerName ) {
		//System.out.println( "Time " + markerName + "=" + (System.currentTimeMillis() - programStartTime) );
	}

	/**
	 * Initialize the program parameters by converting the FX parameters object into a program parameters object.
	 *
	 * @return The program parameters object
	 */
	private com.xeomar.util.Parameters initProgramParameters() {
		// The parameters may have been set in the constructor
		if( parameters != null ) return parameters;

		com.xeomar.util.Parameters parameters;

		Parameters fxParameters = getParameters();
		if( fxParameters == null ) {
			parameters = com.xeomar.util.Parameters.create();
		} else {
			parameters = com.xeomar.util.Parameters.parse( fxParameters.getRaw() );
		}

		return parameters;
	}

	/**
	 * Check for another instance of the program is running after getting the settings but before the splash screen is shown. The fastest way to check is to try and bind to the port defined in the settings. The OS will quickly deny the bind
	 * if the port is already bound.
	 * <p>
	 * See: https://stackoverflow.com/questions/41051127/javafx-single-instance-application
	 * </p>
	 */
	private boolean peerCheck() {
		int port = programSettings.get( "program-port", Integer.class, 0 );
		ProgramServer server = new ProgramServer( this, port );

		if( server.start() ) {
			programServer = server;
			return false;
		}

		new ProgramPeer( this, port ).run();
		requestExit( true );
		return true;
	}

	/**
	 * Process program commands that affect the startup behavior of the product.
	 *
	 * @param parameters The command line parameters
	 * @return True if the program should exit, false otherwise.
	 */
	boolean processCommands( com.xeomar.util.Parameters parameters ) {
		if( parameters.isSet( ProgramFlag.WATCH ) ) {
			return true;
		} else if( parameters.isSet( ProgramFlag.STATUS ) ) {
			// TODO Status may not need to go to the host
			printStatus();
			requestExit( true );
			return true;
		} else if( parameters.isSet( ProgramFlag.STOP ) ) {
			requestExit( true );
			return true;
		}

		return false;
	}

	/**
	 * Process staged updates unless the NOUPDATE flag is set. If there are no staged updates then the method returns false. If no updates were processed due to user input then the method returns false.
	 *
	 * @return True if the program should be restarted, false otherwise.
	 */
	private boolean processStagedUpdates() {
		if( parameters.isSet( ProgramFlag.NOUPDATE ) ) return false;
		int result = productManager.updateProduct();
		if( result != 0 ) requestExit( true );
		return result != 0;
	}

	/**
	 * Process the resources specified on the command line.
	 *
	 * @param parameters The command line parameters
	 */
	void processResources( com.xeomar.util.Parameters parameters ) {
		Stage current = getWorkspaceManager().getActiveWorkspace().getStage();
		Platform.runLater( () -> {
			current.show();
			current.requestFocus();
		} );

		// Open the resources provided on the command line
		for( String uri : parameters.getUris() ) {
			try {
				getResourceManager().openResourcesAndWait( getResourceManager().createResource( uri ) );
			} catch( ExecutionException | ResourceException exception ) {
				log.warn( "Unable to open: " + uri );
			} catch( InterruptedException exception ) {
				// Intentionally ignore exception
			}
		}
	}

	/**
	 * Print the ASCII art title. The text is loaded from the resource /ascii-art-title.txt.
	 * <p>
	 * The text was generated using the Standard FIGlet font:
	 * <a href="http://patorjk.com/software/taag/#p=display&h=0&f=Bulbhead&t=XENON">XENON</a>
	 */
	private void printAsciiArtTitle() {
		try {
			InputStream input = getClass().getResourceAsStream( "/ascii-art-title.txt" );
			BufferedReader reader = new BufferedReader( new InputStreamReader( input, TextUtil.CHARSET ) );

			String line;
			while( (line = reader.readLine()) != null ) {
				System.out.println( line );
			}
		} catch( IOException exception ) {
			// Intentionally ignore exception
		}
	}

	private void printHeader( ProductCard card, com.xeomar.util.Parameters parameters ) {
		ExecMode execMode = getExecMode();
		if( execMode == ExecMode.TEST ) return;

		boolean versionParameterSet = parameters.isSet( ProgramFlag.VERSION );
		String versionString = card.getVersion() + (execMode == ExecMode.PROD ? "" : " [" + execMode + "]");
		//String releaseString = versionString + " " + card.getRelease().getTimestampString();
		String releaseString = "";

		printAsciiArtTitle();
		System.out.println();
		System.out.println( card.getName() + " " + (versionParameterSet ? releaseString : versionString) );
	}

	private void printVersion( ProductCard card ) {
		System.out.println( card.getName() + " home=" + getHomeFolder() );
		System.out.println( card.getName() + " data=" + getDataFolder() );
		System.out.println( "Java version=" + System.getProperty( "java.version" ) + " vendor=" + System.getProperty( "java.vendor" ) );
		System.out.println( "Java home=" + System.getProperty( "java.home" ) );
		System.out.println( "Java locale=" + Locale.getDefault() + " encoding=" + System.getProperty( "file.encoding" ) );
		System.out.println( "OS name=" + System.getProperty( "os.name" ) + " version=" + System.getProperty( "os.version" ) + " arch=" + System.getProperty( "os.arch" ) );
	}

	private void printStatus() {
		log.info( "Status: " + (isRunning() ? "RUNNING" : "STOPPED") );
	}

	private void printHelp( String category ) {
		if( category == null ) category = "general";

		switch( category ) {
			case "general": {
				System.out.println( "Usage: " + card.getArtifact() + "[command...] [file...]" );
				break;
			}
		}
	}

	public ExecMode getExecMode() {
		if( execMode != null ) return execMode;

		String execModeParameter = parameters.get( ProgramFlag.EXECMODE );
		if( execModeParameter != null ) {
			try {
				execMode = ExecMode.valueOf( execModeParameter.toUpperCase() );
			} catch( IllegalArgumentException exception ) {
				execMode = ExecMode.DEV;
			}
		}
		if( execMode == null ) execMode = ExecMode.PROD;

		return execMode;
	}

	private String getExecModePrefix() {
		return getExecMode().getPrefix();
	}

	private void doStartupTasks() throws Exception {
		// Create the action library
		actionLibrary = new ActionLibrary( programResourceBundle );
		registerActionHandlers();

		// Create the UI factory
		UiFactory uiFactory = new UiFactory( Program.this );

		// Set the number of startup steps
		int managerCount = 5;
		int steps = managerCount + uiFactory.getToolCount();
		Platform.runLater( () -> splashScreen.setSteps( steps ) );

		// Update the product card
		this.card.load( getClass() );

		Platform.runLater( () -> splashScreen.update() );

		// Start the product manager
		log.trace( "Starting product manager..." );
		productManager.start();
		productManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "Product manager started." );

		// Start the resource manager
		log.trace( "Starting resource manager..." );
		resourceManager = new ResourceManager( Program.this );
		registerSchemes( resourceManager );
		registerResourceTypes( resourceManager );
		resourceManager.start();
		resourceManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Resource manager started." );

		// Load the settings pages
		getSettingsManager().addSettingsPages( this, programSettings, "/settings/pages.xml" );

		// Start the tool manager
		log.trace( "Starting tool manager..." );
		toolManager = new ToolManager( this );
		toolManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		registerTools( toolManager );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Tool manager started." );

		// Create the notice manager
		log.trace( "Starting notice manager..." );
		noticeManager = new NoticeManager( Program.this ).start();
		noticeManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Notice manager started." );

		// Create the workspace manager
		log.trace( "Starting workspace manager..." );
		workspaceManager = new WorkspaceManager( Program.this ).start();
		workspaceManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Workspace manager started." );

		// Restore the user interface
		log.trace( "Restore the user interface..." );
		Platform.runLater( () -> uiFactory.restore( splashScreen ) );
		uiFactory.awaitRestore( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "User interface restored." );

		// Notify the product manager the UI is ready
		productManager.uiIsAvailable();

		// Finish the splash screen
		int totalSteps = splashScreen.getSteps();
		int completedSteps = splashScreen.getCompletedSteps();
		if( completedSteps != totalSteps ) log.warn( "Startup step mismatch: " + completedSteps + " of " + totalSteps );
		Platform.runLater( () -> splashScreen.done() );

		// Create the program notifier, depends on workspace manager
		notifier = new ProgramNotifier( this );

		// Give the slash screen time to render and the user to see it
		Thread.sleep( 500 );
	}

	private void doShutdownTasks() {
		try {
			fireEvent( new ProgramStoppingEvent( this ) );

			// Notify the product manager the UI is ready
			productManager.uiWillStop();

			// Stop the NoticeManager
			if( noticeManager != null ) {
				log.trace( "Stopping notice manager..." );
				noticeManager.stop();
				noticeManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
				log.debug( "Notice manager stopped." );
			}

			// Stop the workspace manager
			if( workspaceManager != null ) {
				log.trace( "Stopping workspace manager..." );
				workspaceManager.stop();
				workspaceManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
				log.debug( "Workspace manager stopped." );
			}

			// Stop the tool manager
			if( toolManager != null ) {
				log.trace( "Stopping tool manager..." );
				toolManager.stop();
				toolManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
				unregisterTools( toolManager );
				log.debug( "Tool manager stopped." );
			}

			// NOTE Do not try to remove the settings pages during shutdown

			// Stop the resource manager
			if( resourceManager != null ) {
				log.trace( "Stopping resource manager..." );
				resourceManager.stop();
				resourceManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
				unregisterResourceTypes( resourceManager );
				unregisterSchemes( resourceManager );
				log.debug( "Resource manager stopped." );
			}

			// Stop the product manager
			if( productManager != null ) {
				log.trace( "Stopping update manager..." );
				productManager.stop();
				productManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
				log.debug( "Update manager stopped." );
			}

			// Disconnect the settings listener
			if( settingsManager != null ) {
				log.trace( "Stopping settings manager..." );
				settingsManager.stop();
				log.debug( "Settings manager stopped." );
			}

			// Unregister action handlers
			if( actionLibrary != null ) unregisterActionHandlers();

			// Unregister icons
			if( iconLibrary != null ) unregisterIcons();

			// Stop the program server
			if( programServer != null ) {
				log.trace( "Stopping program server..." );
				programServer.stop();
				log.debug( "Program server stopped." );
			}

			// NOTE Do not call Platform.exit() here, it was called already
		} catch( InterruptedException exception ) {
			log.error( "Program shutdown interrupted", exception );
		}
	}

	/**
	 * Find the home directory. This method expects the program jar file to be installed in a sub-directory of the home directory. Example: <code>$HOME/lib/program.jar</code>
	 *
	 * @param parameters The command line parameters
	 */
	private void configureHome( com.xeomar.util.Parameters parameters ) {
		try {
			// If the HOME flag was specified on the command line use it.
			if( programHomeFolder == null && parameters.isSet( ProgramFlag.HOME ) ) programHomeFolder = Paths.get( parameters.get( ProgramFlag.HOME ) );

			// Apparently, when running a linked program, there is not a jdk.module.path system property
			// The program home should be the java home when running as a linked application
			boolean isLinked = System.getProperty( "jdk.module.path" ) == null;
			if( programHomeFolder == null && isLinked ) programHomeFolder = Paths.get( System.getProperty( "java.home" ) );

			// However, when in development, don't use the java home
			if( programHomeFolder == null && getExecMode() == ExecMode.DEV && !isLinked ) programHomeFolder = Paths.get( "target/program" );

			// Use the user directory as a last resort (usually for unit tests)
			if( programHomeFolder == null ) programHomeFolder = Paths.get( System.getProperty( "user.dir" ) );

			// Canonicalize the home path.
			if( programHomeFolder != null ) programHomeFolder = programHomeFolder.toFile().getCanonicalFile().toPath();

			// Create the program home folder when in DEV mode
			if( getExecMode() == ExecMode.DEV ) Files.createDirectories( programHomeFolder );

			if( !Files.exists( programHomeFolder ) ) log.warn( "Program home folder does not exist: " + programHomeFolder );
		} catch( IOException exception ) {
			log.error( "Error configuring home folder", exception );
		}

		// Set install folder on product card
		card.setInstallFolder( programHomeFolder );

		log.debug( "Program home: " + programHomeFolder );
		log.debug( "Program data: " + programDataFolder );
	}

	private void registerIcons() {}

	private void unregisterIcons() {}

	private void registerActionHandlers() {
		getActionLibrary().getAction( "exit" ).pushAction( exitAction );
		getActionLibrary().getAction( "about" ).pushAction( aboutAction );
		getActionLibrary().getAction( "settings" ).pushAction( settingsAction );
		getActionLibrary().getAction( "welcome" ).pushAction( welcomeAction );
		getActionLibrary().getAction( "task" ).pushAction( taskAction );
		getActionLibrary().getAction( "notice" ).pushAction( noticeAction );
		getActionLibrary().getAction( "product" ).pushAction( productAction );
		getActionLibrary().getAction( "update" ).pushAction( updateAction );
		getActionLibrary().getAction( "restart" ).pushAction( restartAction );

		getActionLibrary().getAction( "test-action-1" ).pushAction( new RunnableTestAction( this, () ->{
			((ProgramProductManager)getProgram().getProductManager()).showUpdateFoundDialog();
		} ) );
		getActionLibrary().getAction( "test-action-2" ).pushAction( new RunnableTestAction( this, () -> {
			this.getNoticeManager().addNotice( new Notice("Testing","Test Notice A") );
		} ) );
		getActionLibrary().getAction( "test-action-3" ).pushAction( new RunnableTestAction( this, () -> {
			this.getNoticeManager().addNotice( new Notice("Testing","Test Notice B") );
		} ) );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "exit" ).pullAction( exitAction );
		getActionLibrary().getAction( "about" ).pullAction( aboutAction );
		getActionLibrary().getAction( "settings" ).pullAction( settingsAction );
		getActionLibrary().getAction( "welcome" ).pullAction( welcomeAction );
		getActionLibrary().getAction( "task" ).pullAction( taskAction );
		getActionLibrary().getAction( "notice" ).pullAction( noticeAction );
		getActionLibrary().getAction( "product" ).pullAction( productAction );
		getActionLibrary().getAction( "update" ).pullAction( updateAction );
		getActionLibrary().getAction( "restart" ).pullAction( restartAction );
	}

	private void registerSchemes( ResourceManager manager ) {
		manager.addScheme( new ProgramScheme( this ) );
		manager.addScheme( new FileScheme( this ) );
	}

	private void unregisterSchemes( ResourceManager manager ) {
		manager.removeScheme( "program" );
		manager.removeScheme( "file" );
	}

	private void registerResourceTypes( ResourceManager manager ) {
		manager.registerUriResourceType( ProgramGuideType.URI, new ProgramGuideType( this ) );
		manager.registerUriResourceType( ProgramAboutType.URI, new ProgramAboutType( this ) );
		manager.registerUriResourceType( ProgramSettingsType.URI, new ProgramSettingsType( this ) );
		manager.registerUriResourceType( ProgramWelcomeType.URI, new ProgramWelcomeType( this ) );
		manager.registerUriResourceType( ProgramNoticeType.URI, new ProgramNoticeType( this ) );
		manager.registerUriResourceType( ProgramProductType.URI, new ProgramProductType( this ) );
		manager.registerUriResourceType( ProgramTaskType.URI, new ProgramTaskType( this ) );
	}

	private void unregisterResourceTypes( ResourceManager manager ) {
		manager.unregisterUriResourceType( ProgramTaskType.URI );
		manager.unregisterUriResourceType( ProgramProductType.URI );
		manager.unregisterUriResourceType( ProgramNoticeType.URI );
		manager.unregisterUriResourceType( ProgramWelcomeType.URI );
		manager.unregisterUriResourceType( ProgramSettingsType.URI );
		manager.unregisterUriResourceType( ProgramAboutType.URI );
		manager.unregisterUriResourceType( ProgramGuideType.URI );
	}

	private void registerTools( ToolManager manager ) {
		registerTool( manager, ProgramGuideType.class, GuideTool.class, ToolInstanceMode.SINGLETON, "guide", "guide" );
		registerTool( manager, ProgramAboutType.class, AboutTool.class, ToolInstanceMode.SINGLETON, "about", "about" );
		registerTool( manager, ProgramSettingsType.class, SettingsTool.class, ToolInstanceMode.SINGLETON, "settings", "settings" );
		registerTool( manager, ProgramWelcomeType.class, WelcomeTool.class, ToolInstanceMode.SINGLETON, "welcome", "welcome" );
		registerTool( manager, ProgramNoticeType.class, NoticeTool.class, ToolInstanceMode.SINGLETON, "notice", "notice" );
		registerTool( manager, ProgramProductType.class, ProductTool.class, ToolInstanceMode.SINGLETON, "product", "product" );
		registerTool( manager, ProgramTaskType.class, TaskTool.class, ToolInstanceMode.SINGLETON, "task", "task" );

		toolManager.addToolAlias( "com.xeomar.xenon.tool.AboutTool", AboutTool.class );
		toolManager.addToolAlias( "com.xeomar.xenon.tool.NoticeTool", NoticeTool.class );
		toolManager.addToolAlias( "com.xeomar.xenon.tool.WelcomeTool", WelcomeTool.class );
	}

	private void unregisterTools( ToolManager manager ) {
		unregisterTool( manager, ProgramTaskType.class, TaskTool.class );
		unregisterTool( manager, ProgramProductType.class, ProductTool.class );
		unregisterTool( manager, ProgramWelcomeType.class, WelcomeTool.class );
		unregisterTool( manager, ProgramNoticeType.class, NoticeTool.class );
		unregisterTool( manager, ProgramSettingsType.class, SettingsTool.class );
		unregisterTool( manager, ProgramAboutType.class, AboutTool.class );
		unregisterTool( manager, ProgramGuideType.class, GuideTool.class );
	}

	private void registerTool( ToolManager manager, Class<? extends ResourceType> resourceTypeClass, Class<? extends ProgramTool> toolClass, ToolInstanceMode mode, String toolRbKey, String iconKey ) {
		ResourceType type = resourceManager.getResourceType( resourceTypeClass.getName() );
		String name = getResourceBundle().getString( "tool", toolRbKey + "-name" );
		Node icon = getIconLibrary().getIcon( iconKey );

		ToolMetadata metadata = new ToolMetadata();
		metadata.setProduct( this ).setType( toolClass ).setInstanceMode( mode ).setName( name ).setIcon( icon );
		manager.registerTool( type, metadata );
	}

	private void unregisterTool( ToolManager manager, Class<? extends ResourceType> resourceTypeClass, Class<? extends ProgramTool> toolClass ) {
		manager.unregisterTool( resourceManager.getResourceType( resourceTypeClass.getName() ), toolClass );
	}

	private TaskManager configureTaskManager( TaskManager taskManager ) {
		taskManager.setSettings( programSettings );
		return taskManager;
	}

	private ProductManager configureProductManager( ProductManager productManager ) throws IOException {
		// FIXME Do I want the update settings in the program settings?
		// There is also a set of comments regarding this issue in the ProductManager class
		productManager.setSettings( programSettings );

		// Register the product catalog by replacing the old catalog
		productManager.addCatalog( productManager.removeCatalog( defaultMarket = MarketCard.forProduct() ) );

		// Register the product
		productManager.registerProduct( this );

		return productManager;
	}

	private void notifyProgramUpdated() {
		Release prior = Release.decode( programSettings.get( PROGRAM_RELEASE_PRIOR, (String)null ) );
		Release runtime = this.getCard().getRelease();
		String priorVersion = prior.getVersion().toHumanString();
		String runtimeVersion = runtime.getVersion().toHumanString();
		String title = getResourceBundle().getString( BundleKey.UPDATE, "updates" );
		String message = getResourceBundle().getString( BundleKey.UPDATE, "program-updated-message", priorVersion, runtimeVersion );
		getNoticeManager().addNotice( new Notice( title, message, () -> getProgram().getResourceManager().open( ProgramAboutType.URI ) ) );
	}

	private boolean calcProgramUpdated() {
		// Get the last release setting
		Release previous = Release.decode( programSettings.get( PROGRAM_RELEASE, (String)null ) );
		Release runtime = this.getCard().getRelease();

		boolean programUpdated = previous != null && runtime.compareTo( previous ) > 0;

		if( programUpdated ) programSettings.set( PROGRAM_RELEASE_PRIOR, Release.encode( previous ) );
		programSettings.set( PROGRAM_RELEASE, Release.encode( runtime ) );

		return programUpdated;
	}

	private class Startup extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			doStartupTasks();
			return null;
		}

		@Override
		protected void succeeded() {
			splashScreen.hide();
			time( "splash hidden" );

			getWorkspaceManager().getActiveStage().show();
			getWorkspaceManager().getActiveStage().toFront();

			// Program started event should be fired after the window is shown
			Program.this.fireEvent( new ProgramStartedEvent( Program.this ) );
			time( "program started" );

			// Set the workarea actions
			getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaAction( Program.this ) );
			getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaAction( Program.this ) );
			getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaAction( Program.this ) );

			// Open resources specified on the command line
			processResources( getProgramParameters() );

			// Schedule the first update check
			getProductManager().scheduleUpdateCheck( true );

			// TODO Show user notifications
			//getTaskManager().submit( new ShowApplicationNotices() );

			// Check to see if the application was updated
			if( isProgramUpdated() ) notifyProgramUpdated();
		}

		@Override
		protected void cancelled() {
			splashScreen.hide();
			time( "splash hidden" );
			log.warn( "Startup task cancelled" );
		}

		@Override
		protected void failed() {
			splashScreen.hide();
			log.error( "Error during startup task", getException() );
		}

	}

	private class Shutdown extends Task<Void> {

		@Override
		protected Void call() {
			doShutdownTasks();
			return null;
		}

		@Override
		protected void succeeded() {
			Program.this.fireEvent( new ProgramStoppedEvent( Program.this ) );
		}

		@Override
		protected void cancelled() {
			log.error( "Shutdown task cancelled", getException() );
		}

		@Override
		protected void failed() {
			log.error( "Error during shutdown task", getException() );
		}

	}

}
