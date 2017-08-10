package com.xeomar.xenon;

import com.xeomar.xenon.util.OperatingSystem;
import org.junit.After;
import org.junit.Before;

public abstract class BaseTestCase  {

	protected Program program;

	@Before
	public void setup() throws Exception {
		//Log.setLevel( Log.NONE );

		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();
	}

	@After
	public void teardown() throws Exception {
		//Log.setLevel( Log.NONE );
	}

}
