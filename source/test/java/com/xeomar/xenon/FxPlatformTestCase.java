package com.xeomar.xenon;

import javafx.application.Platform;
import org.junit.Before;

public abstract class FxPlatformTestCase extends BaseTestCase {

	@Before
	public void setup() throws Exception {
		try {
			Platform.startup( () -> {} );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
	}

}
