package com.parallelsymmetry.essence.testutil;

import com.parallelsymmetry.essence.*;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FxTestCase extends ApplicationTest {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	protected Program program;

	private ProgramWatcher watcher;

	protected ProductMetadata metadata;

	static {
		// WORKAROUND Parameters are null during testing due to Java 9 incompatibility
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		try {
			ProductMetadata metadata = new ProductMetadata();
			String prefix = ExecMode.TEST.getPrefix();
			File programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
			if( programDataFolder != null && programDataFolder.exists() ) FileUtils.forceDelete( programDataFolder );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

	@Override
	public void start( Stage stage ) throws Exception {}

	@Before
	public void setup() throws Exception {
		FxToolkit.setupApplication( () -> program = new Program() );
		metadata = program.getMetadata();
		watcher = new ProgramWatcher();
		program.addEventListener( watcher );
	}

	@After
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );
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
