package com.avereon.xenon;

import com.avereon.util.Parameters;
import org.junit.jupiter.api.BeforeEach;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		program = new Program();
		program.setProgramParameters( Parameters.parse( ProgramTestConfig.getParameterValues() ) );
		program.init();
	}

	protected Program getProgram() {
		return program;
	}

}
