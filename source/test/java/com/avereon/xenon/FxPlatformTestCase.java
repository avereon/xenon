package com.avereon.xenon;

import com.avereon.util.Parameters;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;

public abstract class FxPlatformTestCase extends CommonProgramTestBase {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// Create the program
		setProgram( new Xenon().setProgramParameters( Parameters.parse( ProgramTestConfig.getParameterValues() ) ) );

		// Start the FX platform
		Fx.startup();
	}

}
