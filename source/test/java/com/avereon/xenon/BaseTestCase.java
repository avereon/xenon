package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import org.junit.After;
import org.junit.Before;

public abstract class BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		//Log.setLevel( Log.NONE );

		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();

		program = new Program();
	}

	@After
	public void teardown() throws Exception {
		//Log.setLevel( Log.NONE );
	}

}
