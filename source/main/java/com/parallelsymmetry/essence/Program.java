package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.action.*;
import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStartingEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppingEvent;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductBundle;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.resource.ProgramResourceType;
import com.parallelsymmetry.essence.scheme.FileScheme;
import com.parallelsymmetry.essence.scheme.ProgramScheme;
import com.parallelsymmetry.essence.scheme.Schemes;
import com.parallelsymmetry.essence.task.TaskManager;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.logging.LogManager;

public class Program extends Application implements Product {

	public static final String STYLESHEET = "style.css";

	public static final String SETTINGS_EXTENSION = ".settings";

	private Logger log = LoggerFactory.getLogger( Program.class );

	private long startTimestamp;

	private String programTitle;

	private SplashScreen splashScreen;

	private ExecutorService executor;

	private ProductMetadata metadata;

	private File programDataFolder;

	private Settings settings;

	private IconLibrary iconLibrary;

	private ProductBundle productBundle;

	private ActionLibrary actionLibrary;

	private ToolManager toolManager;

	private ResourceManager resourceManager;

	private WorkspaceManager workspaceManager;

	private ProgramEventWatcher watcher;

	private Set<ProgramEventListener> listeners;

	private ExitAction exitAction;

	private AboutAction aboutAction;

	static {
		// This will require Platform.exit() to be called
		//Platform.setImplicitExit( false );
	}

	public static void main( String[] commands ) {
		//log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		startTimestamp = System.currentTimeMillis();

		// Create program action handlers
		exitAction = new ExitAction( this );
		aboutAction = new AboutAction( this );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();

		// Create the event watcher
		addEventListener( watcher = new ProgramEventWatcher() );
	}

	protected void finalize() {
		removeEventListener( watcher );
	}

	@Override
	public void init() throws Exception {
		configureLogging();

		// Load the product metadata.
		metadata = new ProductMetadata();

		// Determine execmode prefix
		String prefix = getExecmodePrefix();

		// Set program values
		programTitle = metadata.getName();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );

