package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.product.Release;
import com.avereon.settings.Settings;
import com.avereon.util.*;
import com.avereon.venza.event.FxEventHub;
import com.avereon.xenon.action.*;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetManager;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.type.*;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticeLogHandler;
import com.avereon.xenon.notice.NoticeManager;
import com.avereon.xenon.product.ProductManager;
import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.scheme.*;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.throwable.ProgramUncaughtExceptionHandler;
import com.avereon.xenon.tool.*;
import com.avereon.xenon.tool.guide.GuideTool;
import com.avereon.xenon.tool.product.ProductTool;
import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.util.DialogUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.System.Logger.Level.*;

public class Program extends Application implements ProgramProduct {

	public static final String STYLESHEET = "xenon.css";

	public static final long MANAGER_ACTION_SECONDS = 10;

	private static final System.Logger log = Log.get();

	private static final String PROGRAM_RELEASE = "product-release";

	private static final String PROGRAM_RELEASE_PRIOR = "product-release-prior";

	private static final String SETTINGS_DEFAULT_PROPERTIES = Program.class.getPackageName().replace( ".", "/" ) + "/settings/default.properties";

	private static final String SETTINGS_PAGES_XML = Program.class.getPackageName().replace( ".", "/" ) + "/settings/pages.xml";

	private static final boolean SHOW_TIMING = false;

	private static final long programStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

	private final ProgramUncaughtExceptionHandler uncaughtExceptionHandler;

	private com.avereon.util.Parameters parameters;

	private SplashScreenPane splashScreen;

	private ProgramTaskManager taskManager;

	private ProductCard card;

	private Path programHomeFolder;

	private Path programDataFolder;

	private Path programLogFolder;

	private UpdateManager updateManager;

	private Settings programSettings;

	private String profile;

	private IconLibrary iconLibrary;

	private ProductBundle programResourceBundle;

	private ActionLibrary actionLibrary;

	private ProgramServer programServer;

	private SettingsManager settingsManager;

	private ToolManager toolManager;

	private AssetManager assetManager;

	private ThemeManager themeManager;

	private WorkspaceManager workspaceManager;

	private ProductManager productManager;

	private NoticeManager noticeManager;

	private ProgramEventWatcher watcher;

	private FxEventHub fxEventHub;

	private CloseWorkspaceAction closeAction;

	private ExitAction exitAction;

	private AboutAction aboutAction;

	private RestartAction restartAction;

	private SettingsAction settingsAction;

	private ThemesAction themesAction;

	private WelcomeAction welcomeAction;

	private NoticeAction noticeAction;

	private ProductAction productAction;

	private UpdateAction updateAction;

	private MockUpdateAction mockUpdateAction;

	private TaskAction taskAction;

	private WallpaperToggleAction wallpaperToggleAction;

	private WallpaperPriorAction wallpaperPriorAction;

	private WallpaperNextAction wallpaperNextAction;

	private Boolean isProgramUpdated;

