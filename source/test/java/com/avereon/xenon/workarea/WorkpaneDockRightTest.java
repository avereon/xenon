package com.avereon.xenon.workarea;

import com.avereon.xenon.resource.Resource;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WorkpaneDockRightTest extends WorkpaneTestCase {

	@Test
	public void testDockRightInLandscapeMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_RIGHT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	public void testDockRightInLandscapeModeWithTopAndBottomDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool topTool = new MockTool( resource );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( resource );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_RIGHT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	public void testDockRightInPortraitMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_RIGHT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	public void testDockRightInPortraitModeWithTopAndBottomDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool topTool = new MockTool( resource );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( resource );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_RIGHT ) );
		assertThat( view.getEdge( Side.TOP ), is( topTool.getToolView().getEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( bottomTool.getToolView().getEdge( Side.TOP ) ) );

		WorkpaneEdge edge = view.getEdge( Side.LEFT );
		assertThat( edge.getEdge( Side.TOP ), is( topTool.getToolView().getEdge( Side.BOTTOM ) ) );
		assertThat( edge.getEdge( Side.BOTTOM ), is( bottomTool.getToolView().getEdge( Side.TOP ) ) );
	}

}
