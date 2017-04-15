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

	private static final long startTimestamp;

	private static String programTitle;

	private SplashScreen splashScreen;

	private ExecutorService executor;

	private ProductMetadata metadata;

	private File programDataFolder;

	// private PersistentSettings settings;

	private WorkspaceManager workspaceManager;

	private ProgramEventWatcher watcher;

	private Set<ProgramEventListener> listeners;

	static {
		startTimestamp = System.currentTimeMillis();
		System.setProperty( "java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n" );
	}

	public static void main( String[] commands ) {
		//log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		// Create the ExecutorService
		int processorCount = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool( Math.max( 2, processorCount ), new ProgramThreadFactory() );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();

		// Create the event watcher
		addEventListener( watcher = new ProgramEventWatcher() );
	}

	@Override
	public void init() throws Exception {
		// Load the product metadata.
		metadata = new ProductMetadata();
		programTitle = metadata.getName();
		programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact(), metadata.getName() );

		log.info( "Platform init time: " + (System.currentTimeMillis() - startTimestamp) );
	}

	@Override
	public void start( Stage stage ) throws Exception {
		dispatchEvent( new ProgramStartingEvent( this ) );

		// Show the splash screen
		splashScreen = new SplashScreen( programTitle );
		splashScreen.show();

		// Submit the startup task
		executor.submit( new StartupTask() );
	}

	@Override
	public void stop() throws Exception {
		dispatchEvent( new ProgramStoppingEvent( this ) );

		//settings.removeEventListener( watcher );

		executor.submit( new ShutdownTask() );
		executor.shutdown();

		dispatchEvent( new ProgramStoppedEvent( this ) );
		removeEventListener( watcher );
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

	public void dispatchEvent( ProgramEvent event ) {
		for( ProgramEventListener listener : listeners ) {
			listener.eventOccurred( event );
		}
	}

	public void addEventListener( ProgramEventListener listener ) {
		this.listeners.add( listener );
	}

	public ProgramEventWatcher getEventWatcher() {
		return watcher;
	}

	public void removeEventListener( ProgramEventListener listener ) {
		this.listeners.remove( listener );
	}

	private void showProgram() {
		Stage stage = workspaceManager.getActiveWorkspace().getStage();
		stage.show();
		dispatchEvent( new ProgramStartedEvent( this ) );
	}

	private class StartupTask extends Task<Void> {

		private Stage stage;

		@Override
		protected Void call() throws Exception {
			// TODO Start the SettingsManager and tie to the EventWatcher
			// TODO Start the ResourceManager
			// TODO Start the UpdateManager

			// Give the slash screen time to render and the user to see it
			Thread.sleep( 500 );

			// Create the workspace manager
			UiFactory factory = new UiFactory( Program.this );
			workspaceManager = new WorkspaceManager( Program.this );

			// Set the number of startup steps
			final int steps = 1 + factory.getUiObjectCount();
			Platform.runLater( () -> splashScreen.setSteps( steps ) );

			// Create the setting manager
			File settingsFile = new File( programDataFolder, "settings.properties" );
			//			settings = new PersistentSettings( executor, settingsFile );
			//			settings.addEventListener( watcher );
			Platform.runLater( () -> splashScreen.update() );

			// Restore the workspace
			Platform.runLater( () -> factory.restoreUi( splashScreen ) );

			// Create the resource manager
			//resourceManager = new ResourceManager(Program.this );
			//int resourceCount = resourceManager.getPreviouslyOpenResourceCount();

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
			// TODO Stop the SettingsManager
			// TODO Stop the ExecutorService
			return null;
		}

	}

}
