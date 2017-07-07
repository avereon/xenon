package com.parallelsymmetry.essence;

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
