package com.parallelsymmetry.essence.testutil;

import com.parallelsymmetry.essence.*;
import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public abstract class FxApplicationTestCase extends FxTestCase {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	private ProgramWatcher watcher;

	protected Program program;

	protected ProductMetadata metadata;

	/**
	 * Override setup() in FxTestCase and does not call super.setup().
	 *
	 * @throws Exception
	 */
	@Before
	@Override
	public void setup() throws Exception {
		// CLEANUP Parameters are null during testing due to Java 9 incompatibility
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		// Remove the existing program data folder
		try {
			String prefix = ExecMode.TEST.getPrefix();
			ProductMetadata metadata = new ProductMetadata();
			File programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
			if( programDataFolder != null && programDataFolder.exists() ) FileUtils.forceDelete( programDataFolder );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}

		super.setup();

		// Parameters are null during testing due to Java 9 incompatibility
		program = (Program)FxToolkit.setupApplication( Program.class, ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		metadata = program.getMetadata();
		program.addEventListener( watcher = new FxApplicationTestCase.ProgramWatcher() );

		waitForEvent( ProgramStartedEvent.class );
	}

	/**
	 * Override cleanup in FxTestCase and does not call super.cleanup().
	 *
	 * @throws Exception
	 */
	@After
	@Override
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );

		for( Window window : Stage.getWindows() ) {
			Platform.runLater( window::hide );
		}

		waitForEvent( ProgramStoppedEvent.class );
		program.removeEventListener( watcher );
	}

	//	@Override
	//	protected void initializeFx() throws Exception {
	//		// Parameters are null during testing due to Java 9 incompatibility
	//		program = (Program)FxToolkit.setupApplication( Program.class, ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );
	//	}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException, TimeoutException {
		waitForEvent( clazz, DEFAULT_WAIT_TIMEOUT );
	}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
		watcher.waitForEvent( clazz, timeout );
	}

	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException, TimeoutException {
		waitForNextEvent( clazz, DEFAULT_WAIT_TIMEOUT );
	}

	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
		watcher.waitForNextEvent( clazz, timeout );
	}

	private class ProgramWatcher implements ProgramEventListener {

		private Map<Class<? extends ProgramEvent>, ProgramEvent> events;

		ProgramWatcher() {
			this.events = new ConcurrentHashMap<>();
		}

		@Override
		public synchronized void eventOccurred( ProgramEvent event ) {
			events.put( event.getClass(), event );
			this.notifyAll();
		}

		/**
		 * Wait for an event of a specific class to occur. If the name has already
		 * occurred this method will return immediately. If the name has not
		 * already occurred then this method waits until the next name occurs, or
		 * the specified timeout, whichever comes first.
		 *
		 * @param clazz The event class to wait for
		 * @param timeout How long, in milliseconds, to wait for the event
		 * @throws InterruptedException If the timeout is exceeded
		 */
		synchronized void waitForEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
			boolean shouldWait = timeout > 0;
			long start = System.currentTimeMillis();
			long duration = 0;

			while( shouldWait && events.get( clazz ) == null ) {
				wait( timeout - duration );
				duration = System.currentTimeMillis() - start;
				shouldWait = duration < timeout;
			}
			duration = System.currentTimeMillis() - start;

			if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + clazz.getName() );
		}

		/**
		 * Wait for the next event of a specific class to occur. This method always
		 * waits until the next event occurs, or the specified timeout, whichever
		 * comes first.
		 *
		 * @param clazz The event class to wait for
		 * @param timeout How long, in milliseconds, to wait for the event
		 * @throws InterruptedException If the timeout is exceeded
		 */
		synchronized void waitForNextEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
			events.remove( clazz );
			waitForEvent( clazz, timeout );
		}

	}

}
