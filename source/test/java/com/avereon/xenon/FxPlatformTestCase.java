package com.avereon.xenon;

import org.junit.jupiter.api.BeforeEach;
import org.testfx.framework.junit5.ApplicationTest;

public abstract class FxPlatformTestCase extends ApplicationTest {

	@BeforeEach
	public void setup() throws Exception {
		//JavaFxStarter.startAndWait( 5000 );
	}

}
