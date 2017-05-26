package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.testutil.FxTestCase;
import javafx.geometry.Side;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkpaneTest extends FxTestCase {

	private Workpane pane;

	@Before
	public void setup() throws Exception {
		super.setup();
		pane = new Workpane();
	}

	@Test
	public void testAddTool() throws Exception {
		Resource resource = new Resource( URI.create( "" ) );
		MockTool tool = new MockTool( resource );

		pane.addTool( tool );

		Workpane.ToolView view = tool.getToolView();
		assertThat( view.getEdge( Side.TOP ).isWall(), is( true ) );
	}

}
