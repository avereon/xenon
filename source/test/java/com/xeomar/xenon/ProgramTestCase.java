package com.xeomar.xenon;

import org.junit.Before;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		super.setup();

		// WORKAROUND The parameters defined below are null during testing due to Java 9 incompatibility
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		program = new Program();
		assertThat( program, not( is( nullValue() )));
		program.init();
	}

}
