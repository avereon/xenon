package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.action.*;
import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStartingEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppingEvent;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductBundle;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.resource.type.ProductInfoType;
import com.parallelsymmetry.essence.resource.type.ProgramSettingsType;
import com.parallelsymmetry.essence.scheme.FileScheme;
import com.parallelsymmetry.essence.scheme.ProgramScheme;
import com.parallelsymmetry.essence.settings.Settings;
import com.parallelsymmetry.essence.task.TaskManager;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

public class Program extends Application implements Product {

	public static final String STYLESHEET = "style.css";

	public static final String SETTINGS_EXTENSION = ".settings";

	private static final long MANAGER_ACTION_SECONDS = 10;

	private static Logger log = LogUtil.get( Program.class );

	private static long managerActionTime = 10;

	private static long programStartTime;

	private SplashScreen splashScreen;

	private TaskManager taskManager;

	private ProductMetadata metadata;

	private File programDataFolder;

	private Settings settings;

	private IconLibrary iconLibrary;

	private ProductBundle productBundle;

	private ActionLibrary actionLibrary;

	private SettingsManager settingsManager;

	private ToolManager toolManager;

	private ResourceManager resourceManager;

	private WorkspaceManager workspaceManager;

	private ProgramEventWatcher watcher;

	private Set<ProgramEventListener> listeners;

	private ExitAction exitAction;

	private AboutAction aboutAction;

	static {
		programStartTime = System.currentTimeMillis();

		// This will require Platform.exit() to be called
		Platform.setImplicitExit( false );
	}

	public static void main( String[] commands ) {
		//log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		// Create program action handlers
		exitAction = new ExitAction( this );
		aboutAction = new AboutAction( this );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();
	}

	@Override
	public void init() throws Exception {
		// NOTE Only do in init() what has to be done before the splash screen can be shown

		// Load the product metadata
		printHeader( metadata = new ProductMetadata() );
		time( "init" );

		configureLogging();
		log.info( "Program init time (ms): " + (System.currentTimeMillis() - programStartTime) );

		// Create the program event watcher after configuring the logging
		addEventListener( watcher = new ProgramEventWatcher() );

		// Fire the program starting event after the event watcher is created
		fireEvent( new ProgramStartingEvent( this ) );

		// FIXME Getting the program settings takes about 1/4 of the startup time
		settingsManager = new SettingsManager( this ).start();
		File programSettingsFolder = new File( programDataFolder, ProgramSettings.BASE );
		settings = settingsManager.getSettings( new File( programSettingsFolder, "program.properties" ) );
		time( "settings" );

		// TODO Check for another instance after getting the settings but before the splash screen is shown
		// https://stackoverflow.com/questions/41051127/javafx-single-instance-application
		// Call Platform.exit() if there is already an instance
	}

	@Override
	public void start( Stage stage ) throws Exception {
		// Show the splash screen as soon as possible after checking for another instance
		splashScreen = new SplashScreen( metadata.getName() );
		splashScreen.show();
		time( "splash" );

		String prefix = getExecmodePrefix();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
		time( "programValues" );

		// Create the executor service
		log.trace( "Starting task manager..." );
		taskManager = new TaskManager();
		// FIXME PLEASE taskManager.loadSettings( settings );
		taskManager.start();
		taskManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		log.debug( "Task manager started." );
		time( "taskManager" );

		// Submit the startup task
		taskManager.submit( new Startup() );
	}

	@Override
	public void stop() throws Exception {
		taskManager.submit( this::doShutdownTasks );
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

		Platform.exit();
	}