		// Create the executor service
		log.trace( "Starting executor service..." );
		TaskManager taskManager = new TaskManager();
		Settings taskManagerSettings = new Settings( taskManager, new File( programDataFolder, "program" + SETTINGS_EXTENSION ) );
		taskManager.loadSettings( taskManagerSettings.getConfiguration() );
		//executor = Executors.newFixedThreadPool( Math.max( 2, processorCount ), new ProgramThreadFactory() );
		executor = taskManager;
		log.debug( "Executor service started." );
	}

	@Override
	public void start( Stage stage ) throws Exception {
		printHeader();

		log.info( "Program init time (ms): " + (System.currentTimeMillis() - startTimestamp) );

		new ProgramStartingEvent( this ).fire( listeners );

		// Show the splash screen
		splashScreen = new SplashScreen( programTitle );
		splashScreen.show();

		// Submit the startup task
		executor.submit( new Startup() );
	}

	@Override
	public void stop() throws Exception {
		new ProgramStoppingEvent( this ).fire( listeners );

		// Submit the shutdown task
		executor.submit( this::doShutdownTasks );
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
		return startTimestamp;
	}

	public File getDataFolder() {
		return programDataFolder;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public IconLibrary getIconLibrary() {
		return iconLibrary;
	}

	public ActionLibrary getActionLibrary() {
		return actionLibrary;
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

	public void fireEvent( ProgramEvent event ) {
		event.fire( listeners );
	}

	public void addEventListener( ProgramEventListener listener ) {
		this.listeners.add( listener );
	}

	public void removeEventListener( ProgramEventListener listener ) {
		this.listeners.remove( listener );
	}

	public ProgramEventWatcher getEventWatcher() {
		return watcher;
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
		// WARN - WARNING
		// INFO - INFO
		// DEBUG - FINE
		// TRACE - FINEST

		// Determine the log level

		StringBuilder builder = new StringBuilder();
		builder.append( "handlers=java.util.logging.ConsoleHandler\n" );
		// TODO Enable the log file handler
		//builder.append( "handlers=java.util.logging.ConsoleHandler,java.util.logging.FileHandler\n" );

		// Configure the simple formatter
		builder.append( "java.util.logging.SimpleFormatter.format=%1$tF %1$tT %4$s %2$s %5$s %6$s%n\n" );

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

	private void printHeader() {
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
		// Give the slash screen time to render and the user to see it
		//Thread.sleep( 1000 );

		// Create the product bundle
		productBundle = new ProductBundle( getClass().getClassLoader() );

		// Create the icon library
		iconLibrary = new IconLibrary();
		registerIcons();

		// Create the action library
		actionLibrary = new ActionLibrary( productBundle, iconLibrary );
		registerActionHandlers();

		// Register schemes
		Schemes.addScheme( new FileScheme( this ) );
		Schemes.addScheme( new ProgramScheme( this ), new ProgramResourceType( this, "program" ) );

		// Create the workspace manager
		log.trace( "Starting workspace manager..." );
		workspaceManager = new WorkspaceManager( Program.this );
		log.debug( "Workspace manager started." );

		// Create the UI factory
		UiFactory factory = new UiFactory( Program.this );

		// Set the number of startup steps
		final int startupCount = 4;
		final int steps = startupCount + factory.getUiObjectCount();
		Platform.runLater( () -> splashScreen.setSteps( steps ) );

		// Update the product metadata
		metadata.loadContributors();
		Platform.runLater( () -> splashScreen.update() );

		// Create the setting manager
		log.trace( "Starting settings manager..." );
		File programSettingsFolder = new File( programDataFolder, ProgramSettings.BASE );
		settings = new Settings( executor, new File( programSettingsFolder, "program.properties" ) );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Settings manager started." );

		// Create the tool manager
		log.trace( "Starting tool manager..." );
		toolManager = new ToolManager( this );
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Tool manager started." );

		log.trace( "Starting resource manager..." );
		resourceManager = new ResourceManager( Program.this );
		//int resourceCount = resourceManager.getPreviouslyOpenResourceCount();
		Platform.runLater( () -> splashScreen.update() );
		log.debug( "Resource manager started." );

		// Restore the workspace
		Platform.runLater( () -> factory.restoreUi( splashScreen ) );

		// TODO Start the update manager

		// Finish the splash screen
		Platform.runLater( () -> splashScreen.done() );

		// Give the slash screen time to render and the user to see it
		Thread.sleep( 1000 );
	}

	private void registerIcons() {}

	private void unregisterIcons() {}

	private void showProgram() {
		workspaceManager.getActiveWorkspace().getStage().show();

		// Set the workarea actions
		getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaAction( this ) );
		getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaAction( this ) );
		getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaAction( this ) );

		new ProgramStartedEvent( this ).fire( listeners );
	}

	private void doShutdownTasks() {
		// TODO Stop the WorkspaceManager
		log.trace( "Stopping workspace manager..." );
		workspaceManager.shutdown();
		log.debug( "Workspace manager stopped." );

		// TODO Stop the UpdateManager

		// TODO Stop the ResourceManager

		// TODO Stop the ToolManager
		toolManager.shutdown();

		// Disconnect the settings listener
		log.trace( "Stopping settings manager..." );
		settings.removeProgramEventListener( watcher );
		log.debug( "Settings manager stopped." );

		// Unregister action handlers
		unregisterActionHandlers();

		// Unregister icons
		unregisterIcons();

		new ProgramStoppedEvent( this ).fire( listeners );

		// Stop the executor service
		log.trace( "Stopping executor service..." );
		executor.shutdown();
		log.debug( "Executor service stopped." );
	}

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

		private Stage stage;

		@Override
		protected Void call() throws Exception {
			doStartupTasks();
			return null;
		}

		@Override
		protected void succeeded() {
			splashScreen.hide();
			showProgram();
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
