package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStartingEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppingEvent;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.settings.PersistentSettings;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

	private PersistentSettings settings;

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
		int executorThreadCount = Runtime.getRuntime().availableProcessors();
		if( executorThreadCount < 2 ) executorThreadCount = 2;
		executor = Executors.newFixedThreadPool( executorThreadCount, new ProgramThreadFactory() );

		// Create the listeners set
		listeners = new CopyOnWriteArraySet<>();

		// Create the event watcher
		watcher = new ProgramEventWatcher();
		addEventListener( watcher );
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
		fireEvent( new ProgramStartingEvent( this ) );

		// Show the splash screen
		splashScreen = new SplashScreen( programTitle ).show();

		// Submit the startup task
		executor.submit( new StartupTask( stage ) );
	}

	@Override
	public void stop() throws Exception {
		fireEvent( new ProgramStoppingEvent( this ) );

		executor.submit( new ShutdownTask() );
		executor.shutdown();

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

	public File getProgramDataFolder() {
		return programDataFolder;
	}

	public void addEventListener( ProgramEventListener listener ) {
		this.listeners.add( listener );
	}

	public void removeEventListener( ProgramEventListener listener ) {
		this.listeners.remove( listener );
	}

	private void showProgram( Stage stage ) {
		FxUtil.centerStage( stage, 400, 250 );

		stage.xProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth ) {
				System.out.println( "X: " + newSceneWidth );
				settings.put("program/windows/0/x", String.valueOf( newSceneWidth ) );
			}
		} );
		stage.yProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight ) {
				System.out.println( "Y: " + newSceneHeight );
			}
		} );
		stage.widthProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth ) {
				System.out.println( "W: " + newSceneWidth );
			}
		} );
		stage.heightProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight ) {
				System.out.println( "H: " + newSceneHeight );
			}
		} );

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

			Thread.sleep( 100 );

			// Set the number of startup steps
			Platform.runLater( () -> splashScreen.setSteps( 1 ) );

			// Create the setting manager
			File settingsFile = new File( programDataFolder, "settings.properties" );
			settings = new PersistentSettings( executor, settingsFile );
			Platform.runLater( () -> splashScreen.update() );

			// Finish the splash screen
			Platform.runLater( () -> splashScreen.done() );

			// Give the slash screen time to finalize and the user to see it
			Thread.sleep( 400 );

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
