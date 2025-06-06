package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.index.Document;
import com.avereon.log.Log;
import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.product.Release;
import com.avereon.settings.Settings;
import com.avereon.util.*;
import com.avereon.xenon.action.*;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetManager;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.AssetWatchService;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.asset.type.*;
import com.avereon.xenon.index.IndexService;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticeLogHandler;
import com.avereon.xenon.notice.NoticeManager;
import com.avereon.xenon.product.ProductManager;
import com.avereon.xenon.scheme.*;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.throwable.ProgramUncaughtExceptionHandler;
import com.avereon.xenon.tool.*;
import com.avereon.xenon.tool.guide.GuideTool;
import com.avereon.xenon.tool.product.ProductTool;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingGroup;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.zerra.event.FxEventHub;
import com.avereon.zerra.javafx.Fx;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.CustomLog;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@CustomLog
public class Xenon extends Application implements XenonProgram {

	public static final String STYLESHEET = "xenon.css";

	public static final long MANAGER_ACTION_SECONDS = 10;

	public static final String DEFAULT_LOG_FILE_PATTERN = "program.%u.log";

	static final String PROGRAM_RELEASE = "product-release";

	static final String PROGRAM_RELEASE_PRIOR = "product-release-prior";

	private static final String DEFAULT_SETTINGS = "settings/default.properties";

	private static final String SETTINGS_PAGES = "settings/pages.xml";

	private static final boolean SHOW_TIMING = false;

	private static final int SPLASH_SCREEN_PAUSE_TIME_MS = 200;

	private static final long programStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

	private final ProgramUncaughtExceptionHandler uncaughtExceptionHandler;

	@Getter
	private final FxEventHub fxEventHub;

	private com.avereon.util.Parameters parameters;

	private boolean daemonRequested;

	private SplashScreenPane splashScreen;

	private TaskManager taskManager;

	private ProductCard card;

	private Path programHomeFolder;

	private Path programDataFolder;

	private Path programLogFolder;

	private Path programTempFolder;

	private UpdateManager updateManager;

	private String profile;

	private String mode;

	private IconLibrary iconLibrary;

	private ActionLibrary actionLibrary;

	private PeerServer peerServer;

	private SettingsManager settingsManager;

	private ToolManager toolManager;

	private AssetManager assetManager;

	private ThemeManager themeManager;

	private WorkspaceManager workspaceManager;

	private ProductManager productManager;

	private NoticeManager noticeManager;

	private IndexService indexService;

	private AssetWatchService assetWatchService;

	private ProgramEventWatcher watcher;

	private ActionMenuAction actionMenuAction;

	private CloseWorkspaceAction closeAction;

	private ExitAction exitAction;

	private AboutAction aboutAction;

	private HelpAction helpAction;

	private RestartAction restartAction;

	private UiResetAction uiResetAction;

	private SettingsAction settingsAction;

	private SettingsToggleAction settingsToggleAction;

	private PropertiesAction propertiesAction;

	private ThemesAction themesAction;

	private WelcomeAction welcomeAction;

	private NoticeAction noticeAction;

	private NoticeToggleAction noticeToggleAction;

	private SearchAction searchAction;

	private SearchToggleAction searchToggleAction;

	private ProductAction productAction;

	private SettingsAction modulesAction;

	private SettingsAction themeAction;

	private UpdateAction updateAction;

	private MockUpdateAction mockUpdateAction;

	private TaskAction taskAction;

	private WallpaperToggleAction wallpaperToggleAction;

	private WallpaperPriorAction wallpaperPriorAction;

	private WallpaperNextAction wallpaperNextAction;

	private WallpaperTintToggleAction wallpaperTintToggleAction;

	private Boolean isProgramUpdated;

	private RestartJob restartJob;

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	public Xenon() {
		time( "instantiate" );

		// Add the uncaught exception handler to the JavaFX Application Thread
		uncaughtExceptionHandler = new ProgramUncaughtExceptionHandler();
		Thread.currentThread().setUncaughtExceptionHandler( uncaughtExceptionHandler );

		// Create the product resource bundle
		Rb.init( this );
		time( "resource-bundle" );

		// Do not implicitly close the program
		Platform.setImplicitExit( false );
		time( "implicit-exit-false" );

		// Create the event hub
		fxEventHub = new FxEventHub();
		time( "fx-event-hub" );
	}

	// THREAD main
	// EXCEPTIONS Handled by the FX framework
	public static void launch( String[] commands ) {
		Application.launch( commands );
	}

