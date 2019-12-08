package com.avereon.xenon.workpane;

import com.avereon.xenon.asset.Asset;
import javafx.geometry.Side;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkpaneDockTopTest extends WorkpaneTestCase {

	@Test
	void testTopDockSize() {
		assertThat( workpane.getTopDockSize(), is( 0.2 ) );
		workpane.setTopDockSize( 0.25 );
		assertThat( workpane.getTopDockSize(), is( 0.25 ) );
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
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertTrue( workpane.isDockSpace( Side.TOP, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) );

		assertFalse( workpane.isDockSpace( Side.TOP, view ) );
		assertTrue( workpane.isDockSpace( Side.BOTTOM, view ) );
		assertFalse( workpane.isDockSpace( Side.LEFT, view ) );
		assertFalse( workpane.isDockSpace( Side.RIGHT, view ) );
	}

	@Test
	void testTopDockSizeMovesWithTool() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.BOTTOM );
		assertThat( edge.getPosition(), is( workpane.getTopDockSize() ) );
		workpane.moveEdge( edge, WORKPANE_HEIGHT * 0.05 );
		assertThat( edge.getPosition(), is( 0.25  ) );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getTopDockSize(), is( 0.25 ) );
	}

	@Test
	void testDockTopInLandscapeMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	void testDockTopInLandscapeModeWithTopAndBottomDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool leftTool = new MockTool( asset );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( asset );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), Matchers.is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), Matchers.is( rightTool.getToolView().getEdge( Side.LEFT ) ) );

		WorkpaneEdge edge = view.getEdge( Side.BOTTOM );
		assertThat( edge.getEdge( Side.LEFT ), Matchers.is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( edge.getEdge( Side.RIGHT ), Matchers.is( rightTool.getToolView().getEdge( Side.LEFT ) ) );
	}

	@Test
	void testDockTopInPortraitMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	void testDockTopInPortraitModeWithTopAndBottomDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool leftTool = new MockTool( asset );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( asset );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

}
