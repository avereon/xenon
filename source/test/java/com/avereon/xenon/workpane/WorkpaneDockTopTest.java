package com.avereon.xenon.workpane;

import com.avereon.xenon.asset.Resource;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneDockTopTest extends WorkpaneTestCase {

	@Test
	void testTopDockSize() {
		assertThat( workpane.getTopDockSize() ).isEqualTo( 0.2 );
		workpane.setTopDockSize( 0.25 );
		assertThat( workpane.getTopDockSize() ).isEqualTo( 0.25 );
	}

	@Test
	void testIsDockSpace() {
		WorkpaneView view = workpane.getDefaultView();
		assertThat( workpane.isDockSpace( Side.TOP, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.BOTTOM, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.LEFT, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.RIGHT, view ) ).isTrue();

		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertThat( workpane.isDockSpace( Side.TOP, tool.getToolView() ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) ).isFalse();

		assertThat( workpane.isDockSpace( Side.TOP, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.BOTTOM, view ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.LEFT, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.RIGHT, view ) ).isFalse();
	}

	@Test
	void testTopDockSizeMovesWithTool() {
		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_TOP );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.BOTTOM );
		assertThat( edge.getPosition() ).isEqualTo( workpane.getTopDockSize() );
		workpane.moveEdge( edge, SCENE_HEIGHT * 0.05 );
		assertThat( edge.getPosition() ).isEqualTo( 0.25 );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getTopDockSize() ).isEqualTo( 0.25 );
	}

	@Test
	void testDockTopInLandscapeMode() {
		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_TOP );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testDockTopInLandscapeModeWithTopAndBottomDocks() {
		Resource resource = new Resource( "mock:asset" );

		MockTool leftTool = new MockTool( resource );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( resource );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_TOP );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( leftTool.getToolView().getEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( rightTool.getToolView().getEdge( Side.LEFT ) );

		WorkpaneEdge edge = view.getEdge( Side.BOTTOM );
		assertThat( edge.getEdge( Side.LEFT ) ).isEqualTo( leftTool.getToolView().getEdge( Side.RIGHT ) );
		assertThat( edge.getEdge( Side.RIGHT ) ).isEqualTo( rightTool.getToolView().getEdge( Side.LEFT ) );
	}

	@Test
	void testDockTopInPortraitMode() {
		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_TOP );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testDockTopInPortraitModeWithTopAndBottomDocks() {
		Resource resource = new Resource( "mock:asset" );

		MockTool leftTool = new MockTool( resource );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( resource );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_TOP );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
	}

}
