package com.avereon.xenon.test.workpane;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import javafx.geometry.Side;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkpaneDockBottomTest extends WorkpaneTestCase {

	@Test
	void testBottomDockSize() {
		assertThat( workpane.getBottomDockSize(), is( 0.2 ) );
		workpane.setBottomDockSize( 0.25 );
		assertThat( workpane.getBottomDockSize(), is( 0.25 ) );
	}

	@Test
	void testIsDockSpace() {
		WorkpaneView view = workpane.getDefaultView();
		assertTrue( workpane.isDockSpace( Side.TOP, view ) );
		assertTrue( workpane.isDockSpace( Side.BOTTOM, view ) );
		assertTrue( workpane.isDockSpace( Side.LEFT, view ) );
		assertTrue( workpane.isDockSpace( Side.RIGHT, view ) );

		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertFalse( workpane.isDockSpace( Side.TOP, tool.getToolView() ) );
		assertTrue( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) );

		assertTrue( workpane.isDockSpace( Side.TOP, view ) );
		assertFalse( workpane.isDockSpace( Side.BOTTOM, view ) );
		assertFalse( workpane.isDockSpace( Side.LEFT, view ) );
		assertFalse( workpane.isDockSpace( Side.RIGHT, view ) );
	}

	@Test
	void testBottomDockSizeMovesWithTool() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.TOP );
		assertThat( edge.getPosition(), Matchers.is( 1 - workpane.getBottomDockSize() ) );
		workpane.moveEdge( edge, -WORKPANE_HEIGHT * 0.05 );
		assertThat( edge.getPosition(), is( 0.75 ) );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getBottomDockSize(), is( 1 - edge.getPosition() ) );
	}

	@Test
	void testDockBottomInLandscapeMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	void testDockBottomInLandscapeModeWithLeftAndRightDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool leftTool = new MockTool( asset );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( asset );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( rightTool.getToolView().getEdge( Side.LEFT ) ) );

		WorkpaneEdge edge = view.getEdge( Side.TOP );
		assertThat( edge.getEdge( Side.LEFT ), is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( edge.getEdge( Side.RIGHT ), is( rightTool.getToolView().getEdge( Side.LEFT ) ) );
	}

	@Test
	void testDockBottomInPortraitMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	void testDockBottomInPortraitModeWithLeftAndRightDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool leftTool = new MockTool( asset );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( asset );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

}