	// THREAD main
	// EXCEPTIONS Handled by the FX framework
	public static void launch( String[] commands ) {
		Application.launch( commands );
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	public Program() {
		time( "instantiate" );
		uncaughtExceptionHandler = new ProgramUncaughtExceptionHandler();

		// Add the uncaught exception handler to the JavaFX Application Thread
		Thread.currentThread().setUncaughtExceptionHandler( uncaughtExceptionHandler );

		// Do not implicitly close the program
		Platform.setImplicitExit( false );
		time( "implicit-exit-false" );
	}

	// THREAD JavaFX-Launcher
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void init() throws Exception {
		// NOTE Only do in init() what should be done before the splash screen is shown
		time( "init" );

		// Add the uncaught exception handler to the JavaFX-Launcher thread
		Thread.currentThread().setUncaughtExceptionHandler( uncaughtExceptionHandler );

		// Init the product card
		card = ProgramConfig.loadProductCard();
		time( "card" );

		// Initialize the program parameters
		parameters = initProgramParameters();
		time( "parameters" );

		// Print the program header, depends on card and parameters
		printHeader( card, parameters );
		time( "print-header" );

		// Determine the program data folder, depends on program parameters
		programDataFolder = configureDataFolder();
		time( "configure-data-folder" );

		// Create the product resource bundle
		programResourceBundle = new ProductBundle( this );
		time( "resource-bundle" );

		// Configure logging, depends on parameters and program data folder
		configureLogging();
		time( "configure-logging" );

		// Configure home folder, depends on logging
		configureHomeFolder( parameters );
		time( "configure-home-folder" );

		// Check for the VERSION CL parameter, depends on program settings
		if( getProgramParameters().isSet( ProgramFlag.VERSION ) ) {
			printVersion( card );
			requestExit( true );
			return;
		}
		time( "version-check" );

		// Check for the HELP CL parameter, depends on program settings
		if( getProgramParameters().isSet( ProgramFlag.HELP ) ) {
			printHelp( getProgramParameters().get( ProgramFlag.HELP ) );
			requestExit( true );
			return;
		}
		time( "help-check" );

		// Create the event hub
		fxEventHub = new FxEventHub();

		// Create the settings manager, depends on program data folder, FX event hub
		settingsManager = configureSettingsManager( new SettingsManager( this ) ).start();

		// Create the program settings, depends on settings manager and default settings values
		programSettings = getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		programSettings.setDefaultValues( loadDefaultSettings() );
		time( "program-settings" );

		// Run the peer check before processing actions in case there is a peer already
		// If this instance is a peer, start the peer and wait to exit
		int port = programSettings.get( "program-port", Integer.class, 0 );
		if( !TestUtil.isTest() && peerCheck( port ) ) {
			//			if( parameters.isSet( ProgramFlag.UPDATE ) ) {
			//				log.log( Log.ERROR, "Cannot run update in peer mode" );
			//			} else {
			new ProgramPeer( this, port ).start();
			//			}
			requestExit( true );
			return;
		}
		time( "peer-check" );

		// NOTE At this point this instance is a host not a peer

		// If this instance is a host, process the control commands before showing the splash screen
		if( !processCliActions( getProgramParameters(), true ) ) {
			requestExit( true );
			return;
		}
		time( "control-commands" );

		// Create the task manager, depends on program settings
		// The task manager is created in the init() method so it is available during unit tests
		log.log( TRACE, "Starting task manager..." );
		taskManager = (ProgramTaskManager)configureTaskManager( new ProgramTaskManager( this ) ).start();
		log.log( DEBUG, "Task manager started." );
		time( "task-manager" );

		// NOTE The start( Stage ) method is called next
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void start( Stage stage ) {
		time( "fx-start" );

		// Add an uncaught exception handler to the FX thread
		Thread.currentThread().setUncaughtExceptionHandler( uncaughtExceptionHandler );
		time( "uncaught-exception-handler" );

		// This must be set before the splash screen is shown
		Application.setUserAgentStylesheet( Application.STYLESHEET_MODENA );
		time( "stylesheet" );

		// Show the splash screen, depends stylesheet
		// NOTE If there is a test failure here it is because tests were run in the same VM
		if( stage.getStyle() != StageStyle.UTILITY ) stage.initStyle( StageStyle.UTILITY );
		splashScreen = new SplashScreenPane( card.getName() );
		if( !parameters.isSet( ProgramFlag.NOSPLASH ) ) splashScreen.show( stage );
		time( "splash-displayed" );

		// Submit but do not wait for the startup task...allow the FX thread to be free
		getTaskManager().submit( new StartupTask() );
	}

	private class StartupTask extends Task<Void> {

		private StartupTask() {
			setPriority( Priority.HIGH );
		}

		@Override
		public Void call() throws Exception {
			doStartTasks();
			return null;
		}

		@Override
		protected void success() {
			doStartSuccess();
		}

		@Override
		protected void cancelled() {
			Platform.runLater( () -> splashScreen.hide() );
			log.log( ERROR, "Startup task cancelled", getException() );
			requestExit( true );
		}

		@Override
		protected void failed() {
			Platform.runLater( () -> splashScreen.hide() );
			log.log( ERROR, "Startup task failed", getException() );
			requestExit( true );
		}

	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doStartTasks() throws Exception {
		time( "do-startup-tasks" );

		// Create the program event watcher, depends on logging
		getFxEventHub().register( Event.ANY, watcher = new ProgramEventWatcher() );
		time( "event-hub" );

		// Fire the program starting event, depends on the event watcher
		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STARTING ) );
		time( "program-starting-event" );

		// Create the product manager, depends on icon library
		productManager = configureProductManager( new ProductManager( this ) );
		time( "product-manager" );

		// Create the icon library
		iconLibrary = new IconLibrary( this );
		time( "icon-library" );

		// Create the action library
		actionLibrary = new ActionLibrary( this );
		registerActionHandlers();
		time( "program-actions" );

		// Create the UI factory
		UiRegenerator uiRegenerator = new UiRegenerator( Program.this );

		// Set the number of startup steps
		int managerCount = 6;
		int steps = managerCount + uiRegenerator.getToolCount();
		Platform.runLater( () -> splashScreen.setSteps( steps ) );

		// Update the product card
		this.card.load( this );

		Platform.runLater( () -> splashScreen.update() );

		// Start the asset manager
		log.log( TRACE, "Starting asset manager..." );
		assetManager = new AssetManager( Program.this );
		assetManager.getEventBus().parent( getFxEventHub() );
		registerSchemes( assetManager );
		registerAssetTypes( assetManager );
		assetManager.start();
		Platform.runLater( () -> splashScreen.update() );
		log.log( DEBUG, "Asset manager started." );

		// Load the settings pages
		getSettingsManager().addSettingsPages( this, programSettings, SETTINGS_PAGES_XML );

		// Start the tool manager
		log.log( TRACE, "Starting tool manager..." );
		toolManager = new ToolManager( this );
		registerTools( toolManager );
		Platform.runLater( () -> splashScreen.update() );
		log.log( DEBUG, "Tool manager started." );

		// Create the theme manager
		log.log( TRACE, "Starting theme manager..." );
		themeManager = new ThemeManager( Program.this ).start();
		getSettingsManager().putOptionProvider( "workspace-theme-option-provider", new ThemeSettingOptionProvider( this ) );
		Platform.runLater( () -> splashScreen.update() );
		log.log( DEBUG, "Theme manager started." );

		// Create the workspace manager
		log.log( TRACE, "Starting workspace manager..." );
		workspaceManager = new WorkspaceManager( Program.this ).start();
		workspaceManager.setTheme( programSettings.get( "workspace-theme-id" ) );
		Platform.runLater( () -> splashScreen.update() );
		log.log( DEBUG, "Workspace manager started." );

		// Create the notice manager, depends on workspace manager
		log.log( TRACE, "Starting notice manager..." );
		noticeManager = new NoticeManager( Program.this ).start();
		Logger.getLogger( "" ).addHandler( new NoticeLogHandler( noticeManager ) );
		Platform.runLater( () -> splashScreen.update() );
		log.log( DEBUG, "Notice manager started." );

		// Start the product manager
		log.log( TRACE, "Starting product manager..." );
		productManager.start();
		productManager.startMods();
		updateManager = new UpdateManager( this );
		log.log( DEBUG, "Product manager started." );

		// Restore the user interface, depends on workspace manager
		log.log( TRACE, "Restore the user interface..." );
		Platform.runLater( () -> uiRegenerator.restore( splashScreen ) );
		uiRegenerator.awaitRestore( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.log( DEBUG, "User interface restored." );

		// Finish the splash screen
		int totalSteps = splashScreen.getSteps();
		int completedSteps = splashScreen.getCompletedSteps();
		if( completedSteps != totalSteps ) log.log( WARNING, "Startup step mismatch: " + completedSteps + " of " + totalSteps );
		Platform.runLater( () -> splashScreen.done() );

		// Give the slash screen time to render and the user to see it
		Thread.sleep( 500 );

		Platform.runLater( () -> {
			if( !parameters.isSet( ProgramFlag.DAEMON ) ) {
				getWorkspaceManager().getActiveStage().show();
				getWorkspaceManager().getActiveStage().toFront();
			}
			splashScreen.hide();
			time( "splash hidden" );
		} );

		// Initiate asset loading
		uiRegenerator.startAssetLoading();

		// Set the workarea actions
		getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaAction( Program.this ) );
		getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaAction( Program.this ) );
		getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaAction( Program.this ) );