	@Override
	public ProductMetadata getMetadata() {
		return metadata;
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

	public ProgramEventWatcher getEventWatcher() {
		return watcher;
	}

	protected void finalize() {
		removeEventListener( watcher );
	}

	private static void time( String markerName ) {
		//System.out.println( "Time " + markerName + "=" + (System.currentTimeMillis() - programStartTime) );
	}

	private String getLogLevel() {
		String level = getParameter( ProgramParameter.LOG_LEVEL );
		if( level != null ) {
			try {
				level = level.toUpperCase();
				switch( level ) {
					case "ERROR": {
						level = "SEVERE";
						break;
					}
					case "WARN": {
						level = "WARNING";
						break;
					}
					case "DEBUG": {
						level = "FINE";
						break;
					}
					case "TRACE": {
						level = "FINEST";
						break;
					}
				}
			} catch( Exception exception ) {
				// Intentionally ignore exception
			}
		}
		return level;
	}

	private void configureLogging() {
		// The default logging configuration is in the logging.properties resource

		// SLF4J - Java Logging
		// ERROR - SEVERE
		// WARN  - WARNING
		// INFO  - INFO
		// DEBUG - FINE
		// TRACE - FINEST

		// Determine the log level

		StringBuilder builder = new StringBuilder();
		builder.append( "handlers=java.util.logging.ConsoleHandler\n" );
		// TODO Enable the log file handler
		//builder.append( "handlers=java.util.logging.ConsoleHandler,java.util.logging.FileHandler\n" );

		// Configure the simple formatter
		// https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax
		builder.append( "java.util.logging.SimpleFormatter.format=%1$tF %1$tT.%1$tL %4$s %5$s %6$s%n\n" );

		// Configure the console handler
		builder.append( "java.util.logging.ConsoleHandler.level=FINEST\n" );
		builder.append( "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\n" );

		// Configure the file handler
		builder.append( "java.util.logging.FileHandler.pattern=%h/java%u.log\n" );
		builder.append( "java.util.logging.FileHandler.limit=50000\n" );
		builder.append( "java.util.logging.FileHandler.count=1\n" );
		builder.append( "java.util.logging.FileHandler.formatter=java.util.logging.XMLFormatter\n" );

		// Set the default log level
		builder.append( ".level=INFO\n" );

		// Set the program log level
		String level = getLogLevel();
		if( level != null ) builder.append( getClass().getPackageName() + ".level=" + level + "\n" );

		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( "utf-8" ) );
			LogManager.getLogManager().readConfiguration( input );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
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

		// Start the tool manager
		log.trace( "Starting tool manager..." );
		toolManager = new ToolManager( this );
		toolManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Tool manager started." );

		// Start the resource manager
		log.trace( "Starting resource manager..." );
		resourceManager = new ResourceManager( Program.this );
		registerSchemes(resourceManager);
		registerResourceTypes(resourceManager);
		resourceManager.start();
		resourceManager.awaitStart( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Resource manager started." );

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
		//		long remaining = 1000 - (System.currentTimeMillis() - start);
		//		if( remaining > 0 ) Thread.sleep( remaining );
		time( "nap" );
		Thread.sleep( 1000 );
		time( "started" );
	}

	private void doShutdownTasks() {
		try {
			fireEvent( new ProgramStoppingEvent( this ) );

			// Stop the workspace manager
			log.trace( "Stopping workspace manager..." );
			// FIXME The program is exiting during this call
			workspaceManager.stop();
			workspaceManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			log.debug( "Workspace manager stopped." );

			// TODO Stop the UpdateManager

			// Stop the resource manager
			log.trace( "Stopping resource manager..." );
			resourceManager.stop();
			resourceManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			unregisterResourceTypes(resourceManager);
			unregisterSchemes(resourceManager);
			log.debug( "Resource manager stopped." );

			// Stop the tool manager
			log.trace( "Stopping tool manager..." );
			toolManager.stop();
			toolManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			log.debug( "Tool manager stopped." );

			// Disconnect the settings listener
			log.trace( "Stopping settings manager..." );
			settingsManager.stop();
			log.debug( "ProgramConfigurationBuilder manager stopped." );

			// Unregister action handlers
			unregisterActionHandlers();

			// Unregister icons
			unregisterIcons();

			// Stop the task manager
			log.trace( "Stopping task manager..." );
			taskManager.stop();
			taskManager.awaitStop( MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			log.debug( "Task manager stopped." );

			fireEvent( new ProgramStoppedEvent( this ) );

			// NOTE Apparently it is not necessary to call Platform.exit() here
		} catch( InterruptedException exception ) {
			log.error( "Program shutdown interrupted", exception );
		}
	}

	private void registerSchemes(ResourceManager manager) {
		manager.addScheme( new ProgramScheme( this ) );
		manager.addScheme( new FileScheme( this ) );
	}

	private void unregisterSchemes(ResourceManager manager) {
		manager.removeScheme( "program" );
		manager.removeScheme( "file" );
	}

	private void registerResourceTypes( ResourceManager manager) {
		manager.registerUriResourceType( "program:about", new ProductInfoType( this, "program" ) );
		manager.registerUriResourceType( "program:settings", new ProgramSettingsType( this, "program" ) );
	}

	private void unregisterResourceTypes(ResourceManager manager) {
		manager.unregisterUriResourceType( "program:about" );
		manager.unregisterUriResourceType( "program:settings" );
	}

	private void registerIcons() {}

	private void unregisterIcons() {}

	private void registerActionHandlers() {
		getActionLibrary().getAction( "exit" ).pushAction( exitAction );
		getActionLibrary().getAction( "about" ).pushAction( aboutAction );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "exit" ).pullAction( exitAction );
		getActionLibrary().getAction( "about" ).pullAction( aboutAction );
	}

	private String getParameter( String key ) {
		Parameters parameters = getParameters();
		if( parameters == null ) {
			// WORKAROUND Parameters are null during testing due to Java 9 incompatibility
			return System.getProperty( key );
		}
		return parameters.getNamed().get( key );
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

			workspaceManager.getActiveWorkspace().getStage().show();

			// Set the workarea actions
			getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaAction( Program.this ) );
			getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaAction( Program.this ) );
			getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaAction( Program.this ) );

			Program.this.fireEvent( new ProgramStartedEvent( Program.this ) );
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

}
