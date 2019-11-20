package com.avereon.xenon;

import com.avereon.util.Parameters;
import org.junit.jupiter.api.BeforeEach;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		program = new Program();
		program.setProgramParameters( Parameters.parse( ProgramTest.getParameterValues() ) );
		program.init();
	}

}
