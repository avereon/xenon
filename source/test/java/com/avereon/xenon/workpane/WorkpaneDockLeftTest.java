package com.avereon.xenon.workpane;

import com.avereon.xenon.resource.Resource;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneDockLeftTest extends WorkpaneTestCase {

	@Test
	void testLeftDockSize() {
		assertThat( workpane.getLeftDockSize() ).isEqualTo( 0.2 );
		workpane.setLeftDockSize( 0.25 );
		assertThat( workpane.getLeftDockSize() ).isEqualTo( 0.25 );
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
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		assertThat( workpane.isDockSpace( Side.TOP, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.BOTTOM, tool.getToolView() ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.LEFT, tool.getToolView() ) ).isTrue();
		assertThat( workpane.isDockSpace( Side.RIGHT, tool.getToolView() ) ).isFalse();

		assertThat( workpane.isDockSpace( Side.TOP, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.BOTTOM, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.LEFT, view ) ).isFalse();
		assertThat( workpane.isDockSpace( Side.RIGHT, view ) ).isTrue();
	}

	@Test
	void testLeftDockSizeMovesWithTool() {
		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_LEFT );

		// Move the dock edge
		WorkpaneEdge edge = view.getEdge( Side.RIGHT );
		assertThat( edge.getPosition() ).isEqualTo( workpane.getLeftDockSize() );
		workpane.moveEdge( edge, SCENE_WIDTH * 0.05 );
		assertThat( edge.getPosition() ).isEqualTo( 0.25 );

		// Verify the top dock size followed the dock edge
		assertThat( workpane.getLeftDockSize() ).isEqualTo( 0.25 );
	}

	@Test
	void testDockLeftInLandscapeMode() {
		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_LEFT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testDockLeftInLandscapeModeWithTopAndBottomDocks() {
		Resource resource = new Resource( "mock:asset" );

		MockTool topTool = new MockTool( resource );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( resource );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_LEFT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testDockLeftInPortraitMode() {
		Resource resource = new Resource( "mock:asset" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_LEFT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testDockLeftInPortraitModeWithTopAndBottomDocks() {
		Resource resource = new Resource( "mock:asset" );

		MockTool topTool = new MockTool( resource );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( resource );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view ).isNotNull();
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DOCK_LEFT );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( topTool.getToolView().getEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( bottomTool.getToolView().getEdge( Side.TOP ) );

		WorkpaneEdge edge = view.getEdge( Side.RIGHT );
		assertThat( edge.getEdge( Side.TOP ) ).isEqualTo( topTool.getToolView().getEdge( Side.BOTTOM ) );
		assertThat( edge.getEdge( Side.BOTTOM ) ).isEqualTo( bottomTool.getToolView().getEdge( Side.TOP ) );
	}

}
