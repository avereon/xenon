package com.xeomar.xenon;

import org.junit.Before;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		super.setup();

		// WORKAROUND The parameters defined below are null during testing due to Java 9 incompatibility
		// NOTE These are also used in FxProgramTestCase
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );
		System.setProperty( ProgramParameter.LOG_LEVEL, "none" );

		program = new Program();
		program.init();
	}

}
