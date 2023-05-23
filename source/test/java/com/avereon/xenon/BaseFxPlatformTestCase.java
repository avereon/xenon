package com.avereon.xenon;

import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseFxPlatformTestCase extends CommonXenonTestCase {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// NOTE Should not use a program for FX only testing

		// Start the FX platform
		Fx.startup();
	}

}
