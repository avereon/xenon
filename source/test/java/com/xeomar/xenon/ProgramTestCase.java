package com.xeomar.xenon;

import org.junit.Before;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		super.setup();

		// WORKAROUND The parameters defined below are null during testing due to Java 9 incompatibility
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		try {
			program = new Program();
		} catch ( Throwable throwable ) {
			throwable.printStackTrace();
		}
		program.init();
	}

}
