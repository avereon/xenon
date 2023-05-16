package com.avereon.xenon;

import com.avereon.util.Parameters;
import org.junit.jupiter.api.BeforeEach;

public class ProgramTestCase extends BaseXenonTestCase {

	protected Xenon program;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		program = new Xenon();
		program.setProgramParameters( Parameters.parse( ProgramTestConfig.getParameterValues() ) );
		program.init();
	}

	protected Xenon getProgram() {
		return program;
	}

}
