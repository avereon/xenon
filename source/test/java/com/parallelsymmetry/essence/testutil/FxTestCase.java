package com.parallelsymmetry.essence.testutil;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.CountDownLatch;

public abstract class FxTestCase extends ApplicationTest {

	@Before
	public void setup() throws Exception {
		initializeFx();
	}

	@After
	public void cleanup() throws Exception {}

	@Override
	public void start( Stage stage ) throws Exception {}

	protected void initializeFx() throws Exception {
		if( isFxInitialized() ) return;
		final CountDownLatch latch = new CountDownLatch( 1 );
		Platform.startup( latch::countDown );
		latch.await();
	}

	protected boolean isFxInitialized() {
		try {
			Platform.runLater( () -> {} );
			return true;
		} catch( IllegalStateException exception ) {
			return false;
		}
	}

}
