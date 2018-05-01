package com.xeomar.xenon;

import com.xeomar.util.Parameters;
import org.junit.Before;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		super.setup();
		program = new Program( Parameters.parse( ProgramTest.getParameterValues() ) );
		program.init();
	}

}