	// THREAD JavaFX-Launcher
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void init() throws Exception {
		// NOTE Only do in init() what should be done before the splash screen is shown
		time( "init" );

		// Add the uncaught exception handler to the JavaFX-Launcher thread
		Thread.currentThread().setUncaughtExceptionHandler( uncaughtExceptionHandler );
		time( "uncaught-exception-handler" );

		// Init the product card
		card = XenonLauncherConfig.loadProductInfo();
		time( "card" );

		// Initialize the program parameters
		parameters = initProgramParameters();
		time( "parameters" );

		// Print the program header, depends on card and parameters
		printHeader( card, parameters );
		time( "print-header" );

		// Determine the program data folder, depends on program parameters
		configureDataFolder();
		time( "configure-data-folder" );

		// Configure logging, depends on parameters and program data folder
		configureLogging();
		time( "configure-logging" );

		// Get the memory setup
		long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;

		// Configure home folder, depends on logging
		configureHomeFolder( parameters );
		time( "configure-home-folder" );

		// Check for the VERSION CLI parameter, depends on product card
		if( getProgramParameters().isSet( XenonFlag.VERSION ) ) {
			printVersion( card );
			requestExit( true );
			return;
		} else {
			log.atDebug().log( "JVM Max Memory: %sMB", maxMemory );
			log.atDebug().log( "Parameters: %s", parameters );
			log.atDebug().log( "Program home: %s", getHomeFolder() );
			log.atDebug().log( "Program data: %s", getDataFolder() );
		}
		time( "version-check" );

		// Check for the HELP CLI parameter, depends on program parameters
		if( getProgramParameters().isSet( XenonFlag.HELP ) ) {
			printHelp( getProgramParameters().get( XenonFlag.HELP ) );
			requestExit( true );
			return;
		}
		time( "help-check" );

		// Create the settings manager, depends on program data folder, FX event hub
		settingsManager = new SettingsManager( this ).start();
		time( "settings-manager" );

		// Create the program settings, depends on settings manager and default settings values
		getSettings().loadDefaultValues( this, DEFAULT_SETTINGS );
		time( "program-settings" );

		// Run the peer check before processing actions in case there is a peer already
		// If this instance is a peer, start the peer and wait to exit
		int port = getSettings().get( "program-port", Integer.class, 0 );
		if( !TestUtil.isTest() && isHostAlreadyRunning( port ) ) {
			new ProgramPeer( this, port ).start();
			requestExit( true );
			return;
		}
		time( "peer-check" );

		// NOTE At this point this instance is a host not a peer

		// If this instance is a host, process the CLI actions before showing the splash screen
		if( processPeerCommands( getProgramParameters(), true ) ) {
			requestExit( true );
			return;
		}
		time( "cli-actions" );

		// NOTE The task manager is created in the init() method, so it is available during tests
		// Create the task manager, depends on program settings
		taskManager = new ProgramTaskManager( this ).start();
		time( "task-manager" );

		// NOTE The start( Stage ) method is called next
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void start( Stage stage ) {
		time( "fx-start" );
		if( !XenonMode.TEST.equals( mode ) && !isHardwareRendered() ) {
			log.atWarning().log( "Hardware rendering is disabled!" );
			log.atWarning().log( "  Consider adding -Dprism.forceGPU=true to the JVM parameters" );
		}

		// This must be set before the splash screen is shown
		Application.setUserAgentStylesheet( Application.STYLESHEET_MODENA );
		time( "stylesheet" );

		// Show the splash screen, depends stylesheet
		// NOTE If there is a test failure here it is because tests were run in the same VM
		if( stage.getStyle() != StageStyle.UNDECORATED ) stage.initStyle( StageStyle.UNDECORATED );
		daemonRequested = parameters.isSet( XenonFlag.DAEMON ) && !parameters.isSet( XenonFlag.NO_DAEMON );
		boolean nosplash = parameters.isSet( XenonFlag.NO_SPLASH );
		if( !daemonRequested && !nosplash ) {
			splashScreen = new SplashScreenPane( card.getName() );
			splashScreen.show( stage );
			time( "splash-displayed" );
		}

		// Submit, but do not wait for the startup task...allow the FX thread to be free
		getTaskManager().submit( new StartupTask() );
	}

	@Override
	public Xenon initForTesting( com.avereon.util.Parameters parameters ) throws Exception {
		setProgramParameters( parameters );
		init();
		iconLibrary = new IconLibrary( this );
		actionLibrary = new ActionLibrary( this );
		card = ProductCard.card( this );
		assetManager = new AssetManager( Xenon.this ).start();
		assetManager.getEventBus().parent( getFxEventHub() );
		registerSchemes( assetManager );
		registerAssetTypes( assetManager );
		toolManager = new ToolManager( this ).start();
		themeManager = new ThemeManager( Xenon.this ).start();
		workspaceManager = new WorkspaceManager( Xenon.this ).start();
		noticeManager = new NoticeManager( Xenon.this ).start();
		return this;
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
			if( splashScreen != null ) {
				Fx.run( () -> splashScreen.hide() );
			}
			log.atSevere().withCause( getException() ).log( "Startup task cancelled" );
			requestExit( true );
		}

		@Override
		protected void failed() {
			if( splashScreen != null ) {
				Fx.run( () -> splashScreen.hide() );
			}
			log.atSevere().withCause( getException() ).log( "Startup task failed" );
			Fx.run( () -> requestExit( true ) );
		}

	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doStartTasks() throws Exception {
		time( "do-startup-tasks" );

		// Reset the UI if the reset flag is set
		if( parameters.isSet( XenonFlag.RESET ) ) new UiFactory( this ).reset();

		// Create the program event watcher, depends on logging
		getFxEventHub().register( Event.ANY, watcher = new ProgramEventWatcher() );
		time( "event-hub" );

		// Fire the program starting event, depends on the event watcher
		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STARTING ) );
		time( "program-starting-event" );

		// Update the product card
		card = XenonLauncherConfig.loadProductCard();
		time( "update-product-card" );

		// Create the icon library
		iconLibrary = new IconLibrary( this );
		time( "icon-library" );

		// Create the action library
		actionLibrary = new ActionLibrary( this );
		registerActionHandlers();
		time( "program-actions" );

		// Set the number of startup steps
		int serviceCount = 8;
		if( splashScreen != null ) {
			Fx.run( () -> splashScreen.setExpectedSteps( serviceCount ) );
		}

		if( splashScreen != null ) splashScreen.update();

		// Start the asset manager
		log.atFiner().log( "Starting asset manager..." );
		assetManager = new AssetManager( Xenon.this ).start();
		registerSchemes( assetManager );
		registerAssetTypes( assetManager );
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Asset manager started." );
		time( "asset-manager" );

		// Start the asset watch service
		log.atFiner().log( "Starting asset watch service..." );
		assetWatchService = new AssetWatchService( Xenon.this ).start();
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Asset watch service started." );
		time( "asset-watch-service" );

		// Start the index service
		log.atFiner().log( "Starting index service..." );
		indexService = new IndexService( Xenon.this ).start();
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Index service started." );
		time( "index-service" );

		// Load the settings pages
		getSettingsManager().addSettingsPages( this, getSettings(), SETTINGS_PAGES );
		time( "settings-pages" );

		// Start the tool manager
		log.atFiner().log( "Starting tool manager..." );
		toolManager = new ToolManager( this ).start();
		registerTools( toolManager );
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Tool manager started." );
		time( "tool-manager" );

