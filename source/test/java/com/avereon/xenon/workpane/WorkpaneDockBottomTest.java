package com.avereon.xenon.workpane;

import com.avereon.xenon.asset.Asset;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneDockBottomTest extends WorkpaneTestCase {

	@Test
	void testBottomDockSize() {
		assertThat( workpane.getBottomDockSize() ).isEqualTo( 0.2 );
		workpane.setBottomDockSize( 0.25 );
		assertThat( workpane.getBottomDockSize() ).isEqualTo( 0.25 );
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
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertThat( workpane.isDockSpace( Side.TOP, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) ).isFalse();

		assertThat( workpane.isDockSpace( Side.TOP, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.BOTTOM, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.LEFT, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.RIGHT, view ) ).isFalse();
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
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_BOTTOM );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.TOP );
		assertThat( edge.getPosition() ).isEqualTo( 1 - workpane.getBottomDockSize() );
		workpane.moveEdge( edge, -WORKPANE_HEIGHT * 0.05 );
		assertThat( edge.getPosition() ).isEqualTo( 0.75 );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getBottomDockSize() ).isEqualTo( 1 - edge.getPosition() );
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
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_BOTTOM );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
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
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_BOTTOM );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( leftTool.getToolView().getEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( rightTool.getToolView().getEdge( Side.LEFT ) );

		WorkpaneEdge edge = view.getEdge( Side.TOP );
		assertThat( edge.getEdge( Side.LEFT ) ).isEqualTo( leftTool.getToolView().getEdge( Side.RIGHT ) );
		assertThat( edge.getEdge( Side.RIGHT ) ).isEqualTo( rightTool.getToolView().getEdge( Side.LEFT ) );
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
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_BOTTOM );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
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
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_BOTTOM );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
	}

}
