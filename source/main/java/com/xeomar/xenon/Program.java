package com.xeomar.xenon;

import com.xeomar.xenon.action.*;
import com.xeomar.xenon.event.ProgramStartedEvent;
import com.xeomar.xenon.event.ProgramStartingEvent;
import com.xeomar.xenon.event.ProgramStoppedEvent;
import com.xeomar.xenon.event.ProgramStoppingEvent;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.product.ProductBundle;
import com.xeomar.xenon.product.ProductMetadata;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.resource.type.ProgramAboutType;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import com.xeomar.xenon.resource.type.ProgramWelcomeType;
import com.xeomar.xenon.scheme.FileScheme;
import com.xeomar.xenon.scheme.ProgramScheme;
import com.xeomar.xenon.settings.ReadOnlySettings;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.task.TaskManager;
import com.xeomar.xenon.tool.AboutTool;
import com.xeomar.xenon.tool.GuideTool;
import com.xeomar.xenon.tool.WelcomeTool;
import com.xeomar.xenon.tool.settings.SettingsPage;
import com.xeomar.xenon.tool.settings.SettingsTool;
import com.xeomar.xenon.util.JavaUtil;
import com.xeomar.xenon.util.OperatingSystem;
import com.xeomar.xenon.workspace.ToolInstanceMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Program extends Application implements Product {

	public static final String STYLESHEET = "style.css";

	public static final String SETTINGS_EXTENSION = ".settings";

	private static final long MANAGER_ACTION_SECONDS = 10;

	private static Logger log = LogUtil.get( Program.class );

	private static long programStartTime = System.currentTimeMillis();

	private static long managerActionTime = 10;

	private SplashScreen splashScreen;

	private TaskManager taskManager;

	private ProductMetadata metadata;

	private File programDataFolder;

	private Settings programSettings;

	private IconLibrary iconLibrary;

	private ProductBundle productBundle;

	private ActionLibrary actionLibrary;

	private SettingsManager settingsManager;

	private Map<String, SettingsPage> settingsPages;

	private ToolManager toolManager;

	private ResourceManager resourceManager;

	private WorkspaceManager workspaceManager;

	private ProgramEventWatcher watcher;

	private ProgramNotifier notifier;

	private Set<ProgramEventListener> listeners;

	private ExitAction exitAction;

	private AboutAction aboutAction;

	private SettingsAction settingsAction;

	private WelcomeAction welcomeAction;

	public static void main( String[] commands ) {
		launch( commands );
	}

	public Program() {
		// Create program action handlers
		exitAction = new ExitAction( this );
		aboutAction = new AboutAction( this );
		settingsAction = new SettingsAction( this );
		welcomeAction = new WelcomeAction( this );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();
	}

	@Override
	public void init() throws Exception {
		time( "init" );

		// NOTE Only do in init() what has to be done before the splash screen can be shown

		// Load the product metadata
		metadata = new ProductMetadata();

		// Print the program header
		printHeader( metadata );

		// Configure logging
		LogUtil.configureLogging( getParameter( ProgramParameter.LOG_LEVEL ) );

		// Create the program event watcher after configuring the logging
		addEventListener( watcher = new ProgramEventWatcher() );

		// Fire the program starting event after the event watcher is created
		fireEvent( new ProgramStartingEvent( this ) );

		String prefix = getExecmodePrefix();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
		time( "programValues" );

		// FIXME Getting the program settings takes about 1/4 of the startup time
		// Create the settings manager before getting the program settings
		settingsManager = new SettingsManager( this ).start();

		// Get default settings map
		Properties properties = new Properties();
		properties.load( new InputStreamReader( getClass().getResourceAsStream( "/settings/default.properties" ), "utf-8" ) );

		// Get the program settings after the settings manager and before the task manager
		programSettings = settingsManager.getSettings( "program.properties" );
		programSettings.setDefaultSettings( new ReadOnlySettings( properties ) );
		time( "settings" );

		// TODO Check for another instance after getting the settings but before the splash screen is shown
		// https://stackoverflow.com/questions/41051127/javafx-single-instance-application
		// The fastest way to check might be to try and bind to the port defined in
		// the settings. The OS will quickly deny the bind.
		// Call Platform.exit() if there is already an instance

		// Create the program notifier after creating the program settings
		notifier = new ProgramNotifier( this );

		// Create the executor service
		log.trace( "Starting task manager..." );
		taskManager = new TaskManager();
		taskManager.loadSettings( programSettings );
		taskManager.start();
		taskManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "Task manager started." );
		time( "taskManager" );
	}

	@Override
	public void start( Stage stage ) throws Exception {
		// Show the splash screen
		splashScreen = new SplashScreen( metadata.getName() );
		splashScreen.show();
		time( "splash" );

		// Submit the startup task
		taskManager.submit( new Startup() );
	}

	@Override
	public void stop() throws Exception {
		taskManager.submit( new Shutdown() ).get();

		// Stop the task manager
		log.trace( "Stopping task manager..." );
		taskManager.stop();
		taskManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "Task manager stopped." );
	}

	public void requestExit() {
		requestExit( false );
	}

	public void requestExit( boolean force ) {
		// TODO If the user desires, prompt to exit the program
		if( !force ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.initOwner( getWorkspaceManager().getActiveWorkspace().getStage() );
			alert.setTitle( getResourceBundle().getString( "program", "program.close.title" ) );
			alert.setHeaderText( getResourceBundle().getString( "program", "program.close.message" ) );
			alert.setContentText( getResourceBundle().getString( "program", "program.close.prompt" ) );

			Optional<ButtonType> result = alert.showAndWait();
			if( result.isPresent() && result.get() != ButtonType.YES ) return;
		}

		if( !JavaUtil.isTest() ) Platform.exit();
	}

	public String getParameter( String key ) {
		Parameters parameters = getParameters();
		// WORKAROUND Parameters are null during testing due to Java 9 incompatibility
		if( parameters == null ) return System.getProperty( key );
		return parameters.getNamed().get( key );
	}

	@Override
	public Program getProgram() {
		return this;
	}

	@Override
	public ProductMetadata getMetadata() {
		return metadata;
	}

	@Override
	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	@Override
	public ProductBundle getResourceBundle() {
		return productBundle;
	}

	public long getStartTime() {
		return programStartTime;
	}

	public File getDataFolder() {
		return programDataFolder;
	}

	public ProgramNotifier getNotifier() {
		return notifier;
	}

	public ExecutorService getExecutor() {
		return taskManager;
	}

	public IconLibrary getIconLibrary() {
		return iconLibrary;
	}

	public ActionLibrary getActionLibrary() {
		return actionLibrary;
	}

	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public ToolManager getToolManager() {
		return toolManager;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public WorkspaceManager getWorkspaceManager() {
		return workspaceManager;
	}

	public void addEventListener( ProgramEventListener listener ) {
		this.listeners.add( listener );
	}

	public void removeEventListener( ProgramEventListener listener ) {
		this.listeners.remove( listener );
	}

	public void fireEvent( ProgramEvent event ) {
		event.fire( listeners );
	}

	protected void finalize() {
		removeEventListener( watcher );
	}

	private static void time( String markerName ) {
		//System.out.println( "Time " + markerName + "=" + (System.currentTimeMillis() - programStartTime) );
	}

	private void printHeader( ProductMetadata metadata ) {
		System.out.println( metadata.getName() + " " + metadata.getVersion() );
		System.out.println( "Java " + System.getProperty( "java.vm.version" ) );
	}

	private String getExecmodePrefix() {
		String prefix = "";

		String execmode = getParameter( ProgramParameter.EXECMODE );
		if( ProgramParameter.EXECMODE_DEVL.equals( execmode ) ) prefix = ExecMode.DEVL.getPrefix();
		if( ProgramParameter.EXECMODE_TEST.equals( execmode ) ) prefix = ExecMode.TEST.getPrefix();

		return prefix;
	}

	private void doStartupTasks() throws Exception {
		// Create the product bundle
		productBundle = new ProductBundle( getClass().getClassLoader() );

		// Create the icon library
		iconLibrary = new IconLibrary();
		registerIcons();

		// Create the action library
		actionLibrary = new ActionLibrary( productBundle, iconLibrary );
		registerActionHandlers();

		// Create the UI factory
		UiFactory factory = new UiFactory( Program.this );

		// Set the number of startup steps
		int managerCount = 5;
		int steps = managerCount + factory.getToolCount();
		Platform.runLater( () -> splashScreen.setSteps( steps ) );

		// Update the splash screen for the task manager which is already started
		Platform.runLater( () -> splashScreen.update() );

		// Update the product metadata
		metadata.loadContributors();
		Platform.runLater( () -> splashScreen.update() );

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
		settingsPages = getSettingsManager().addSettingsPages( this, programSettings, "/settings/pages.xml" );

		// Start the tool manager
		log.trace( "Starting tool manager..." );
		toolManager = new ToolManager( this );
		registerTools( toolManager );
		toolManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Tool manager started." );

		// Create the workspace manager
		log.trace( "Starting workspace manager..." );
		workspaceManager = new WorkspaceManager( Program.this ).start();
		workspaceManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		Platform.runLater( () -> factory.restoreUi( splashScreen ) );
		log.debug( "Workspace manager started." );

		// TODO Start the update manager

		// Finish the splash screen
		Platform.runLater( () -> splashScreen.done() );

		// Give the slash screen time to render and the user to see it
		Thread.sleep( 500 );
	}

	private void doShutdownTasks() {
		try {
			fireEvent( new ProgramStoppingEvent( this ) );

			// Stop the workspace manager
			log.trace( "Stopping workspace manager..." );
			workspaceManager.stop();
			workspaceManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			log.debug( "Workspace manager stopped." );

			// TODO Stop the UpdateManager

			// Stop the tool manager
			log.trace( "Stopping tool manager..." );
			toolManager.stop();
			toolManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			unregisterTools( toolManager );
			log.debug( "Tool manager stopped." );

			// Do not try to remove the settings pages during shutdown

			// Stop the resource manager
			log.trace( "Stopping resource manager..." );
			resourceManager.stop();
			resourceManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			unregisterResourceTypes( resourceManager );
			unregisterSchemes( resourceManager );
			log.debug( "Resource manager stopped." );

			// Disconnect the settings listener
			log.trace( "Stopping settings manager..." );
			settingsManager.stop();
			log.debug( "Settings manager stopped." );

			// Unregister action handlers
			unregisterActionHandlers();

			// Unregister icons
			unregisterIcons();

			// NOTE Do not call Platform.exit() here, it was called already
		} catch( InterruptedException exception ) {
			log.error( "Program shutdown interrupted", exception );
		}
	}

	private void registerIcons() {}

	private void unregisterIcons() {}

	private void registerActionHandlers() {
		getActionLibrary().getAction( "exit" ).pushAction( exitAction );
		getActionLibrary().getAction( "about" ).pushAction( aboutAction );
		getActionLibrary().getAction( "settings" ).pushAction( settingsAction );
		getActionLibrary().getAction( "welcome" ).pushAction( welcomeAction );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "exit" ).pullAction( exitAction );
		getActionLibrary().getAction( "about" ).pullAction( aboutAction );
		getActionLibrary().getAction( "settings" ).pullAction( settingsAction );
		getActionLibrary().getAction( "welcome" ).pullAction( welcomeAction );
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
	}

	private void unregisterResourceTypes( ResourceManager manager ) {
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
	}

	private void unregisterTools( ToolManager manager ) {
		unregisterTool( manager, ProgramWelcomeType.class, WelcomeTool.class );
		unregisterTool( manager, ProgramSettingsType.class, SettingsTool.class );
		unregisterTool( manager, ProgramAboutType.class, AboutTool.class );
		unregisterTool( manager, ProgramGuideType.class, GuideTool.class );
	}

	private void registerTool( ToolManager manager, Class<? extends ResourceType> resourceTypeClass, Class<? extends ProductTool> toolClass, ToolInstanceMode mode, String toolRbKey, String iconKey ) {
		ResourceType type = resourceManager.getResourceType( resourceTypeClass.getName() );
		String name = getResourceBundle().getString( "tool", toolRbKey );
		Node icon = getIconLibrary().getIcon( iconKey );

		ToolMetadata metadata = new ToolMetadata();
		metadata.setProduct( this ).setType( toolClass ).setInstanceMode( mode ).setName( name ).setIcon( icon );
		manager.registerTool( type, metadata );
	}

	private void unregisterTool( ToolManager manager, Class<? extends ResourceType> resourceTypeClass, Class<? extends ProductTool> toolClass ) {
		manager.unregisterTool( resourceManager.getResourceType( resourceTypeClass.getName() ), toolClass );
	}

//	private void registerSettingsPages() {
//		settingsPages = getSettingsManager().addSettingsPages( this, programSettings, "/settings/pages.xml" );
//	}
//
//	private void unregisterSettingsPages() {
//		//
//		//getSettingsManager().removeSettingsPages( settingsPages );
//	}

	private class Startup extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			doStartupTasks();
			return null;
		}

		@Override
		protected void succeeded() {
			splashScreen.hide();
			workspaceManager.getActiveWorkspace().getStage().show();

			// Program started event shoudl be fired after the window is shown
			Program.this.fireEvent( new ProgramStartedEvent( Program.this ) );

			// Set the workarea actions
			getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaAction( Program.this ) );
			getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaAction( Program.this ) );
			getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaAction( Program.this ) );
		}

		@Override
		protected void cancelled() {
			splashScreen.hide();
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
		protected Void call() throws Exception {
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
