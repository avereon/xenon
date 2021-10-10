package com.avereon.xenon.test.workpane;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkpaneDockLeftTest extends WorkpaneTestCase {

	@Test
	void testLeftDockSize() {
		assertThat( workpane.getLeftDockSize(), is( 0.2 ) );
		workpane.setLeftDockSize( 0.25 );
		assertThat( workpane.getLeftDockSize(), is( 0.25 ) );
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
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertFalse( workpane.isDockSpace( Side.TOP, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) );
		assertTrue( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) );
		assertFalse( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) );

		assertFalse( workpane.isDockSpace( Side.TOP, view ) );
		assertFalse( workpane.isDockSpace( Side.BOTTOM, view ) );
		assertFalse( workpane.isDockSpace( Side.LEFT, view ) );
		assertTrue( workpane.isDockSpace( Side.RIGHT, view ) );
	}

	@Test
	void testLeftDockSizeMovesWithTool() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.RIGHT );
		assertThat( edge.getPosition(), is( workpane.getLeftDockSize() ) );
		workpane.moveEdge( edge, WORKPANE_WIDTH * 0.05 );
		assertThat( edge.getPosition(), is( 0.25 ) );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getLeftDockSize(), is( 0.25 ) );
	}

	@Test
	void testDockLeftInLandscapeMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	void testDockLeftInLandscapeModeWithTopAndBottomDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool topTool = new MockTool( asset );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( asset );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	void testDockLeftInPortraitMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	void testDockLeftInPortraitModeWithTopAndBottomDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool topTool = new MockTool( asset );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( asset );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( topTool.getToolView().getEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( bottomTool.getToolView().getEdge( Side.TOP ) ) );

		WorkpaneEdge edge = view.getEdge( Side.RIGHT );
		assertThat( edge.getEdge( Side.TOP ), is( topTool.getToolView().getEdge( Side.BOTTOM ) ) );
		assertThat( edge.getEdge( Side.BOTTOM ), is( bottomTool.getToolView().getEdge( Side.TOP ) ) );
	}

}