		// Create the theme manager
		log.atFiner().log( "Starting theme manager..." );
		themeManager = new ThemeManager( this ).start();
		getSettingsManager().putOptionProvider( "workspace-theme-option-provider", new ThemeSettingOptionProvider( this ) );
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Theme manager started." );
		time( "theme-manager" );

		// Create the workspace manager
		log.atFiner().log( "Starting workspace manager..." );
		workspaceManager = new WorkspaceManager( this ).start();
		workspaceManager.setTheme( getSettings().get( "workspace-theme-id" ) );
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Workspace manager started." );
		time( "workspace-manager" );

		// Create the notice manager, depends on workspace manager
		log.atFiner().log( "Starting notice manager..." );
		noticeManager = new NoticeManager( this ).start();
		Logger.getLogger( "" ).addHandler( new NoticeLogHandler( noticeManager ) );
		if( splashScreen != null ) splashScreen.update();
		log.atFine().log( "Notice manager started." );
		time( "notice-manager" );

		// Start the product manager, depends on icon library
		log.atFiner().log( "Starting product manager..." );
		productManager = new ProductManager( this ).start();
		log.atFine().log( "Product manager started." );
		time( "product-manager" );

		// Start the update manager, depends on product manager
		log.atFiner().log( "Starting update manager..." );
		updateManager = new UpdateManager( this );
		restartJob = new RestartJob( this );
		log.atFine().log( "Update manager started." );
		time( "update-manager" );

		// Start the mods, depends on product manager
		log.atFiner().log( "Starting mods..." );
		productManager.startMods();
		log.atFine().log( "Mods started." );
		time( "product-mods" );

		// Before restoring the UI, update the default tools
		log.atFiner().log( "Updating default tools..." );
		toolManager.updateDefaultToolsFromSettings();
		log.atFine().log( "Default tools updated." );
		time( "update-default-tools" );

		// Restore the user interface, depends on workspace manager, default tools
		log.atFiner().log( "Restore the user interface..." );
		UiReader uiReader = new UiReader( Xenon.this );
		uiReader.loadWorkspaces();
		uiReader.awaitLoadWorkspaces( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );

		log.atFine().log( "User interface restored." );
		time( "user-interface-restored" );

		// Finish the splash screen
		if( splashScreen != null ) {
			Fx.run( () -> splashScreen.done() );
			time( "splash-done" );

			// Give the slash screen time to render and the user to see it
			if( splashScreen.isVisible() ) Thread.sleep( SPLASH_SCREEN_PAUSE_TIME_MS );

			Fx.run( () -> splashScreen.hide() );
			time( "splash-hidden" );
		}

