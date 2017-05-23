package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.testutil.FxTestCase;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

public class WorkpaneTest extends FxTestCase {

	private Workpane pane;

	@Before
	public void setUp() {
		pane = new Workpane();
	}

	@Test
	public void testAddTool() throws Exception {
		Resource resource = new Resource( URI.create( "" ) );
		MockTool tool = new MockTool( resource );
		pane.addTool( tool );
	}

}
