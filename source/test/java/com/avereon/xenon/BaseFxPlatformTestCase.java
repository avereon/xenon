package com.avereon.xenon;

import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseFxPlatformTestCase extends BaseForAllTests {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// NOTE Do not create an application for tests that only need the FX platform

		// Start the FX platform
		Fx.startup();
	}

}
