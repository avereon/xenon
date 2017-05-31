package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.action.CloseWorkarea;
import com.parallelsymmetry.essence.action.Exit;
import com.parallelsymmetry.essence.action.NewWorkarea;
import com.parallelsymmetry.essence.action.RenameWorkarea;
import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStartingEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppingEvent;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductBundle;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Program extends Application implements Product {

	public static final String STYLESHEET = "style.css";

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

	private WorkspaceManager workspaceManager;

	private ProgramEventWatcher watcher;

	private Set<ProgramEventListener> listeners;

	private Exit exitActionHandler;

	static {
		// Initialize the logging

		try {
			LogManager.getLogManager().readConfiguration( Program.class.getResourceAsStream( "/logging.properties" ) );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}

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
		exitActionHandler = new Exit( this );

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
		int processorCount = Runtime.getRuntime().availableProcessors();
		log.trace( "Starting executor service..." );
		executor = Executors.newFixedThreadPool( Math.max( 2, processorCount ), new ProgramThreadFactory() );
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

	public long getStartTime() {
		return startTimestamp;
	}

	public File getProgramDataFolder() {
		return programDataFolder;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public ProductBundle getResourceBundle() {
		return productBundle;
	}

	public IconLibrary getIconLibrary() {
		return iconLibrary;
	}

	public ActionLibrary getActionLibrary() {
		return actionLibrary;
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

	private void configureLogging() {
		// The default logging configuration is in the logging.properties resource

		// Parse the log level parameter
		Level level = Level.INFO;
		try {
			level = Level.parse( getParameter( ProgramParameter.LOG_LEVEL ).toUpperCase() );
		} catch( Exception exception ) {
			// Intentionally ignore exception
		}

		// Set all the program loggers to log at the specified level
		String packageName = getClass().getPackageName();
		for( String name : Collections.list( LogManager.getLogManager().getLoggerNames() ) ) {
			java.util.logging.Logger.getLogger( name ).setLevel( level );
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

		// Create the workspace manager
		log.trace( "Starting workspace manager..." );
		workspaceManager = new WorkspaceManager( Program.this );
		log.debug( "Workspace manager started." );

		// Create the UI factory
		UiFactory factory = new UiFactory( Program.this );

		// Set the number of startup steps
		final int steps = 2 + factory.getUiObjectCount();
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

		// TODO Create the tool manager

		// TODO Create the resource manager
		//resourceManager = new ResourceManager(Program.this );
		//int resourceCount = resourceManager.getPreviouslyOpenResourceCount();

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
		getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkarea( this ) );
		getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkarea( this ) );
		getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkarea( this ) );

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
		getActionLibrary().getAction( "exit" ).pushAction( exitActionHandler );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "exit" ).pullAction( exitActionHandler );
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
