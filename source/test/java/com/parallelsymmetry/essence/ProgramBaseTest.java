package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.ProductMetadata;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tests based on TextFx: https://github.com/TestFX/TestFX
 */
public abstract class ProgramBaseTest {

	private static final long DEFAULT_WAIT_TIMEOUT = 10000;

	protected Program program;

	protected ProductMetadata metadata;

	private ProgramWatcher watcher;

	@Before
	public void setup() throws Exception {
		FxToolkit.registerPrimaryStage();
		program = (Program)FxToolkit.setupApplication( Program.class, "" );
		watcher = new ProgramWatcher();
		program.addEventListener( watcher );
		metadata = program.getMetadata();
	}

	@After
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );
	}

	//	@Override
	//	public void start( Stage stage ) throws Exception {
	//		program = new Program();
	//		watcher = new ProgramWatcher();
	//		program.addEventListener( watcher );
	//
	//		program.init();
	//		program.start( stage );
	//		metadata = program.getMetadata();
	//	}
	//
	//	@Override
	//	public void stop() throws Exception {
	//		program.stop();
	//	}

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
			long start = System.currentTimeMillis();
			long duration = 0;
			while( duration < timeout && events.get( clazz ) == null ) {
				duration = System.currentTimeMillis() - start;
				wait( timeout - duration );
			}
		}

		public synchronized void waitForNext( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
			events.remove( clazz );
			waitFor( clazz, timeout );
		}

	}

}