		// Set the workarea actions
		getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaAction( Xenon.this ) );
		getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaAction( Xenon.this ) );
		getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaAction( Xenon.this ) );

		// Show the active stage
		Stage activeStage = getWorkspaceManager().getActiveStage();
		if( !daemonRequested && activeStage != null ) {
			Fx.run( () -> {
				getWorkspaceManager().getActiveStage().show();
				getWorkspaceManager().getActiveStage().toFront();
				time( "workspace-visible" );
			} );
		}

		// Notify listeners the UI is ready
		getProgram().getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.UI_READY ) );

		// Register the program checks
		new ProgramChecks( this ).register();

		// Initiate asset loading
		uiReader.loadAssets();

		// Open assets specified on the command line
		processAssets( getProgramParameters() );
	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doStartSuccess() {
		time( "program-started" );

		// The program-started event should be fired after the window is shown
		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STARTED ) );

		// Check for staged updates
		getProductManager().checkForStagedUpdatesAtStart();

		// Schedule the first update check, depends on productManager.checkForStagedUpdatesAtStart()
		getProductManager().scheduleUpdateCheck( true );

		// TODO Show user notifications
		//getTaskManager().submit( new ShowApplicationNotices() );

		// Index program documents
		indexProgramDocuments();
	}

	private void indexProgramDocuments() {
		AboutTool about = new AboutTool( this, new Asset( ProgramAboutType.URI ) );
		String icon = "about";
		String name = Rb.text( RbKey.TOOL, "about-name" );
		getIndexService().submit( "about", new Document( ProgramAboutType.URI, icon, name, about.getIndexContent() ) );

		// TODO Index Help documents

		indexSettings();
	}

	private void indexSettings() {
		for( String id : getSettingsManager().getPageIds() ) {
			SettingsPage page = getSettingsManager().getSettingsPage( id );

			StringBuilder content = new StringBuilder();
			for( SettingGroup group : page.getGroups() ) {
				for( SettingData data : group.getSettingsList() ) {
					String label = Rb.text( page.getProduct(), RbKey.SETTINGS, data.getRbKey() );
					content.append( label ).append( "\n" );

					// TODO Don't forget settings tags
				}
			}

			StringBuilder path = new StringBuilder();
			SettingsPage p = page;
			while( p != null ) {
				path.insert( 0, p.getId() );
				path.insert( 0, "/" );
				p = p.getParent();
			}
			URI uri = URI.create( ProgramSettingsType.URI + path.toString() );

			//log.atConfig().log( "page uri=%s", uri );

			getIndexService().submit( "settings", new Document( uri, page.getIcon(), page.getTitle(), content.toString() ) );
		}
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void stop() throws Exception {
		time( "program-shutdown" );

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
			log.atSevere().withCause( getException() ).log( "Shutdown task cancelled" );
		}

		@Override
		protected void failed() {
			log.atSevere().withCause( getException() ).log( "Shutdown task failed" );
		}

	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doShutdownTasks() {
		time( "do-shutdown-tasks" );

		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STOPPING ) );

		// Stop the product manager
		if( productManager != null ) {
			log.atFiner().log( "Stopping product manager..." );
			productManager.stopMods();
			productManager.stop();
			log.atFine().log( "Product manager stopped." );
		}

		// Stop the NoticeManager
		if( noticeManager != null ) {
			log.atFiner().log( "Stopping notice manager..." );
			noticeManager.stop();
			log.atFine().log( "Notice manager stopped." );
		}

		// Stop the workspace manager
		if( workspaceManager != null ) {
			log.atFiner().log( "Stopping workspace manager..." );
			workspaceManager.stop();
			log.atFine().log( "Workspace manager stopped." );
		}

		// Stop the theme manager
		if( themeManager != null ) {
			log.atFiner().log( "Stopping theme manager..." );
			themeManager.stop();
			log.atFine().log( "Theme manager stopped." );
		}

		// Stop the tool manager
		if( toolManager != null ) {
			log.atFiner().log( "Stopping tool manager..." );
			toolManager.stop();
			unregisterTools( toolManager );
			log.atFine().log( "Tool manager stopped." );
		}

		// NOTE Do not try to remove the settings pages during shutdown

		// Stop the index service
		if( indexService != null ) {
			log.atFiner().log( "Stopping index service..." );
			indexService.stop();
			log.atFine().log( "Index service stopped." );
		}

		// Stop the file watch service
		if( assetWatchService != null ) {
			log.atFiner().log( "Stopping asset watch service..." );
			assetWatchService.stop();
			log.atFine().log( "Asset watch service stopped." );
		}

		// Stop the asset manager
		if( assetManager != null ) {
			log.atFiner().log( "Stopping asset manager..." );
			assetManager.stop();
			unregisterAssetTypes( assetManager );
			unregisterSchemes( assetManager );
			log.atFine().log( "Asset manager stopped." );
		}

		// Disconnect the settings listener
		if( settingsManager != null ) {
			log.atFiner().log( "Stopping settings manager..." );
			settingsManager.stop();
			log.atFine().log( "Settings manager stopped." );
		}

		// Unregister action handlers
		if( actionLibrary != null ) {
			unregisterActionHandlers();
		}

		// Unregister the program
		if( productManager != null ) {
			productManager.unregisterProgram( this );
		}

		// Stop the peer server
		if( peerServer != null ) {
			log.atFiner().log( "Stopping peer server..." );
			peerServer.stop();
			log.atFine().log( "Peer server stopped." );
		}

		// Stop the task manager
		if( taskManager != null ) {
			log.atFiner().log( "Stopping task manager..." );
			taskManager.stop();
			log.atFine().log( "Task manager stopped." );
		}

		// Start the restart job, if requested
		if( restartJob != null ) restartJob.start();

		// NOTE Do not call Platform.exit() here, it was called already
	}

	// THREAD TaskPool-worker
	// EXCEPTIONS Handled by the Task framework
	private void doStopSuccess() {
		// Do not add this as a shutdown hook, it hangs the JVM.
		new JvmSureStop( 5000 ).start();

		// Dispatch the program stopped event
		getFxEventHub().dispatch( new ProgramEvent( this, ProgramEvent.STOPPED ) );

		// Unregister the program event watcher
		//getFxEventHub().unregister( Event.ANY, watcher );
	}

	// THREAD JavaFX Application Thread
	// EXCEPTIONS Handled by the FX framework
	@Override
	public void requestRestart( RestartJob.Mode mode, String... commands ) {
		restartJob.setMode( mode, commands );
		requestExit( true );
	}

	@Override
	public boolean requestExit( boolean skipChecks ) {
		return requestExit( skipChecks, skipChecks );
	}

	@Override
	public boolean requestExit( boolean skipVerifyCheck, boolean skipKeepAliveCheck ) {
		if( workspaceManager != null && !workspaceManager.handleModifiedAssets( ProgramScope.PROGRAM, workspaceManager.getModifiedAssets() ) ) {
			return false;
		}

		boolean shutdownVerify = true;
		boolean shutdownKeepAlive = false;
		if( getSettings() != null ) {
			shutdownVerify = getSettings().get( "shutdown-verify", Boolean.class, shutdownVerify );
			shutdownKeepAlive = getSettings().get( "shutdown-keepalive", Boolean.class, shutdownKeepAlive );
		}

		// If the user desires, prompt to exit the program
		if( !skipVerifyCheck && shutdownVerify ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( Rb.text( RbKey.PROGRAM, "program.close.title" ) );
			alert.setHeaderText( Rb.text( RbKey.PROGRAM, "program.close.message" ) );
			alert.setContentText( Rb.text( RbKey.PROGRAM, "program.close.prompt" ) );

			Stage stage = getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() != ButtonType.YES ) {
				return false;
			}
		}

		// The workspaceManager can be null if the program is already running as a peer
		if( workspaceManager != null ) {
			Fx.run( () -> workspaceManager.hideWindows() );
		}

		boolean exiting = !TestUtil.isTest() && (skipKeepAliveCheck || !shutdownKeepAlive);

		// Shutdown the FX platform
		if( exiting ) Platform.exit();

		return exiting;
	}

	@Override
	public boolean isRunning() {
		return taskManager.isRunning();
	}

	@Override
	public boolean isHardwareRendered() {
		return Platform.isSupported( ConditionalFeature.SCENE3D );
	}

	@Override
	public boolean isUpdateInProgress() {
		return getSettings().get( "update-in-progress", Boolean.class, false );
	}

	@Override
	public void setUpdateInProgress( boolean updateInProgress ) {
		getSettings().set( "update-in-progress", updateInProgress ).flush();
	}

	@Override
	public com.avereon.util.Parameters getProgramParameters() {
		return parameters;
	}

	@Override
	public Xenon setProgramParameters( com.avereon.util.Parameters parameters ) {
		this.parameters = parameters;
		return this;
	}

	@Override
	public Xenon getProgram() {
		return this;
	}

	@Override
	public String getProfile() {
		if( profile == null ) profile = parameters.get( XenonFlag.PROFILE );
		return profile;
	}

	@Override
	public String getMode() {
		if( mode == null ) mode = parameters.get( XenonFlag.MODE );
		return mode;
	}

	private String getProfileMode() {
		String profile = getProfile();
		String mode = getMode();
		return combineProfileMode( profile, mode );
	}

	/**
	 * Get the profile and mode as a single string. If there is not a profile or
	 * mode, the empty string is returned.
	 *
	 * @return The profile and mode as a single string
	 */
	String combineProfileMode( String profile, String mode ) {
		String profileModeString;

		if( TextUtil.isEmpty( profile ) ) {
			if( TextUtil.isEmpty( mode ) ) {
				profileModeString = "";
			} else {
				profileModeString = mode;
			}
		} else {
			if( TextUtil.isEmpty( mode ) ) {
				profileModeString = profile;
			} else {
				profileModeString = profile + "-" + mode;
			}
		}

		return profileModeString;
	}

	@Override
	public Path getHomeFolder() {
		return programHomeFolder;
	}

	@Override
	public boolean isProgramUpdated() {
		if( isProgramUpdated == null ) isProgramUpdated = calcProgramUpdated();
		return isProgramUpdated;
	}

	@Override
	public ProductCard getCard() {
		return card;
	}

	/**
	 * The program data folder. See {@link #configureDataFolder()} for details.
	 *
	 * @return The program data folder
	 */
	@Override
	public Path getDataFolder() {
		return programDataFolder;
	}

	@Override
	public Path getLogFolder() {
		return programLogFolder;
	}

	@Override
	public Path getTempFolder() {
		return programTempFolder;
	}

	@Override
	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	@Override
	public TaskManager getTaskManager() {
		return taskManager;
	}

	//	public final ExecutorService getExecutor() {
	//		return taskManager;
	//	}

	@Override
	public IconLibrary getIconLibrary() {
		return iconLibrary;
	}

	@Override
	public ActionLibrary getActionLibrary() {
		return actionLibrary;
	}

	@Override
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	@Override
	public Settings getSettings() {
		return getSettingsManager().getSettings( ProgramSettings.PROGRAM );
	}

	@Override
	public ToolManager getToolManager() {
		return toolManager;
	}

	@Override
	public AssetManager getAssetManager() {
		return assetManager;
	}

	@Override
	public ThemeManager getThemeManager() {
		return themeManager;
	}

	@Override
	public WorkspaceManager getWorkspaceManager() {
		return workspaceManager;
	}

	@Override
	public ProductManager getProductManager() {
		return productManager;
	}

	@Override
	public NoticeManager getNoticeManager() {
		return noticeManager;
	}

	@Override
	public IndexService getIndexService() {
		return indexService;
	}

	public AssetWatchService getAssetWatchService() {
		return assetWatchService;
	}

	@Override
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return fxEventHub.register( type, handler );
	}

	@Override
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return fxEventHub.unregister( type, handler );
	}

	@Override
	public String toString() {
		return getCard().getName();
	}

	/**
	 * Simple method to show the number of milliseconds since the program started
	 * using a marker to denote where the time is taken.
	 *
	 * @param markerName The marker name
	 */
	private static void time( String markerName ) {
		if( !SHOW_TIMING ) return;
		long delta = System.currentTimeMillis() - programStartTime;
		System.err.println( "time=" + delta + " marker=" + markerName + " thread=" + Thread.currentThread().getName() );
	}

	/**
	 * Initialize the program parameters by converting the FX parameters object into a program parameters object.
	 *
	 * @return The program parameters object
	 */
	private com.avereon.util.Parameters initProgramParameters() {
		// The parameters may have been set in the constructor
		if( parameters != null ) {
			return parameters;
		}

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
	private boolean isHostAlreadyRunning( int port ) {
		// If the peer server starts this process is a host, not a peer
		if( peerServer == null ) peerServer = new PeerServer( this, port ).start();
		return isPeer();
	}

	private boolean isHost() {
		return peerServer.isRunning();
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
	boolean processPeerCommands( com.avereon.util.Parameters parameters, boolean startup ) {
		if( parameters.isSet( XenonFlag.HELLO ) ) {
			if( startup ) {
				log.atWarning().log( "No existing host to say hello to, just talking to myself!" );
			} else {
				log.atWarning().log( "Hello peer. Good to hear from you!" );
			}
			return true;
		} else if( parameters.isSet( XenonFlag.STATUS ) ) {
			printStatus( startup );
			return true;
		} else if( parameters.isSet( XenonFlag.STOP ) ) {
			if( startup ) {
				if( isHost() ) {
					log.atWarning().log( "Program is already stopped!" );
				}
			} else {
				if( isHost() ) {
					Fx.run( () -> requestExit( true ) );
				}
			}
			return true;
		} else if( parameters.isSet( XenonFlag.WATCH ) ) {
			if( startup ) {
				log.atWarning().log( "No existing host to watch, I'm out!" );
			} else {
				log.atWarning().log( "A watcher has connected!" );
			}
			return true;
		} else if( !parameters.anySet( XenonFlag.QUIET_ACTIONS ) ) {
			if( !startup ) getWorkspaceManager().showActiveWorkspace();
		}

		return false;
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
		if( uris.size() == 0 ) {
			return;
		}

		getWorkspaceManager().showActiveWorkspace();

		// Open the assets provided on the command line
		try {
			getAssetManager().openAssetsAndWait( getAssetManager().createAssets( uris ), 5, TimeUnit.SECONDS );
		} catch( AssetException | ExecutionException | TimeoutException exception ) {
			log.atWarning().log( "Unable to open assets: %s", uris );
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
		String mode = getMode();
		if( XenonMode.TEST.equals( mode ) ) return;

		String profileMode = getProfileMode();
		boolean versionParameterSet = parameters.isSet( XenonFlag.VERSION );
		String versionString = card.getVersion() + (TextUtil.isEmpty( profileMode ) ? "" : " [" + profileMode + "]");
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
		System.out.println( "Java locale=" + Locale.getDefault() + " encoding=" + Charset.defaultCharset().displayName() );
		System.out.println( "OS name=" + System.getProperty( "os.name" ) + " version=" + System.getProperty( "os.version" ) + " arch=" + System.getProperty( "os.arch" ) );
	}

	private void printStatus( boolean startup ) {
		String status = startup ? "STOPPED" : "RUNNING";
		if( getWorkspaceManager() != null && !getWorkspaceManager().getActiveWorkspace().isShowing() ) {
			status = "HIDDEN";
		}
		log.atInfo().log( "Status: %s", status );
	}

	private static void printHelp( String category ) {
		if( "true".equals( category ) ) category = "general";

		InputStream input = Xenon.class.getResourceAsStream( "help/" + category + ".txt" );

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

	private String getDataFolderSuffix() {
		String profileMode = getProfileMode();
		return TextUtil.isEmpty( profileMode ) ? "" : "-" + profileMode;
	}

	private void configureDataFolder() {
		String suffix = getDataFolderSuffix();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( card.getArtifact() + suffix, card.getName() + suffix );
		programTempFolder = programDataFolder.resolve( "temp" );
	}

	private void configureLogging() {
		programLogFolder = getDataFolder().resolve( "logs" );
		Log.configureLogging( this, parameters, programLogFolder, DEFAULT_LOG_FILE_PATTERN );
		Log.setPackageLogLevel( "com.avereon", parameters.get( LogFlag.LOG_LEVEL, LogFlag.INFO ) );
		//Log.setPackageLogLevel( "javafx", parameters.get( LogFlag.LOG_LEVEL, LogFlag.WARN ) );
	}

	/**
	 * Find the home directory. The home directory is resolved by using the first of:
	 * <ul>
	 *   <li>The HOME flag on the command line</li>
	 *   <li>The launcher path</li>
	 *   <li>The java home</li>
	 *   <li>The user directory</li>
	 *   <li>The current directory</li>
	 * </ul>
	 *
	 * @param parameters The command line parameters
	 */
	private void configureHomeFolder( com.avereon.util.Parameters parameters ) {
		try {
			// Check the command line HOME flag
			if( programHomeFolder == null && parameters.isSet( XenonFlag.HOME ) ) {
				programHomeFolder = Paths.get( parameters.get( XenonFlag.HOME ) );
			}

			// Check the launcher path
			if( programHomeFolder == null ) {
				programHomeFolder = getHomeFromLauncherPath();
			}

			// Check Java home when running as a linked (jlink) program
			// When running as a linked (jlink) program, there is not a jdk.module.path system property
			// The java home can be used as the program home when running as a linked application
			if( programHomeFolder == null && System.getProperty( "jdk.module.path" ) == null ) {
				programHomeFolder = Paths.get( System.getProperty( "java.home" ) );
			}

			// Use the user folder as a last resort (usually for unit tests)
			if( programHomeFolder == null ) {
				programHomeFolder = Paths.get( System.getProperty( "user.dir" ) );
			}

			// When in development mode, use the target/program folder
			if( XenonMode.DEV.equals( getMode() ) ) {
				programHomeFolder = Paths.get( "target/program" );
			}

			// Canonicalize the home path
			programHomeFolder = programHomeFolder.toFile().getCanonicalFile().toPath();

			// Set install folder on product card
			card.setInstallFolder( programHomeFolder );

			// Create the program home folder when in DEV mode
			if( XenonMode.DEV.equals( getMode() ) ) {
				Files.createDirectories( programHomeFolder );
			}

			if( !Files.exists( programHomeFolder ) ) {
				log.atWarning().log( "Program home folder does not exist: %s", programHomeFolder );
			}
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error configuring home folder" );
		}
	}

	@Override
	public Path getHomeFromLauncherPath() {
		return getHomeFromLauncherPath( OperatingSystem.getJavaLauncherPath(), OperatingSystem.isWindows() );
	}

	Path getHomeFromLauncherPath( String launcherPath, boolean isWindows ) {
		if( launcherPath == null ) return null;
		Path path = Paths.get( launcherPath ).getParent();
		return isWindows ? path : path.getParent();
	}

	private void registerActionHandlers() {
		getActionLibrary().getAction( "menu" ).pushAction( actionMenuAction = new ActionMenuAction( this ) );
		getActionLibrary().getAction( "workspace-close" ).pushAction( closeAction = new CloseWorkspaceAction( this ) );
		getActionLibrary().getAction( "exit" ).pushAction( exitAction = new ExitAction( this ) );
		getActionLibrary().getAction( "about" ).pushAction( aboutAction = new AboutAction( this ) );
		getActionLibrary().getAction( "help-content" ).pushAction( helpAction = new HelpAction( this ) );
		getActionLibrary().getAction( "settings" ).pushAction( settingsAction = new SettingsAction( this, "general" ) );
		getActionLibrary().getAction( "settings-toggle" ).pushAction( settingsToggleAction = new SettingsToggleAction( this ) );
		//getActionLibrary().getAction( "properties" ).pushAction( propertiesAction = new PropertiesAction( this ) );
		getActionLibrary().getAction( "themes" ).pushAction( themesAction = new ThemesAction( this ) );
		getActionLibrary().getAction( "welcome" ).pushAction( welcomeAction = new WelcomeAction( this ) );
		getActionLibrary().getAction( "task" ).pushAction( taskAction = new TaskAction( this ) );
		getActionLibrary().getAction( "notice" ).pushAction( noticeAction = new NoticeAction( this ) );
		getActionLibrary().getAction( "notice-toggle" ).pushAction( noticeToggleAction = new NoticeToggleAction( this ) );
		getActionLibrary().getAction( "search" ).pushAction( searchAction = new SearchAction( this ) );
		getActionLibrary().getAction( "search-toggle" ).pushAction( searchToggleAction = new SearchToggleAction( this ) );
		getActionLibrary().getAction( "product" ).pushAction( productAction = new ProductAction( this ) );
		getActionLibrary().getAction( "modules" ).pushAction( modulesAction = new SettingsAction( this, "modules" ) );
		getActionLibrary().getAction( "theme" ).pushAction( themeAction = new SettingsAction( this, "workspace-theme" ) );
		getActionLibrary().getAction( "update" ).pushAction( updateAction = new UpdateAction( this ) );
		getActionLibrary().getAction( "restart" ).pushAction( restartAction = new RestartAction( this ) );

		getActionLibrary().getAction( "wallpaper-toggle" ).pushAction( wallpaperToggleAction = new WallpaperToggleAction( this ) );
		getActionLibrary().getAction( "wallpaper-prior" ).pushAction( wallpaperPriorAction = new WallpaperPriorAction( this ) );
		getActionLibrary().getAction( "wallpaper-next" ).pushAction( wallpaperNextAction = new WallpaperNextAction( this ) );
		getActionLibrary().getAction( "wallpaper-tint-toggle" ).pushAction( wallpaperTintToggleAction = new WallpaperTintToggleAction( this ) );

		getActionLibrary().getAction( "show-updates-posted" ).pushAction( new RunnableTestAction( this, () -> getProductManager().showPostedUpdates() ) );
		getActionLibrary().getAction( "show-updates-staged" ).pushAction( new RunnableTestAction( this, () -> getProductManager().showStagedUpdates() ) );

		getActionLibrary().getAction( "mock-update" ).pushAction( mockUpdateAction = new MockUpdateAction( this ) );
		getActionLibrary().getAction( "uireset" ).pushAction( uiResetAction = new UiResetAction( this ) );
		getActionLibrary().getAction( "test-action-1" ).pushAction( new RunnableTestAction( this, () -> log.atSevere().withCause( new Throwable( "This is a test throwable" ) ).log() ) );
		getActionLibrary().getAction( "test-action-2" ).pushAction( new RunnableTestAction( this, () -> this.getNoticeManager().warning( "Warning Title", "Warning message to user: %s", "mark" ) ) );
		getActionLibrary().getAction( "test-action-3" ).pushAction( new RunnableTestAction( this, () -> this.getNoticeManager().addNotice( new Notice( "Testing", new Button( "Test Notice A" ) ) ) ) );
		getActionLibrary().getAction( "test-action-4" ).pushAction( new RunnableTestAction( this, () -> {} ) );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "menu" ).pullAction( actionMenuAction );
		getActionLibrary().getAction( "workspace-close" ).pullAction( closeAction );
		getActionLibrary().getAction( "exit" ).pullAction( exitAction );
		getActionLibrary().getAction( "about" ).pullAction( aboutAction );
		getActionLibrary().getAction( "help-content" ).pullAction( helpAction );
		getActionLibrary().getAction( "settings" ).pullAction( settingsAction );
		getActionLibrary().getAction( "settings-toggle" ).pullAction( settingsToggleAction );
		//getActionLibrary().getAction( "properties" ).pullAction( propertiesAction );
		getActionLibrary().getAction( "themes" ).pullAction( themesAction );
		getActionLibrary().getAction( "welcome" ).pullAction( welcomeAction );
		getActionLibrary().getAction( "task" ).pullAction( taskAction );
		getActionLibrary().getAction( "notice" ).pullAction( noticeAction );
		getActionLibrary().getAction( "notice-toggle" ).pullAction( noticeToggleAction );
		getActionLibrary().getAction( "search" ).pullAction( searchAction );
		getActionLibrary().getAction( "search-toggle" ).pullAction( searchToggleAction );
		getActionLibrary().getAction( "product" ).pullAction( productAction );
		getActionLibrary().getAction( "update" ).pullAction( updateAction );
		getActionLibrary().getAction( "mock-update" ).pullAction( mockUpdateAction );
		getActionLibrary().getAction( "uireset" ).pullAction( uiResetAction );
		getActionLibrary().getAction( "restart" ).pullAction( restartAction );
		getActionLibrary().getAction( "wallpaper-toggle" ).pullAction( wallpaperToggleAction );
		getActionLibrary().getAction( "wallpaper-prior" ).pullAction( wallpaperPriorAction );
		getActionLibrary().getAction( "wallpaper-next" ).pullAction( wallpaperNextAction );
		getActionLibrary().getAction( "wallpaper-tint-toggle" ).pullAction( wallpaperTintToggleAction );
	}

	private void registerSchemes( AssetManager manager ) {
		manager.addScheme( new NewScheme( this ) );
		manager.addScheme( new FaultScheme( this ) );
		manager.addScheme( new XenonScheme( this ) );
		//manager.addScheme( new ProgramHelpScheme( this ) );
		manager.addScheme( new FileScheme( this ) );
		manager.addScheme( new HttpsScheme( this ) );
		manager.addScheme( new HttpScheme( this ) );
	}

	private void unregisterSchemes( AssetManager manager ) {
		manager.removeScheme( HttpScheme.ID );
		manager.removeScheme( HttpsScheme.ID );
		manager.removeScheme( FileScheme.ID );
		//manager.removeScheme( ProgramHelpScheme.ID );
		manager.removeScheme( XenonScheme.ID );
		manager.removeScheme( FaultScheme.ID );
		manager.removeScheme( NewScheme.ID );
	}

	private void registerAssetTypes( AssetManager manager ) {
		manager.addAssetType( new ProgramGuideType( this ) );
		manager.addAssetType( new ProgramAboutType( this ) );
		manager.addAssetType( new ProgramSettingsType( this ) );
		manager.addAssetType( new ProgramWelcomeType( this ) );
		manager.addAssetType( new ProgramNoticeType( this ) );
		manager.addAssetType( new ProgramSearchType( this ) );
		manager.addAssetType( new ProgramHelpType( this ) );
		manager.addAssetType( new ProgramModuleType( this ) );
		manager.addAssetType( new ProgramTaskType( this ) );
		manager.addAssetType( new ProgramAssetNewType( this ) );
		manager.addAssetType( new ProgramAssetType( this ) );
		manager.addAssetType( new ProgramThemesType( this ) );
		manager.addAssetType( new ProgramFaultType( this ) );
		manager.addAssetType( new ProgramPropertiesType( this ) );

		registerProgramAssetAliases( manager );
	}

	private void registerProgramAssetAliases( AssetManager manager ) {
		// This is a reflection way of going through all the current program asset types
		List<String> programAliases = List.of( "about", "asset", "fault", "guide", "help", "new", "notice", "properties", "search", "settings", "task", "welcome" );
		for( String alias : programAliases ) {
			URI aliasUri = URI.create( "program:/" + alias );
			char[] targetChars = alias.toCharArray();
			targetChars[ 0 ] = Character.toUpperCase( targetChars[ 0 ] );
			String targetClassName = "Program" + new String( targetChars ) + "Type";
			try {
				Class<?> targetClass = Class.forName( AssetType.class.getPackageName() + ".type." + targetClassName );
				Field uriField = targetClass.getField( "URI" );
				URI targetUri = (URI)uriField.get( null );
				manager.registerAssetAlias( aliasUri, targetUri );
			} catch( ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore ) {
				// Intentionally ignore exception
			}
		}
	}

	private void unregisterAssetTypes( AssetManager manager ) {
		manager.removeAssetType( new ProgramPropertiesType( this ) );
		manager.removeAssetType( new ProgramFaultType( this ) );
		manager.removeAssetType( new ProgramThemesType( this ) );
		manager.removeAssetType( new ProgramAssetType( this ) );
		manager.removeAssetType( new ProgramAssetNewType( this ) );
		manager.removeAssetType( new ProgramTaskType( this ) );
		manager.removeAssetType( new ProgramModuleType( this ) );
		manager.removeAssetType( new ProgramHelpType( this ) );
		manager.removeAssetType( new ProgramSearchType( this ) );
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
		registerTool( manager, new ProgramSearchType( this ), SearchTool.class, ToolInstanceMode.SINGLETON, "search", "search" );
		registerTool( manager, new ProgramModuleType( this ), SettingsTool.class, ToolInstanceMode.SINGLETON, "product", "product" );
		registerTool( manager, new ProgramSettingsType( this ), SettingsTool.class, ToolInstanceMode.SINGLETON, "settings", "settings" );
		registerTool( manager, new ProgramTaskType( this ), TaskTool.class, ToolInstanceMode.SINGLETON, "task", "task" );
		registerTool( manager, new ProgramWelcomeType( this ), WelcomeTool.class, ToolInstanceMode.SINGLETON, "welcome", "welcome" );
		registerTool( manager, new ProgramFaultType( this ), FaultTool.class, ToolInstanceMode.UNLIMITED, "fault", "fault" );
		registerTool( manager, new ProgramAssetNewType( this ), NewAssetTool.class, ToolInstanceMode.SINGLETON, "asset", "asset" );
		registerTool( manager, new ProgramAssetType( this ), AssetTool.class, ToolInstanceMode.SINGLETON, "asset", "asset" );
		registerTool( manager, new ProgramThemesType( this ), ThemeTool.class, ToolInstanceMode.SINGLETON, "themes", "themes" );
		registerTool( manager, new ProgramHelpType( this ), HelpTool.class, ToolInstanceMode.UNLIMITED, "help", "help" );
		registerTool( manager, new ProgramPropertiesType( this ), PropertiesTool.class, ToolInstanceMode.SINGLETON, "properties", "properties" );

		toolManager.addToolAlias( "com.avereon.xenon.tool.about.AboutTool", AboutTool.class );
		toolManager.addToolAlias( "com.avereon.xenon.tool.notice.NoticeTool", NoticeTool.class );
		toolManager.addToolAlias( "com.avereon.xenon.tool.task.TaskTool", TaskTool.class );
		toolManager.addToolAlias( "com.avereon.xenon.tool.welcome.WelcomeTool", WelcomeTool.class );
	}

	private void unregisterTools( ToolManager manager ) {
		unregisterTool( manager, new ProgramPropertiesType( this ), PropertiesTool.class );
		unregisterTool( manager, new ProgramHelpType( this ), HelpTool.class );
		unregisterTool( manager, new ProgramAssetType( this ), AssetTool.class );
		unregisterTool( manager, new ProgramAssetNewType( this ), NewAssetTool.class );
		unregisterTool( manager, new ProgramFaultType( this ), FaultTool.class );
		unregisterTool( manager, new ProgramTaskType( this ), TaskTool.class );
		unregisterTool( manager, new ProgramModuleType( this ), ProductTool.class );
		unregisterTool( manager, new ProgramWelcomeType( this ), WelcomeTool.class );
		unregisterTool( manager, new ProgramSearchType( this ), SearchTool.class );
		unregisterTool( manager, new ProgramNoticeType( this ), NoticeTool.class );
		unregisterTool( manager, new ProgramSettingsType( this ), SettingsTool.class );
		unregisterTool( manager, new ProgramAboutType( this ), AboutTool.class );
		unregisterTool( manager, new ProgramGuideType( this ), GuideTool.class );
	}

	private void registerTool( ToolManager manager, AssetType assetType, Class<? extends ProgramTool> toolClass, ToolInstanceMode mode, String toolRbKey, String iconKey ) {
		// The problem with using the class name is it can change if the class package or name is changed.
		AssetType type = assetManager.getAssetType( assetType.getKey() );
		String name = Rb.text( "tool", toolRbKey + "-name" );
		Node icon = getIconLibrary().getIcon( iconKey );

		ToolRegistration metadata = new ToolRegistration( this, toolClass );
		metadata.setName( name ).setIcon( icon ).setInstanceMode( mode );
		manager.registerTool( type, metadata );
	}

	private void unregisterTool( ToolManager manager, AssetType assetType, Class<? extends ProgramTool> toolClass ) {
		manager.unregisterTool( assetManager.getAssetType( assetType.getKey() ), toolClass );
	}

	private boolean calcProgramUpdated() {
		// Get the last release setting
		Release previous = Release.decode( getSettings().get( PROGRAM_RELEASE, (String)null ) );
		Release runtime = this.getCard().getRelease();

		boolean programUpdated = previous != null && runtime.compareTo( previous ) > 0;

		if( programUpdated ) getSettings().set( PROGRAM_RELEASE_PRIOR, Release.encode( previous ) );
		getSettings().set( PROGRAM_RELEASE, Release.encode( runtime ) );

		return programUpdated;
	}

}
