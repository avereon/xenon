package com.parallelsymmetry.essence;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Program extends Application {

	private static final Logger log = LoggerFactory.getLogger( Program.class );

	private static String title;

	private SplashScreen splashScreen;

	private ExecutorService executorService;

	public static void main( String[] commands ) {
		log.info( "Main method before launch" );
		launch( commands );
	}

	public Program() {
		// Create the ExecutorService
		int executorThreadCount = Runtime.getRuntime().availableProcessors();
		if( executorThreadCount < 2 ) executorThreadCount = 2;
		executorService = Executors.newFixedThreadPool( executorThreadCount );
	}

	@Override
	public void init() throws Exception {
		log.info( "Initialize the program" );

		title = "Essence";
	}

	@Override
	public void start( Stage primaryStage ) throws Exception {
		log.info( "Start the program" );

		// Show the splash screen
		splashScreen = new SplashScreen( title ).show();

		// Submit the startup task
		executorService.submit( new StartupTask( primaryStage ) );
	}

	@Override
	public void stop() throws Exception {
		log.info( "Stop the program" );

		executorService.submit( new ShutdownTask() );
		executorService.shutdown();

		super.stop();
	}

	private void process() {
		try {
			log.info( "Starting process..." );
			Thread.sleep( 500 );
			log.info( "Process complete." );
		} catch( InterruptedException exception ) {
			//log.error( "Thread interrupted", exception );
		}
	}

	private void showProgram( Stage stage ) {
		// FIXME Not specifying the location allows the OS to choose
		stage.show();
	}

	private class StartupTask extends Task<Void> {

		private Stage stage;

		public StartupTask( Stage stage ) {
			this.stage = stage;
		}

		@Override
		protected Void call() throws Exception {
			// TODO Start the SettingsManager
			// TODO Start the ResourceManager
			// TODO Start the UpdateManager
			process();
			Platform.runLater( () -> splashScreen.update() );
			process();
			Platform.runLater( () -> splashScreen.update() );
			process();
			Platform.runLater( () -> splashScreen.update() );
			process();
			Platform.runLater( () -> splashScreen.update() );
			process();
			Platform.runLater( () -> splashScreen.update() );
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
