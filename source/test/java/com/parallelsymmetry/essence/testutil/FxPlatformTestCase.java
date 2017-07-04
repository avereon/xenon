package com.parallelsymmetry.essence.testutil;

import javafx.application.Platform;
import org.junit.Before;

public abstract class FxPlatformTestCase {

	@Before
	public void setup() throws Exception {
		try {
			Platform.startup( () -> {} );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
	}

}
