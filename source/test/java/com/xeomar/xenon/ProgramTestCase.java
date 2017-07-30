package com.xeomar.xenon;

import org.junit.Before;

public class ProgramTestCase extends BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		super.setup();
		program = new Program();
		program.init();
	}

}
