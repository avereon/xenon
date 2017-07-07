package com.parallelsymmetry.essence;

import org.junit.After;
import org.junit.Before;

public abstract class BaseTestCase  {

	protected Program program;

	@Before
	public void setup() throws Exception {
		//Log.setLevel( Log.NONE );
	}

	@After
	public void teardown() throws Exception {
		//Log.setLevel( Log.NONE );
	}

}
