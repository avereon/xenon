package com.parallelsymmetry.essence.testutil;

import com.parallelsymmetry.essence.*;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FxApplicationTestCase extends FxTestCase {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	private ProgramWatcher watcher;

	protected Program program;

	protected ProductMetadata metadata;

	@Before
	@Override
	public void setup() throws Exception {
		// WORKAROUND Parameters are null during testing due to Java 9 incompatibility
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

		initializeFx(  );

		metadata = program.getMetadata();
		program.addEventListener( watcher = new FxApplicationTestCase.ProgramWatcher() );
	}

	@After
	@Override
	public void cleanup() throws Exception {
		program.removeEventListener( watcher );
		FxToolkit.cleanupApplication( program );
	}

	@Override
	protected void initializeFx() throws Exception {
		FxToolkit.setupApplication( () -> program = new Program() );
	}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException {
		waitForEvent( clazz, DEFAULT_WAIT_TIMEOUT );
	}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
		watcher.waitFor( clazz, timeout );
	}

	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException {
		waitForNextEvent( clazz, DEFAULT_WAIT_TIMEOUT );
	}

	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
		watcher.waitForNext( clazz, timeout );
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
		 * Wait for an name of a specific name to occur. If the name has already
		 * occurred this method will return immediately. If the name has not
		 * already occurred then this method waits until the next name occurs, or
		 * the specified timeout, whichever comes first.
		 *
		 * @param clazz The event class to wait for
		 * @param timeout How long, in milliseconds, to wait for the event
		 * @throws InterruptedException If the timeout is exceeded
		 */
		synchronized void waitFor( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			boolean shouldWait = timeout > 0;
			long start = System.currentTimeMillis();
			long duration = 0;

			while( shouldWait && events.get( clazz ) == null ) {
				wait( timeout - duration );
				duration = System.currentTimeMillis() - start;
				shouldWait = duration < timeout;
			}
		}

		/**
		 * Wait for an name of a specific name to occur. This method always waits
		 * until the next name occurs, or the specified timeout, whichever comes
		 * first.
		 *
		 * @param clazz The event class to wait for
		 * @param timeout How long, in milliseconds, to wait for the event
		 * @throws InterruptedException If the timeout is exceeded
		 */
		synchronized void waitForNext( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			events.remove( clazz );
			waitFor( clazz, timeout );
		}

	}

}
