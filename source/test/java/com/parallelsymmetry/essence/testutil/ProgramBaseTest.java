package com.parallelsymmetry.essence.testutil;

import com.parallelsymmetry.essence.*;
import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tests based on TextFx: https://github.com/TestFX/TestFX
 */
public abstract class ProgramBaseTest extends FxTestCase {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	protected ProductMetadata metadata;

	protected Program program;

	private ProgramWatcher watcher;

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

	@Before
	public void setUp() throws Exception {
		super.setUp();
		System.out.println( "ProgramBaseTest setup called..." );
		program = new Program();
		program.init();
		Platform.runLater( () -> {
			try {
				program.start( new Stage() );
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );

		metadata = program.getMetadata();
		watcher = new ProgramWatcher();
		program.addEventListener( watcher );
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		waitForEvent( ProgramStartedEvent.class );
		Platform.runLater( () -> {
			try {
				program.stop();
				// Don't use requestExit because it calls Platform.exit()
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );
		waitForEvent( ProgramStoppedEvent.class );
		assertFalse( program.getWorkspaceManager().getActiveWorkspace().getStage().isShowing() );
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

		public synchronized void waitFor( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			boolean shouldWait = timeout > 0;
			long start = System.currentTimeMillis();

			while( shouldWait && events.get( clazz ) == null ) {
				long duration = System.currentTimeMillis() - start;
				shouldWait = duration < timeout;
				if( shouldWait ) wait( timeout - duration );
			}
		}

		public synchronized void waitForNext( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			events.remove( clazz );
			waitFor( clazz, timeout );
		}

	}

}
