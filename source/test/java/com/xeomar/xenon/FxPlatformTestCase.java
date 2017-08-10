package com.xeomar.xenon;

import com.xeomar.xenon.util.JavaFxStarter;
import javafx.application.Platform;
import org.junit.Before;

public abstract class FxPlatformTestCase extends BaseTestCase {

	@Before
	public void setup() throws Exception {
		try {
			//Platform.startup( () -> {} );
			JavaFxStarter.startAndWait( 1000 );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
	}

}
