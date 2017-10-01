package com.xeomar.xenon;

import com.xeomar.xenon.util.JavaFxStarter;
import org.junit.Before;

public abstract class FxPlatformTestCase extends BaseTestCase {

	@Before
	public void setup() throws Exception {
		try {
			JavaFxStarter.startAndWait( 2000 );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
	}

}
