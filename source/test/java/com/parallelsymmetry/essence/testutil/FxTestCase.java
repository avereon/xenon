package com.parallelsymmetry.essence.testutil;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.testfx.framework.junit.ApplicationTest;

public abstract class FxTestCase extends ApplicationTest {

	private static volatile boolean fxInitialized;

	@Before
	public void setup() throws Exception {
		initializeFx();
	}

	@After
	public void cleanup() throws Exception {}

	@Override
	public void start( Stage stage ) throws Exception {}

	protected void initializeFx() throws Exception {
		try {
			Platform.startup( () -> {} );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
	}

}
