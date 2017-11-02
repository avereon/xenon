package com.xeomar.xenon;

import com.xeomar.razor.OperatingSystem;
import org.junit.After;
import org.junit.Before;

public abstract class BaseTestCase {

	protected Program program;

	@Before
	public void setup() throws Exception {
		//  -Dglass.platform=Monocle -Dmonocle.platform=Headless -Dprism.order=sw
		//		System.setProperty( "glass.platform", "Monocle" );
		//		System.setProperty( "monocle.platform", "Headless" );
		//		System.setProperty( "prism.order", "sw" );

		//Log.setLevel( Log.NONE );

		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();
	}

	@After
	public void teardown() throws Exception {
		//Log.setLevel( Log.NONE );
	}

}
