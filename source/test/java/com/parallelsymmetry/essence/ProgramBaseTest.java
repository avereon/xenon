package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.ProductMetadata;
import javafx.stage.Stage;
import org.testfx.framework.junit.ApplicationTest;

/**
 * Tests based on TextFx: https://github.com/TestFX/TestFX
 */
public abstract class ProgramBaseTest extends ApplicationTest {

	protected Program program = new Program();

	protected ProductMetadata metadata;

	@Override
	public void start( Stage stage ) throws Exception {
		program.init();
		program.start( stage );
		metadata = program.getMetadata();
	}

	@Override
	public void stop() throws Exception {
		program.stop();
	}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException {
		new ProgramStartWatcher( program, clazz ).waitFor( 10000 );
	}

	private class ProgramStartWatcher implements ProgramEventListener {

		private Program program;

		private Class<? extends ProgramEvent> eventType;

		public ProgramStartWatcher( Program program,Class<? extends ProgramEvent> eventType ) {
			this.program = program;
			this.eventType  = eventType;
			program.addEventListener( this );
		}

		@Override
		public void eventOccurred( ProgramEvent event ) {
			if( eventType == event.getClass() ) {
				synchronized( this ) {
					this.notifyAll();
					program.removeEventListener( this );
				}
			}
		}

		public void waitFor( long timeout ) throws InterruptedException {
			synchronized( this ) {
				wait( timeout );
			}
		}

	}

}
