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

	protected Program program;

	private ProgramWatcher watcher;

	protected ProductMetadata metadata;

	static {
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
	}

	@Before
	@Override
	public void setup() throws Exception {
		initializeFx(  );
		metadata = program.getMetadata();
		watcher = new FxApplicationTestCase.ProgramWatcher();
		program.addEventListener( watcher );
	}

	@After
	@Override
	public void cleanup() throws Exception {
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

		public ProgramWatcher() {
			this.events = new ConcurrentHashMap<>();
		}

		@Override
		public synchronized void eventOccurred( ProgramEvent event ) {
			events.put( event.getClass(), event );
			this.notifyAll();
		}

		/**
		 * Wait for an event of a specific type to occur. If the event has already
		 * occurred this method will return immediately. If the event has not
		 * already occurred then this method waits until the next event occurs, or
		 * the specified timeout, whichever comes first.
		 *
		 * @param clazz
		 * @param timeout
		 * @throws InterruptedException
		 */
		public synchronized void waitFor( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			boolean shouldWait = timeout > 0;
			long start = System.currentTimeMillis();

			while( shouldWait && events.get( clazz ) == null ) {
				long duration = System.currentTimeMillis() - start;
				shouldWait = duration < timeout;
				if( shouldWait ) wait( timeout - duration );
			}
		}

		/**
		 * Wait for an event of a specific type to occur. This method always waits
		 * until the next event occurs, or the specified timeout, whichever comes
		 * first.
		 *
		 * @param clazz
		 * @param timeout
		 * @throws InterruptedException
		 */
		public synchronized void waitForNext( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			events.remove( clazz );
			waitFor( clazz, timeout );
		}

	}

}
