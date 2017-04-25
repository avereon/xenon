package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.product.ProductMetadata;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ProgramBaseTest extends ApplicationTest {

	private Program program = new Program();

	private ProductMetadata metadata;

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

	@Test
	public void testSomething() throws Exception {
		assertNotNull( program );

		// Wait for the program to start
		new ProgramStartWatcher( program ).waitFor( 10000 );

		String workareaName = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();

		assertThat( program.getWorkspaceManager().getActiveWorkspace().getStage().getTitle(), is( workareaName + " - " + metadata.getName() ) );
	}

	//	@Test
	//	public void testSomethingElse() {
	//		assertNotNull( program );
	//	}
	//
	//	@Test
	//	public void testYetSomethingElse() {
	//		assertNotNull( program );
	//	}

	private class ProgramStartWatcher implements ProgramEventListener {

		private Program program;

		public ProgramStartWatcher( Program program ) {
			this.program = program;
			program.addEventListener( this );
		}

		@Override
		public void eventOccurred( ProgramEvent event ) {
			if( event instanceof ProgramStartedEvent ) {
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
