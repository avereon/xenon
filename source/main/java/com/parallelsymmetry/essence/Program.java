package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.action.ExitProgramHandler;
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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Program extends Application implements Product {

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

	private ExitProgramHandler exitActionHandler;

	static {
		try {
			LogManager.getLogManager().readConfiguration( Program.class.getResourceAsStream( "/logging.properties" ) );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	public static void main( String[] commands ) {
		//log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		startTimestamp = System.currentTimeMillis();

		// Create program action handlers
		exitActionHandler = new ExitProgramHandler( this );

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
	}

	@Override
	public void start( Stage stage ) throws Exception {
		printHeader();
		log.info( "Program init time (ms): " + (System.currentTimeMillis() - startTimestamp) );

		new ProgramStartingEvent( this ).fire( listeners );

		// Show the splash screen
		splashScreen = new SplashScreen( programTitle );
		//splashScreen.initOwner( stage );
		splashScreen.show();

		// Create the executor service
		int processorCount = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool( Math.max( 2, processorCount ), new ProgramThreadFactory() );

		// Submit the startup task
		executor.submit( new StartupTask() );
	}

	@Override
	public void stop() throws Exception {
		new ProgramStoppingEvent( this ).fire( listeners );

		// Submit the shutdown task
		executor.submit( new ShutdownTask() );

		// Stop the executor service
		executor.shutdown();
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

	private String getParameter( String key ) {
		Parameters parameters = getParameters();
		if( parameters == null ) {
			// WORKAROUND Parameters are null during testing due to Java 9 incompatibility
			return System.getProperty( key );
		}
		return parameters.getNamed().get( key );
	}

	private void showProgram() {
		workspaceManager.getActiveWorkspace().getStage().show();
		new ProgramStartedEvent( this ).fire( listeners );
	}

	private void registerIcons() {}

	private void unregisterIcons() {}

	private void registerActionHandlers() {
		getActionLibrary().getAction( "exit" ).pushAction( exitActionHandler );
	}

	private void unregisterActionHandlers() {
		getActionLibrary().getAction( "exit" ).pullAction( exitActionHandler );
	}

	private class StartupTask extends Task<Void> {

		private Stage stage;

		@Override
		protected Void call() throws Exception {
			// Give the slash screen time to render and the user to see it
			Thread.sleep( 500 );

			// Create the product bundle
			productBundle = new ProductBundle( getClass().getClassLoader(), getParameter( ProgramParameter.LOCALE ) );

			// Create the icon library
			iconLibrary = new IconLibrary();
			registerIcons();

			// Create the action library
			actionLibrary = new ActionLibrary( productBundle, iconLibrary );
			registerActionHandlers();

			// Create the workspace manager
			workspaceManager = new WorkspaceManager( Program.this );

			// Create the UI factory
			UiFactory factory = new UiFactory( Program.this );

			// Set the number of startup steps
			final int steps = 2 + factory.getUiObjectCount();
			Platform.runLater( () -> splashScreen.setSteps( steps ) );

			// Update the product metadata
			metadata.loadContributors();
			Platform.runLater( () -> splashScreen.update() );

			// Create the setting manager
			File programSettingsFolder = new File( programDataFolder, ProgramSettings.BASE );
			settings = new Settings( executor, new File( programSettingsFolder, "program.properties" ) );
			Platform.runLater( () -> splashScreen.update() );

			// TODO Create the tool manager

			// Restore the workspace
			Platform.runLater( () -> factory.restoreUi( splashScreen ) );

			// TODO Create the resource manager
			//resourceManager = new ResourceManager(Program.this );
			//int resourceCount = resourceManager.getPreviouslyOpenResourceCount();

			// TODO Start the update manager

			// Finish the splash screen
			Platform.runLater( () -> splashScreen.done() );

			// Give the slash screen time to render and the user to see it
			Thread.sleep( 500 );

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
		}

		@Override
		protected void failed() {
			log.error( "Error during startup task", getException() );
			splashScreen.hide();
		}

	}

	private class ShutdownTask extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			// TODO Stop the UpdateManager
			// TODO Stop the ResourceManager
			// TODO Stop the ToolManager

			// Disconnect the settings listener
			settings.removeProgramEventListener( watcher );

			// Unregister action handlers
			unregisterActionHandlers();

			// Unregister icons
			unregisterIcons();

			new ProgramStoppedEvent( this ).fire( listeners );

			return null;
		}

	}

}
