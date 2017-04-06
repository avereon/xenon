package com.parallelsymmetry.essence;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
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

		// Load the product descriptor. This is done before showing
		// the splash screen so it must be done quickly.
		ProductMetadata metadata = new ProductMetadata();

		title = metadata.getName();

		System.err.println( metadata.getGroup() );
		System.err.println( metadata.getArtifact() );
		System.err.println( metadata.getVersion() );
		System.err.println( metadata.getTimestamp() );

		System.err.println( metadata.getName() );
		System.err.println( metadata.getProvider() );
		System.err.println( metadata.getInception() );

		System.err.println( metadata.getSummary() );
		System.err.println( metadata.getDescription() );

	}

	@Override
	public void start( Stage stage ) throws Exception {
		log.info( "Start the program" );

		// Show the splash screen
		splashScreen = new SplashScreen( title ).show();

		// Submit the startup task
		executorService.submit( new StartupTask( stage ) );
	}

	@Override
	public void stop() throws Exception {
		log.info( "Stop the program" );

		executorService.submit( new ShutdownTask() );
		executorService.shutdown();
	}

	private void process() {
		try {
			log.info( "Starting process..." );
			Thread.sleep( 800 );
			log.info( "Process complete." );
		} catch( InterruptedException exception ) {
			//log.error( "Thread interrupted", exception );
		}
	}

	private void showProgram( Stage stage ) {
		FxUtil.centerStage( stage, 400, 250 );
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

			Platform.runLater( () -> splashScreen.setSteps( 3 ) );

			process();
			Platform.runLater( () -> splashScreen.update() );

			process();
			Platform.runLater( () -> splashScreen.update() );

			process();
			Platform.runLater( () -> splashScreen.update() );

			Thread.sleep( 500 );
			Platform.runLater( () -> splashScreen.done() );

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
