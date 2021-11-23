package com.avereon.xenon.workpane;

import com.avereon.xenon.asset.Asset;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneDockRightTest extends WorkpaneTestCase {

	@Test
	void testRightDockSize() {
		assertThat( workpane.getRightDockSize() ).isEqualTo( 0.2 );
		workpane.setRightDockSize( 0.25 );
		assertThat( workpane.getRightDockSize() ).isEqualTo( 0.25 );
	}

	@Test
	void testIsDockSpace() {
		WorkpaneView view = workpane.getDefaultView();
		assertThat( workpane.isDockSpace( Side.TOP, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.BOTTOM, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.LEFT, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.RIGHT, view ) ).isTrue();

		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertThat( workpane.isDockSpace( Side.TOP, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) ).isTrue();

		assertThat( workpane.isDockSpace( Side.TOP, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.BOTTOM, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.LEFT, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.RIGHT, view ) ).isFalse();
	}

	@Test
	void testRightDockSizeMovesWithTool() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_RIGHT );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.LEFT );
		assertThat( edge.getPosition() ).isEqualTo( 1 - workpane.getRightDockSize() );
		workpane.moveEdge( edge, -WORKPANE_WIDTH * 0.05 );
		assertThat( edge.getPosition() ).isEqualTo( 0.75 );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getRightDockSize() ).isEqualTo( 1 - edge.getPosition() );
	}

	@Test
	void testDockRightInLandscapeMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_RIGHT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testDockRightInLandscapeModeWithTopAndBottomDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool topTool = new MockTool( asset );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( asset );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_RIGHT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testDockRightInPortraitMode() {
		Asset asset = new Asset( "mock:asset" );
		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_RIGHT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testDockRightInPortraitModeWithTopAndBottomDocks() {
		Asset asset = new Asset( "mock:asset" );

		MockTool topTool = new MockTool( asset );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( asset );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( asset );
		tool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_RIGHT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( topTool.getToolView().getEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( bottomTool.getToolView().getEdge( Side.TOP ) );

		WorkpaneEdge edge = view.getEdge( Side.LEFT );
		assertThat( edge.getEdge( Side.TOP ) ).isEqualTo( topTool.getToolView().getEdge( Side.BOTTOM ) );
		assertThat( edge.getEdge( Side.BOTTOM ) ).isEqualTo( bottomTool.getToolView().getEdge( Side.TOP ) );
	}

}
