package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStartingEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppingEvent;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Program extends Application implements Product {

	private Logger log = LoggerFactory.getLogger( Program.class );

	private static final long startTimestamp;

	private static String title;

	private SplashScreen splashScreen;

	private ExecutorService executorService;

	private ProductMetadata metadata;

	private ProgramEventWatcher watcher;

	private Set<ProgramEventListener> listeners;

	static {
		startTimestamp = System.currentTimeMillis();
		System.setProperty("java.util.logging.SimpleFormatter.format","%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
	}

	public static void main( String[] commands ) {
		//log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		// Create the ExecutorService
		int executorThreadCount = Runtime.getRuntime().availableProcessors();
		if( executorThreadCount < 2 ) executorThreadCount = 2;
		executorService = Executors.newFixedThreadPool( executorThreadCount );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();

		// Create the event watcher
		watcher = new ProgramEventWatcher();
		addEventListener( watcher );
	}

	@Override
	public void init() throws Exception {
		//log.info( "Initialize the program" );

		// Load the product metadata. This must be done quickly because it is
		// loaded before the splash screen is displayed.
		metadata = new ProductMetadata();
		title = metadata.getName();

		System.out.println( System.currentTimeMillis() - startTimestamp );
	}

	@Override
	public void start( Stage stage ) throws Exception {
		//log.info( "Start the program" );
		fireEvent( new ProgramStartingEvent( this ) );

		// Show the splash screen
		splashScreen = new SplashScreen( title ).show();

		// Submit the startup task
		executorService.submit( new StartupTask( stage ) );
	}

	@Override
	public void stop() throws Exception {
		//log.info( "Stop the program" );
		fireEvent( new ProgramStoppingEvent( this ) );

		executorService.submit( new ShutdownTask() );
		executorService.shutdown();

		fireEvent( new ProgramStoppedEvent( this ) );
		removeEventListener( watcher );
	}

	@Override
	public ProductMetadata getMetadata() {
		return metadata;
	}

	public long getStartTime() {
		return startTimestamp;
	}

	public void addEventListener( ProgramEventListener listener ) {
		this.listeners.add( listener );
	}

	public void removeEventListener( ProgramEventListener listener ) {
		this.listeners.remove( listener );
	}

	private void process() {
		try {
			//log.info( "Starting process..." );
			Thread.sleep( 200 );
			//log.info( "Process complete." );
		} catch( InterruptedException exception ) {
			//log.error( "Thread interrupted", exception );
		}
	}

	private void showProgram( Stage stage ) {
		FxUtil.centerStage( stage, 400, 250 );
		stage.show();
		fireEvent( new ProgramStartedEvent( this ) );
	}

	private void fireEvent( ProgramEvent event ) {
		for( ProgramEventListener listener : listeners ) {
			listener.eventOccurred( event );
		}
	}

	private class StartupTask extends Task<Void> {

		private Stage stage;

		public StartupTask( Stage stage ) {
			this.stage = stage;
		}

		@Override
		protected Void call() throws Exception {
			// TODO Start the SettingsManager and tie to the EventWatcher
			// TODO Start the ResourceManager
			// TODO Start the UpdateManager

			// Set the number of startup steps
			Platform.runLater( () -> splashScreen.setSteps( 10 ) );

			for( int index = 0; index < 10; index++ ) {
				// Do something time consuming
				process();

				// Update the splash screen
				Platform.runLater( () -> splashScreen.update() );
			}

			// Finish the splash screen
			Platform.runLater( () -> splashScreen.done() );

			// Give the slash screen time to finalize
			Thread.sleep( 500 );

			return null;
		}

		@Override
		protected void succeeded() {
			splashScreen.hide();
			showProgram( stage );
		}

		@Override
		protected void cancelled() {
			splashScreen.hide();
		}

		@Override
		protected void failed() {
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