		// Check to see if the application was updated
		if( isProgramUpdated() ) Platform.runLater( this::notifyProgramUpdated );

		// Open assets specified on the command line
		processAssets( getProgramParameters() );
	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doStartSuccess() {
		// Check for staged updates
		getProductManager().checkForStagedUpdatesAtStart();

		// Schedule the first update check, depends on productManager.checkForStagedUpdatesAtStart()
		getProductManager().scheduleUpdateCheck( true );

		// TODO Show user notifications
		//getTaskManager().submit( new ShowApplicationNotices() );
		new ProgramChecks( this );

		// Program started event should be fired after the window is shown
		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STARTED ) );
		time( "program started" );
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void stop() throws Exception {
		time( "stop" );

		// Submit and wait for the shutdown task to complete
		taskManager.submit( new ShutdownTask() ).get();
	}

	private class ShutdownTask extends Task<Void> {

		private ShutdownTask() {
			setPriority( Priority.HIGH );
		}

		@Override
		public Void call() {
			doShutdownTasks();
			return null;
		}

		@Override
		protected void success() {
			doStopSuccess();
		}

		@Override
		protected void cancelled() {
			log.log( ERROR, "Shutdown task cancelled", getException() );
		}

		@Override
		protected void failed() {
			log.log( ERROR, "Shutdown task failed", getException() );
		}

	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doShutdownTasks() {
		time( "do-shutdown-tasks" );

		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STOPPING ) );

		// Stop the product manager
		if( productManager != null ) {
			log.log( TRACE, "Stopping product manager..." );
			productManager.stopMods();
			productManager.stop();
			log.log( DEBUG, "Product manager stopped." );
		}

		// Stop the NoticeManager
		if( noticeManager != null ) {
			log.log( TRACE, "Stopping notice manager..." );
			noticeManager.stop();
			log.log( DEBUG, "Notice manager stopped." );
		}

		// Stop the workspace manager
		if( workspaceManager != null ) {
			log.log( TRACE, "Stopping workspace manager..." );
			workspaceManager.stop();
			log.log( DEBUG, "Workspace manager stopped." );
		}

		// Stop the theme manager
		if( themeManager != null ) {
			log.log( TRACE, "Stopping theme manager..." );
			themeManager.stop();
			log.log( DEBUG, "Theme manager stopped." );
		}

		// Stop the tool manager
		if( toolManager != null ) {
			log.log( TRACE, "Stopping tool manager..." );
			toolManager.stop();
			unregisterTools( toolManager );
			log.log( DEBUG, "Tool manager stopped." );
		}

		// NOTE Do not try to remove the settings pages during shutdown

		// Stop the asset manager
		if( assetManager != null ) {
			log.log( TRACE, "Stopping asset manager..." );
			assetManager.stop();
			unregisterAssetTypes( assetManager );
			unregisterSchemes( assetManager );
			log.log( DEBUG, "Asset manager stopped." );
		}

		// Disconnect the settings listener
		if( settingsManager != null ) {
			log.log( TRACE, "Stopping settings manager..." );
			settingsManager.stop();
			log.log( DEBUG, "Settings manager stopped." );
		}

		// Unregister action handlers
		if( actionLibrary != null ) unregisterActionHandlers();

		// Unregister the program
		if( productManager != null ) productManager.unregisterProgram( this );

		// Stop the program server
		if( programServer != null ) {
			log.log( TRACE, "Stopping program server..." );
			programServer.stop();
			log.log( DEBUG, "Program server stopped." );
		}

		// Stop the task manager
		if( taskManager != null ) {
			log.log( TRACE, "Stopping task manager..." );
			taskManager.stop();
			log.log( DEBUG, "Task manager stopped." );
		}

		// NOTE Do not call Platform.exit() here, it was called already
	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doStopSuccess() {
		// Do not add this as a shutdown hook, it hangs the JVM.
		new JvmSureStop( 5000 ).start();
		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STOPPED ) );

		// Unregister the event watcher
		getFxEventHub().unregister( Event.ANY, watcher );
	}

	public void requestRestart( String... commands ) {
		// Register a shutdown hook to restart the program
		ProgramShutdownHook programShutdownHook = new ProgramShutdownHook( this, ProgramShutdownHook.Mode.RESTART, commands );
		Runtime.getRuntime().addShutdownHook( programShutdownHook );

		// Request the program stop.
		if( !requestExit( true ) ) {
			Runtime.getRuntime().removeShutdownHook( programShutdownHook );
			return;
		}

		// The shutdown hook should restart the program
		log.log( INFO, "Restarting..." );
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	public void requestUpdate( ProgramShutdownHook.Mode mode, String... commands ) {
		// Register a shutdown hook to update the program
		//try {
		ProgramShutdownHook programShutdownHook = new ProgramShutdownHook( this, mode, commands );
		//		} catch( IOException exception ) {
		//			String title = rb().text( BundleKey.UPDATE, "updates" );
		//			String message = rb().text( BundleKey.UPDATE, "update-stage-failure" );
		//			getNoticeManager().addNotice( new Notice( title, message ) );
		//			return;
		//		}
		Runtime.getRuntime().addShutdownHook( programShutdownHook );

		// Request the program stop
		boolean exiting = requestExit( true );

		if( exiting ) {
			log.log( INFO, "Restarting to update..." );
			// The shutdown hook should update the program
		} else {
			Runtime.getRuntime().removeShutdownHook( programShutdownHook );
		}
	}

	public boolean requestExit( boolean skipChecks ) {
		return requestExit( skipChecks, skipChecks );
	}

	@SuppressWarnings( "ConstantConditions" )
	public boolean requestExit( boolean skipVerifyCheck, boolean skipKeepAliveCheck ) {
		if( workspaceManager != null && !workspaceManager.handleModifiedAssets( ProgramScope.PROGRAM, workspaceManager.getModifiedAssets() ) ) return false;

		boolean shutdownVerify = true;
		boolean shutdownKeepAlive = false;
		if( programSettings != null ) {
			shutdownVerify = programSettings.get( "shutdown-verify", Boolean.class, shutdownVerify );
			shutdownKeepAlive = programSettings.get( "shutdown-keepalive", Boolean.class, shutdownKeepAlive );
		}

		// If the user desires, prompt to exit the program
		if( !skipVerifyCheck && shutdownVerify ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( rb().text( BundleKey.PROGRAM, "program.close.title" ) );
			alert.setHeaderText( rb().text( BundleKey.PROGRAM, "program.close.message" ) );
			alert.setContentText( rb().text( BundleKey.PROGRAM, "program.close.prompt" ) );

			Stage stage = getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() != ButtonType.YES ) return false;
		}

		// The workspaceManager can be null if the program is already running as a peer
		if( workspaceManager != null ) Platform.runLater( () -> workspaceManager.hideWindows() );

		boolean exiting = !TestUtil.isTest() && (skipKeepAliveCheck || !shutdownKeepAlive);

		if( exiting ) Platform.exit();

		return exiting;
	}

	public boolean isRunning() {
		return taskManager.isRunning();
	}

	public boolean isUpdateInProgress() {
		return programSettings.get( "update-in-progress", Boolean.class, false );
	}

	public void setUpdateInProgress( boolean updateInProgress ) {
		programSettings.set( "update-in-progress", updateInProgress ).flush();
	}

	public com.avereon.util.Parameters getProgramParameters() {
		return parameters;
	}

	void setProgramParameters( com.avereon.util.Parameters parameters ) {
		this.parameters = parameters;
	}

	@Override
	public Program getProgram() {
		return this;
	}

	public String getProfile() {
		if( profile == null ) profile = parameters.get( ProgramFlag.PROFILE );
		return profile;
	}

	/**
	 * Get the home folder. If the home folder is null that means that the program is not installed locally and was most likely started with a technology like
	 * Java Web Start.
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
	public ProductBundle rb() {
		return programResourceBundle;
	}

	public final Path getDataFolder() {
		return programDataFolder;
	}

	public final Path getLogFolder() {
		return programLogFolder;
	}

	public final UpdateManager getUpdateManager() {
		return updateManager;
	}

	public final TaskManager getTaskManager() {
		return taskManager;
	}

	//	public final ExecutorService getExecutor() {
	//		return taskManager;
	//	}

	public final IconLibrary getIconLibrary() {
		return iconLibrary;
	}

	public final ActionLibrary getActionLibrary() {
		return actionLibrary;
	}

	public final SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public final Settings getProgramSettings() {
		return programSettings;
	}

	public final ToolManager getToolManager() {
		return toolManager;
	}

	public final AssetManager getAssetManager() {
		return assetManager;
	}

	public final ThemeManager getThemeManager() {
		return themeManager;
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

	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return fxEventHub.register( type, handler );
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return fxEventHub.unregister( type, handler );
	}

	FxEventHub getFxEventHub() {
		return fxEventHub;
	}

	private static void time( String markerName ) {
		if( !SHOW_TIMING ) return;
		long delta = System.currentTimeMillis() - programStartTime;
		System.err.println( "time=" + delta + " marker=" + markerName + " thread=" + Thread.currentThread().getName() );
	}

	/**
	 * Load the default settings map from the classpath.
	 *
	 * @return The default settings map
	 * @throws IOException If an IOException occurs
	 */
	private Map<String, Object> loadDefaultSettings() throws IOException {
		Properties properties = new Properties();
		Map<String, Object> defaultSettingsValues = new HashMap<>();
		InputStream defaultSettingsInput = getClassLoader().getResourceAsStream( SETTINGS_DEFAULT_PROPERTIES );
		if( defaultSettingsInput != null ) properties.load( new InputStreamReader( defaultSettingsInput, TextUtil.CHARSET ) );
		properties.forEach( ( k, v ) -> defaultSettingsValues.put( (String)k, v ) );
		return defaultSettingsValues;
	}

	/**
	 * Initialize the program parameters by converting the FX parameters object into a program parameters object.
	 *
	 * @return The program parameters object
	 */
	private com.avereon.util.Parameters initProgramParameters() {
		// The parameters may have been set in the constructor
		if( parameters != null ) return parameters;

		com.avereon.util.Parameters parameters;

		Parameters fxParameters = getParameters();
		if( fxParameters == null ) {
			parameters = com.avereon.util.Parameters.create();
		} else {
			parameters = com.avereon.util.Parameters.parse( fxParameters.getRaw() );
		}

		return parameters;
	}

	/**
	 * Check for another instance of the program is running after getting the
	 * settings but before the splash screen is shown. The fastest way to check is
	 * to try and bind to the port defined in the settings. The OS will quickly
	 * deny the bind if the port is already bound.
	 * <p>
	 * See: https://stackoverflow.com/questions/41051127/javafx-single-instance-application
	 * </p>
	 */
	private boolean peerCheck( int port ) {
		// If the program server starts this process is a host, not a peer
		programServer = new ProgramServer( this, port ).start();
		return !programServer.isRunning();
	}

	private boolean isHost() {
		return programServer.isRunning();
	}

	private boolean isPeer() {
		return !isHost();
	}

	/**
	 * Process program commands that affect the startup behavior of the product.
	 *
	 * @param parameters The command line parameters
	 * @return True if the program should continue to start when it is a host
	 */
	boolean processCliActions( com.avereon.util.Parameters parameters, boolean startup ) {
		if( parameters.isSet( ProgramFlag.HELLO ) ) {
			if( startup ) {
				log.log( WARNING, "No existing host to say hello to, just talking to myself!" );
			} else {
				log.log( WARNING, "Hello peer. Good to hear from you!" );
			}
			return false;
		} else if( parameters.isSet( ProgramFlag.STATUS ) ) {
			printStatus( startup );
			return false;
		} else if( parameters.isSet( ProgramFlag.STOP ) ) {
			if( startup ) {
				if( isHost() ) log.log( WARNING, "Program is already stopped!" );
			} else {
				if( isHost() ) Platform.runLater( () -> requestExit( true ) );
			}
			return false;
		} else if( parameters.isSet( ProgramFlag.WATCH ) ) {
			if( startup ) {
				log.log( WARNING, "No existing host to watch, I'm out!" );
			} else {
				log.log( WARNING, "A watcher has connected!" );
			}
			return false;
		} else if( !parameters.anySet( ProgramFlag.QUIET_ACTIONS ) ) {
			if( !startup ) getWorkspaceManager().showActiveWorkspace();
		}

		return true;
	}

	//	/**
	//	 * Process staged updates at startup unless the NOUPDATE flag is set. This
	//	 * situation happens if updates are staged and the updater was not run or did
	//	 * not run successfully. If there are no staged updates then the method
	//	 * returns false. If no updates were processed due to user input then the
	//	 * method also returns false.
	//	 *
	//	 * @return True if the program should be restarted, false otherwise.
	//	 */
	//	private boolean processStagedUpdates() {
	//		if( parameters.isSet( ProgramFlag.NOUPDATE ) ) return false;
	//		int result = productManager.updateProduct();
	//		if( result != 0 ) requestExit( true );
	//		return result != 0;
	//	}

	/**
	 * Process the assets specified on the command line.
	 *
	 * @param parameters The command line parameters
	 */
	void processAssets( com.avereon.util.Parameters parameters ) {
		List<String> uris = parameters.getUris();
		if( uris.size() == 0 ) return;

		getWorkspaceManager().showActiveWorkspace();

		// Open the assets provided on the command line
		try {
			getAssetManager().openAssetsAndWait( getAssetManager().createAssets( uris ) );
		} catch( ExecutionException | AssetException exception ) {
			log.log( WARNING, "Unable to open assets: " + uris );
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception
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

	private void printHeader( ProductCard card, com.avereon.util.Parameters parameters ) {
		String profile = getProfile();
		if( Profile.TEST.equals( profile ) ) return;

		boolean versionParameterSet = parameters.isSet( ProgramFlag.VERSION );
		String versionString = card.getVersion() + (profile == null ? "" : " [" + profile + "]");
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
		System.out.println( "OS name=" + System.getProperty( "os.name" ) + " version=" + System.getProperty( "os.version" ) + " arch=" + System.getProperty(
			"os.arch" ) );
	}

	private void printStatus( boolean startup ) {
		String status = startup ? "STOPPED" : "RUNNING";
		if( getWorkspaceManager() != null && !getWorkspaceManager().getActiveWorkspace().getStage().isShowing() ) status = "HIDDEN";
		log.log( INFO, "Status: " + status );
	}

	private void printHelp( String category ) {
		if( "true".equals( category ) ) category = "general";
		InputStream input = getClass().getResourceAsStream( "help/" + category + ".txt" );

		if( input == null ) {
			System.out.println( "No help for category: " + category );
			return;
		}

		try {
			String line;
			BufferedReader reader = new BufferedReader( new InputStreamReader( input, StandardCharsets.UTF_8 ) );
			while( (line = reader.readLine()) != null ) {
				System.out.println( line );
			}
		} catch( Exception exception ) {
			System.out.println( "Unable to get help for category: " + category );
		}
	}

	private String getProfileSuffix() {
		return profile == null ? "" : "-" + profile;
	}

	private Path configureDataFolder() {
		String suffix = getProfileSuffix();
		return OperatingSystem.getUserProgramDataFolder( card.getArtifact() + suffix, card.getName() + suffix );
	}

	private void configureLogging() {
		programLogFolder = getDataFolder().resolve( "logs" );
		Log.configureLogging( this, parameters, programLogFolder, "program.%u.log" );
		Log.setPackageLogLevel( "com.avereon", parameters.get( LogFlag.LOG_LEVEL, LogFlag.INFO ) );
		//Log.setPackageLogLevel( "javafx", parameters.get( LogFlag.LOG_LEVEL, LogFlag.WARN ) );
	}

	/**
	 * Find the home directory. This method expects the program jar file to be installed in a sub-directory of the home directory. Example:
	 * <code>$HOME/lib/program.jar</code>
	 *
	 * @param parameters The command line parameters
	 */
	private void configureHomeFolder( com.avereon.util.Parameters parameters ) {
		try {
			// If the HOME flag was specified on the command line use it
			if( programHomeFolder == null && parameters.isSet( ProgramFlag.HOME ) ) programHomeFolder = Paths.get( parameters.get( ProgramFlag.HOME ) );

			// Check the launcher path
			if( programHomeFolder == null ) programHomeFolder = getHomeFromLauncherPath();

			// When running as a linked (jlink) program, there is not a jdk.module.path system property.
			// The java home can be used as the program home when running as a linked application.
			if( programHomeFolder == null && System.getProperty( "jdk.module.path" ) == null ) {
				programHomeFolder = Paths.get( System.getProperty( "java.home" ) );
			}

			// However, when in development, don't use the java home
			if( programHomeFolder == null && Profile.DEV.equals( getProfile() ) ) programHomeFolder = Paths.get( "target/program" );

			// Use the user directory as a last resort (usually for unit tests)
			if( programHomeFolder == null ) programHomeFolder = Paths.get( System.getProperty( "user.dir" ) );

			// Canonicalize the home path
			programHomeFolder = programHomeFolder.toFile().getCanonicalFile().toPath();

			// Create the program home folder when in DEV mode
			if( Profile.DEV.equals( getProfile() ) ) Files.createDirectories( programHomeFolder );

			if( !Files.exists( programHomeFolder ) ) log.log( WARNING, "Program home folder does not exist: " + programHomeFolder );
		} catch( IOException exception ) {
			log.log( ERROR, "Error configuring home folder", exception );
		}

		// Set install folder on product card
		card.setInstallFolder( programHomeFolder );

		log.log( DEBUG, "Program home: " + getHomeFolder() );
		log.log( DEBUG, "Program data: " + getDataFolder() );
	}

	private Path getHomeFromLauncherPath() {
		return getHomeFromLauncherPath( System.getProperty( "java.launcher.path" ) );
	}

	private Path getHomeFromLauncherPath( String launcherPath ) {
		if( launcherPath == null ) return null;

		Path path = Paths.get( launcherPath );
		if( OperatingSystem.isWindows() ) {
			return path;
		} else if( OperatingSystem.isLinux() ) {
			return path.getParent();
		} else if( OperatingSystem.isMac() ) {
			// FIXME Unchecked, may be incorrect. Please review on actual platform.
			return path.getParent();
		}

		return null;
	}

	private void registerActionHandlers() {
		getActionLibrary().getAction( "workspace-close" ).pushAction( closeAction = new CloseWorkspaceAction( this ) );
		getActionLibrary().getAction( "exit" ).pushAction( exitAction = new ExitAction( this ) );
		getActionLibrary().getAction( "about" ).pushAction( aboutAction = new AboutAction( this ) );
		getActionLibrary().getAction( "settings" ).pushAction( settingsAction = new SettingsAction( this ) );
		getActionLibrary().getAction( "themes" ).pushAction( themesAction = new ThemesAction( this ) );
		getActionLibrary().getAction( "welcome" ).pushAction( welcomeAction = new WelcomeAction( this ) );
		getActionLibrary().getAction( "task" ).pushAction( taskAction = new TaskAction( this ) );
		getActionLibrary().getAction( "notice" ).pushAction( noticeAction = new NoticeAction( this ) );
		getActionLibrary().getAction( "product" ).pushAction( productAction = new ProductAction( this ) );
		getActionLibrary().getAction( "update" ).pushAction( updateAction = new UpdateAction( this ) );
		getActionLibrary().getAction( "mock-update" ).pushAction( mockUpdateAction = new MockUpdateAction( this ) );
		getActionLibrary().getAction( "restart" ).pushAction( restartAction = new RestartAction( this ) );
		getActionLibrary().getAction( "wallpaper-toggle" ).pushAction( wallpaperToggleAction = new WallpaperToggleAction( this ) );
		getActionLibrary().getAction( "wallpaper-prior" ).pushAction( wallpaperPriorAction = new WallpaperPriorAction( this ) );
		getActionLibrary().getAction( "wallpaper-next" ).pushAction( wallpaperNextAction = new WallpaperNextAction( this ) );

		getActionLibrary().getAction( "test-action-1" ).pushAction( new RunnableTestAction( this, () -> {
			log.log( Log.ERROR, new Throwable( "This is a test throwable" ) );
		} ) );
		getActionLibrary().getAction( "test-action-2" ).pushAction( new RunnableTestAction( this, () -> {
			this.getNoticeManager().warning( "Warning Title", "Warning message to user: %s", "mark" );
		} ) );
		getActionLibrary().getAction( "test-action-3" ).pushAction( new RunnableTestAction( this, () -> {
			this.getNoticeManager().addNotice( new Notice( "Testing", new Button( "Test Notice A" ) ) );
		} ) );
		getActionLibrary().getAction( "test-action-4" ).pushAction( new RunnableTestAction( this, () -> {
			//
		} ) );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "workspace-close" ).pullAction( closeAction );
		getActionLibrary().getAction( "exit" ).pullAction( exitAction );
		getActionLibrary().getAction( "about" ).pullAction( aboutAction );
		getActionLibrary().getAction( "settings" ).pullAction( settingsAction );
		getActionLibrary().getAction( "themes" ).pullAction( themesAction );
		getActionLibrary().getAction( "welcome" ).pullAction( welcomeAction );
		getActionLibrary().getAction( "task" ).pullAction( taskAction );
		getActionLibrary().getAction( "notice" ).pullAction( noticeAction );
		getActionLibrary().getAction( "product" ).pullAction( productAction );
		getActionLibrary().getAction( "update" ).pullAction( updateAction );
		getActionLibrary().getAction( "mock-update" ).pullAction( mockUpdateAction );
		getActionLibrary().getAction( "restart" ).pullAction( restartAction );
		getActionLibrary().getAction( "wallpaper-toggle" ).pullAction( wallpaperToggleAction );
		getActionLibrary().getAction( "wallpaper-prior" ).pullAction( wallpaperPriorAction );
		getActionLibrary().getAction( "wallpaper-next" ).pullAction( wallpaperNextAction );
	}

	private void registerSchemes( AssetManager manager ) {
		manager.addScheme( new NewScheme( this ) );
		manager.addScheme( new FaultScheme( this ) );
		manager.addScheme( new ProgramScheme( this ) );
		manager.addScheme( new FileScheme( this ) );
		manager.addScheme( new HttpsScheme( this ) );
		manager.addScheme( new HttpScheme( this ) );
	}

	private void unregisterSchemes( AssetManager manager ) {
		manager.removeScheme( HttpScheme.ID );
		manager.removeScheme( HttpsScheme.ID );
		manager.removeScheme( FileScheme.ID );
		manager.removeScheme( ProgramScheme.ID );
		manager.removeScheme( FaultScheme.ID );
		manager.removeScheme( NewScheme.ID );
	}

	private void registerAssetTypes( AssetManager manager ) {
		manager.addAssetType( new ProgramGuideType( this ) );
		manager.addAssetType( new ProgramAboutType( this ) );
		manager.addAssetType( new ProgramSettingsType( this ) );
		manager.addAssetType( new ProgramWelcomeType( this ) );
		manager.addAssetType( new ProgramNoticeType( this ) );
		manager.addAssetType( new ProgramProductType( this ) );
		manager.addAssetType( new ProgramTaskType( this ) );
		manager.addAssetType( new ProgramAssetNewType( this ) );
		manager.addAssetType( new ProgramAssetChooserType( this ) );
		manager.addAssetType( new ProgramThemesType( this ) );
		manager.addAssetType( new ProgramFaultType( this ) );
	}

	private void unregisterAssetTypes( AssetManager manager ) {
		manager.removeAssetType( new ProgramFaultType( this ) );
		manager.removeAssetType( new ProgramThemesType( this ) );
		manager.removeAssetType( new ProgramAssetChooserType( this ) );
		manager.removeAssetType( new ProgramAssetNewType( this ) );
		manager.removeAssetType( new ProgramTaskType( this ) );
		manager.removeAssetType( new ProgramProductType( this ) );
		manager.removeAssetType( new ProgramNoticeType( this ) );
		manager.removeAssetType( new ProgramWelcomeType( this ) );
		manager.removeAssetType( new ProgramSettingsType( this ) );
		manager.removeAssetType( new ProgramAboutType( this ) );
		manager.removeAssetType( new ProgramGuideType( this ) );
	}

	private void registerTools( ToolManager manager ) {
		registerTool( manager, new ProgramAboutType( this ), AboutTool.class, ToolInstanceMode.SINGLETON, "about", "about" );
		registerTool( manager, new ProgramGuideType( this ), GuideTool.class, ToolInstanceMode.SINGLETON, "guide", "guide" );
		registerTool( manager, new ProgramNoticeType( this ), NoticeTool.class, ToolInstanceMode.SINGLETON, "notice", "notice" );
		registerTool( manager, new ProgramProductType( this ), ProductTool.class, ToolInstanceMode.SINGLETON, "product", "product" );
		registerTool( manager, new ProgramSettingsType( this ), SettingsTool.class, ToolInstanceMode.SINGLETON, "settings", "settings" );
		registerTool( manager, new ProgramTaskType( this ), TaskTool.class, ToolInstanceMode.SINGLETON, "task", "task" );
		registerTool( manager, new ProgramWelcomeType( this ), WelcomeTool.class, ToolInstanceMode.SINGLETON, "welcome", "welcome" );
		registerTool( manager, new ProgramFaultType( this ), FaultTool.class, ToolInstanceMode.UNLIMITED, "fault", "fault" );
		registerTool( manager, new ProgramAssetNewType( this ), NewAssetTool.class, ToolInstanceMode.SINGLETON, "asset", "asset" );
		registerTool( manager, new ProgramAssetChooserType( this ), AssetTool.class, ToolInstanceMode.SINGLETON, "asset", "asset" );
		registerTool( manager, new ProgramThemesType( this ), ThemeTool.class, ToolInstanceMode.SINGLETON, "themes", "themes" );

		toolManager.addToolAlias( "com.avereon.xenon.tool.about.AboutTool", AboutTool.class );
		toolManager.addToolAlias( "com.avereon.xenon.tool.notice.NoticeTool", NoticeTool.class );
		toolManager.addToolAlias( "com.avereon.xenon.tool.task.TaskTool", TaskTool.class );
		toolManager.addToolAlias( "com.avereon.xenon.tool.welcome.WelcomeTool", WelcomeTool.class );
	}

	private void unregisterTools( ToolManager manager ) {
		unregisterTool( manager, new ProgramAssetChooserType( this ), AssetTool.class );
		unregisterTool( manager, new ProgramAssetNewType( this ), NewAssetTool.class );
		unregisterTool( manager, new ProgramFaultType( this ), FaultTool.class );
		unregisterTool( manager, new ProgramTaskType( this ), TaskTool.class );
		unregisterTool( manager, new ProgramProductType( this ), ProductTool.class );
		unregisterTool( manager, new ProgramWelcomeType( this ), WelcomeTool.class );
		unregisterTool( manager, new ProgramNoticeType( this ), NoticeTool.class );
		unregisterTool( manager, new ProgramSettingsType( this ), SettingsTool.class );
		unregisterTool( manager, new ProgramAboutType( this ), AboutTool.class );
		unregisterTool( manager, new ProgramGuideType( this ), GuideTool.class );
	}

	private void registerTool(
		ToolManager manager, AssetType assetType, Class<? extends ProgramTool> toolClass, ToolInstanceMode mode, String toolRbKey, String iconKey
	) {
		// The problem with using the class name is it can change if the class package or name is changed.
		AssetType type = assetManager.getAssetType( assetType.getKey() );
		String name = rb().text( "tool", toolRbKey + "-name" );
		Node icon = getIconLibrary().getIcon( iconKey );

		ToolRegistration metadata = new ToolRegistration( this, toolClass );
		metadata.setName( name ).setIcon( icon ).setInstanceMode( mode );
		manager.registerTool( type, metadata );
	}

	private void unregisterTool( ToolManager manager, AssetType assetType, Class<? extends ProgramTool> toolClass ) {
		manager.unregisterTool( assetManager.getAssetType( assetType.getKey() ), toolClass );
	}

	private SettingsManager configureSettingsManager( SettingsManager settingsManager ) {
		settingsManager.getEventBus().parent( fxEventHub );
		return settingsManager;
	}

	private TaskManager configureTaskManager( TaskManager taskManager ) {
		taskManager.getEventBus().parent( fxEventHub );
		return taskManager;
	}

	private ProductManager configureProductManager( ProductManager productManager ) throws IOException {
		productManager.getEventBus().parent( fxEventHub );

		// Register the provider repos
		productManager.registerProviderRepos( RepoState.forProduct( getClass() ) );

		// FIXME Do I want the update settings in the program settings?
		// There is also a set of comments regarding this issue in the ProductManager class
		productManager.setSettings( programSettings );

		// Register the product
		productManager.registerProgram( this );

		return productManager;
	}

//	String[] getUpdateCommands( com.avereon.util.Parameters parameters ) {
//		// Required to set values needed for:
//		// - the title of the progress window to have the product name
//		// - the updater to launch an elevated updater with the correct launcher name
//		// - the proper location for the log file
//		config();
//
//		log.log( Log.WARN, "Starting the update process!" );
//
//		// All the update commands should be in a file
//		Path updateCommandFile = Paths.get( parameters.get( ProgramFlag.UPDATE ), "" );
//		if( !Files.exists( updateCommandFile ) || !Files.isRegularFile( updateCommandFile ) ) {
//			log.log( Log.WARN, "Missing update command file: " + updateCommandFile );
//			throw new IllegalArgumentException( "Missing update command file: " + updateCommandFile );
//		}
//
//		// The progress window title
//		String updatingProgramText = rb().textOr( BundleKey.UPDATE, "updating", "Updating {0}", getCard().getName() );
//
//		// Force the location of the updater log file
//		String logFolder = PathUtil.getParent( Log.getLogFile() );
//		String logFile = PathUtil.resolve( logFolder, "update.%u.log" );
//
//		List<String> commands = new ArrayList<>();
//		commands.add( UpdateFlag.TITLE );
//		commands.add( updatingProgramText );
//		commands.add( UpdateFlag.FILE );
//		commands.add( parameters.get( ProgramFlag.UPDATE ) );
//		commands.add( ProgramFlag.LOG_FILE );
//		commands.add( logFile );
//		if( parameters.isSet( LogFlag.LOG_LEVEL ) ) {
//			commands.add( LogFlag.LOG_LEVEL );
//			commands.add( parameters.get( LogFlag.LOG_LEVEL ) );
//		}
//
//		return commands.toArray( new String[]{} );
//	}

	private void notifyProgramUpdated() {
		Release prior = Release.decode( programSettings.get( PROGRAM_RELEASE_PRIOR, (String)null ) );
		Release runtime = this.getCard().getRelease();
		String priorVersion = prior.getVersion().toHumanString();
		String runtimeVersion = runtime.getVersion().toHumanString();
		String title = rb().text( BundleKey.UPDATE, "updates" );
		String message = rb().text( BundleKey.UPDATE, "program-updated-message", priorVersion, runtimeVersion );
		getNoticeManager().addNotice( new Notice( title, message, () -> getProgram().getAssetManager().openAsset( ProgramAboutType.URI ) ).setRead( true ) );
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

}
