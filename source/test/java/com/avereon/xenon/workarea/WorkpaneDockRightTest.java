package com.avereon.xenon.workarea;

import com.avereon.xenon.resource.Resource;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class WorkpaneDockRightTest extends WorkpaneTestCase {

	@Test
	void testRightDockSize() {
		assertThat( workpane.getRightDockSize(), is( 0.2 ) );
		workpane.setRightDockSize( 0.25 );
		assertThat( workpane.getRightDockSize(), is( 0.25 ) );
	}

	@Test
	void testRightDockSizeMovesWithTool() {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_RIGHT ) );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.LEFT );
		assertThat( edge.getPosition(), is( 1 - workpane.getRightDockSize() ) );
		workpane.moveEdge( edge, -WORKPANE_WIDTH * 0.05 );
		assertThat( edge.getPosition(), is( 0.75 ) );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getRightDockSize(), is( 1 - edge.getPosition() ) );
	}

	@Test
	void testDockRightInLandscapeMode() {
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
	void testDockRightInLandscapeModeWithTopAndBottomDocks() {
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
	void testDockRightInPortraitMode() {
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
	void testDockRightInPortraitModeWithTopAndBottomDocks() {
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
