package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStartingEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppingEvent;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Program extends Application implements Product {

	private Logger log = LoggerFactory.getLogger( Program.class );

	private long startTimestamp;

	private String programTitle;

	private SplashScreen splashScreen;

	private ExecutorService executor;

	private ProductMetadata metadata;

	private File programDataFolder;

	private Settings settings;

	private WorkspaceManager workspaceManager;

	private ProgramEventWatcher watcher;

	private Set<ProgramEventListener> listeners;

	static {
		System.setProperty( "java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s %6$s%n" );
	}

	public static void main( String[] commands ) {
		//log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		startTimestamp = System.currentTimeMillis();

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
		// Load the product metadata.
		metadata = new ProductMetadata();
		programTitle = metadata.getName();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact(), metadata.getName() );
	}

	@Override
	public void start( Stage stage ) throws Exception {
		printHeader();
		System.out.println( "Program init time (ms): " + (System.currentTimeMillis() - startTimestamp ));

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

	private void printHeader() {
		System.out.println( metadata.getName() + " " + metadata.getVersion() );
		System.out.println( "Java " + System.getProperty( "java.vm.version" ) );
	}

	private void showProgram() {
		Stage stage = workspaceManager.getActiveWorkspace().getStage();
		stage.show();
		new ProgramStartedEvent( this ).fire( listeners );
	}

	private class StartupTask extends Task<Void> {

		private Stage stage;

		@Override
		protected Void call() throws Exception {
			// Give the slash screen time to render and the user to see it
			Thread.sleep( 500 );

			// Create the workspace manager
			UiFactory factory = new UiFactory( Program.this );
			workspaceManager = new WorkspaceManager( Program.this );

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

			// Disconnect the settings listener
			settings.removeProgramEventListener( watcher );

			new ProgramStoppedEvent( this ).fire( listeners );

			return null;
		}

	}

}
