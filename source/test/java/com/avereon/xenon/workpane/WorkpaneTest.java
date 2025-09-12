package com.avereon.xenon.workpane;

import com.avereon.util.ThreadUtil;
import com.avereon.zerra.javafx.Fx;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneTest extends WorkpaneTestCase {

	@Test
	void testAddToolAllocateCheck() {
		MockTool tool = new MockTool( asset );
		tool.setWorkpane( workpane );
		workpane.addTool( tool, false );
		assertThat( workpane.getTools().contains( tool ) ).isTrue();
		assertThat( tool.canFindSelfFromWorkpane() ).isTrue();
		assertThat( tool.canFindWorkpaneFromSelf() ).isTrue();
	}

	@Test
	void testAddRemoveToolEvents() {
		MockTool tool = new MockTool( asset );

		// Add the tool but do not set it active.
		workpane.addTool( tool, false );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.ALLOCATE );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.DISPLAY );
		// Didn't choose to select the tool, so no activate event
		assertThat( tool.getEvents().size() ).isEqualTo( 2 );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool() ).isEqualTo( tool );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		// Didn't choose to select the tool, so no deactivate event
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.CONCEAL );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.DEALLOCATE );
		assertThat( tool.getEvents().size() ).isEqualTo( 4 );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView() ).isNull();
		assertThat( view.getActiveTool() ).isNull();
	}

	@Test
	void testAddSelectRemoveToolEvents() {
		MockTool tool = new MockTool( asset );
		assertThat( workpane.getActiveTool() ).isNull();

		// Add the tool and set it active.
		workpane.addTool( tool, true );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.ALLOCATE );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.DISPLAY );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.ACTIVATE );
		assertThat( tool.getEvents().size() ).isEqualTo( 3 );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool() ).isEqualTo( tool );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.DEACTIVATE );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.CONCEAL );
		ToolEventAssert.assertThat( tool.getNextEvent() ).hasMethod( MockTool.DEALLOCATE );
		assertThat( tool.getEvents().size() ).isEqualTo( 6 );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView() ).isNull();
		assertThat( view.getActiveTool() ).isNull();
	}

	@Test
	void testSelectToolEvents() {
		MockTool tool1 = new MockTool( asset );
		MockTool tool2 = new MockTool( asset );
		MockTool tool3 = new MockTool( asset );
		assertThat( tool1.getEvents().size() ).isEqualTo( 0 );
		assertThat( tool2.getEvents().size() ).isEqualTo( 0 );
		assertThat( tool3.getEvents().size() ).isEqualTo( 0 );

		// Add tool one but do not activate it.
		workpane.addTool( tool1, false );
		ToolEventAssert.assertThat( tool1.getNextEvent() ).hasMethod( MockTool.ALLOCATE );
		ToolEventAssert.assertThat( tool1.getNextEvent() ).hasMethod( MockTool.DISPLAY );
		assertThat( tool1.getEvents().size() ).isEqualTo( 2 );

		// Add tool two but do not activate it.
		workpane.addTool( tool2, false );
		ToolEventAssert.assertThat( tool2.getNextEvent() ).hasMethod( MockTool.ALLOCATE );
		assertThat( tool2.getEvents().size() ).isEqualTo( 1 );

		// Add tool three and activate it.
		workpane.addTool( tool3, true );
		ToolEventAssert.assertThat( tool1.getNextEvent() ).hasMethod( MockTool.CONCEAL );
		assertThat( tool1.getEvents().size() ).isEqualTo( 3 );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.ALLOCATE );
		//ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.ACTIVATE); // Extra
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.DISPLAY );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.ACTIVATE );
		assertThat( tool3.getEvents().size() ).isEqualTo( 3 );

		// Try to set tool three active again.
		workpane.setActiveTool( tool3 );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.DEACTIVATE );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.ACTIVATE );
		assertThat( tool3.getEvents().size() ).isEqualTo( 5 );

		// Set tool two active.
		workpane.setActiveTool( tool2 );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.DEACTIVATE );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.CONCEAL );
		assertThat( tool3.getEvents().size() ).isEqualTo( 7 );
		ToolEventAssert.assertThat( tool2.getNextEvent() ).hasMethod( MockTool.DISPLAY );
		ToolEventAssert.assertThat( tool2.getNextEvent() ).hasMethod( MockTool.ACTIVATE );
		assertThat( tool2.getEvents().size() ).isEqualTo( 3 );

		// Remove tool one.
		// This tests the removal of an inactive tool.
		workpane.removeTool( tool1 );
		ToolEventAssert.assertThat( tool1.getNextEvent() ).hasMethod( MockTool.DEALLOCATE );
		assertThat( tool1.getEvents().size() ).isEqualTo( 4 );

		// Remove tool two.
		// This tests the removal of an active tool when there is more than one tool.
		workpane.removeTool( tool2 );
		ToolEventAssert.assertThat( tool2.getNextEvent() ).hasMethod( MockTool.DEACTIVATE );
		ToolEventAssert.assertThat( tool2.getNextEvent() ).hasMethod( MockTool.CONCEAL );
		ToolEventAssert.assertThat( tool2.getNextEvent() ).hasMethod( MockTool.DEALLOCATE );
		assertThat( tool2.getEvents().size() ).isEqualTo( 6 );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.DISPLAY );
		ToolEventAssert.assertThat( tool3.getNextEvent() ).hasMethod( MockTool.ACTIVATE );
		assertThat( tool3.getEvents().size() ).isEqualTo( 9 );
	}

	@Test
	void testSetActiveTool() {
		// given
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );
		Tool tool3 = new MockTool( asset );

		tool1.setTitle( "Tool 1" );
		tool2.setTitle( "Tool 2" );
		tool3.setTitle( "Tool 3" );

		workpane.addTool( tool1, false );
		workpane.addTool( tool2, false );
		workpane.addTool( tool3, false );

		assertThat( view.getActiveTool() ).isEqualTo( tool1 );
		assertThat( getActiveToolTabTool( view ) ).isEqualTo( tool1 );

		// when
		workpane.setActiveTool( tool2 );

		// then
		assertThat( view.getActiveTool() ).isEqualTo( tool2 );
		assertThat( getActiveToolTabTool( view ) ).isEqualTo( tool2 );

		// when
		workpane.setActiveTool( tool3 );

		// then
		assertThat( view.getActiveTool() ).isEqualTo( tool3 );
		assertThat( getActiveToolTabTool( view ) ).isEqualTo( tool3 );

		// when
		workpane.setActiveTool( tool1 );

		// then
		assertThat( view.getActiveTool() ).isEqualTo( tool1 );
		assertThat( getActiveToolTabTool( view ) ).isEqualTo( tool1 );
	}

	@Test
	void testDockMode() {
		assertThat( workpane.getDockMode() ).isEqualTo( Workpane.DEFAULT_DOCK_MODE );

		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		assertThat( workpane.getDockMode() ).isEqualTo( Workpane.DockMode.PORTRAIT );

		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		assertThat( workpane.getDockMode() ).isEqualTo( Workpane.DockMode.LANDSCAPE );

		workpane.setDockMode( Workpane.DEFAULT_DOCK_MODE );
		assertThat( workpane.getDockMode() ).isEqualTo( Workpane.DEFAULT_DOCK_MODE );
	}

	@Test
	void testSplitNorth() {
		WorkpaneView view1 = workpane.split( Side.TOP );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitSouth() {
		WorkpaneView view1 = workpane.split( Side.BOTTOM );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitWest() {
		WorkpaneView view1 = workpane.split( Side.LEFT );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitEast() {
		WorkpaneView view1 = workpane.split( Side.RIGHT );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitNorthCompound() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		WorkpaneView view2 = workpane.split( Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitSouthCompound() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		WorkpaneView view2 = workpane.split( Side.BOTTOM );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitEastCompound() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		WorkpaneView view2 = workpane.split( Side.RIGHT );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1.0 - Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitWestCompound() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		WorkpaneView view2 = workpane.split( Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitViewNorth() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitViewNorthWithSize() {
		double size = 0.31;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.TOP, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( workpane.getTopDockSize() );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( workpane.getTopDockSize() );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		double position = workpane.getTopDockSize() + (1 - view.getEdge( Side.TOP ).getPosition()) * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( view, Side.TOP, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( view1.getEdge( Side.BOTTOM ).getPosition() );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( position );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( position );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitViewSouth() {
		WorkpaneView view1 = workpane.split( view, Side.BOTTOM );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitViewSouthWithSize() {
		double size = 0.33;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.BOTTOM, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 1 - workpane.getBottomDockSize() );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1 - workpane.getBottomDockSize() );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		double position = 1 - workpane.getBottomDockSize() - view.getEdge( Side.BOTTOM ).getPosition() * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( view, Side.BOTTOM, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( position );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( view1.getEdge( Side.TOP ).getPosition() );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( position );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitViewWest() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitViewWestWithSize() {
		double size = 0.35;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.LEFT, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( workpane.getLeftDockSize() );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( workpane.getLeftDockSize() );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		double position = workpane.getLeftDockSize() + (1 - view.getEdge( Side.LEFT ).getPosition()) * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( view, Side.LEFT, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( view1.getEdge( Side.RIGHT ).getPosition() );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( position );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( position );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testSplitViewEast() {
		WorkpaneView view1 = workpane.split( view, Side.RIGHT );
		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );
	}

	@Test
	void testSplitViewEastWithSize() {
		double size = 0.37;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.RIGHT, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 1 - workpane.getRightDockSize() );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1 - workpane.getRightDockSize() );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1 );

		double position = 1 - workpane.getRightDockSize() - view.getEdge( Side.RIGHT ).getPosition() * size;

		WorkpaneView view2 = workpane.split( view, Side.RIGHT, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition() ).isEqualTo( position );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( view1.getEdge( Side.LEFT ).getPosition() );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( position );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( view, view1, view2 );
	}

	@Test
	void testCloseLastToolInDefaultView() {
		// given
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );

		tool1.setTitle( "Tool 1" );
		tool2.setTitle( "Tool 2" );

		WorkpaneView defaultView = workpane.getDefaultView();
		WorkpaneView leftView = workpane.split( Side.LEFT );

		workpane.addTool( tool1, leftView, false );
		workpane.addTool( tool2, defaultView, true );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( leftView, defaultView );
		assertThat( workpane.getDefaultView() ).isEqualTo( defaultView );

		// when
		workpane.closeTool( tool2 );

		// then
		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( leftView, defaultView );
		assertThat( workpane.getDefaultView() ).isEqualTo( defaultView );
		assertThat( leftView.getTools() ).containsExactly( tool1 );
		assertThat( defaultView.getTools() ).isEmpty();
		assertThat( workpane.getTools() ).hasSize( 1 );
	}

	@Test
	void testCloseToolInViewThatCanAutoMerge() {
		// given
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );

		tool1.setTitle( "Tool 1" );
		tool2.setTitle( "Tool 2" );

		WorkpaneView defaultView = workpane.getDefaultView();
		WorkpaneView leftView = workpane.split( Side.LEFT );

		workpane.addTool( tool1, leftView, false );
		workpane.addTool( tool2, defaultView, true );

		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( leftView, defaultView );
		assertThat( workpane.getDefaultView() ).isEqualTo( defaultView );

		// when
		workpane.closeTool( tool1 );

		// then
		assertThat( workpane.getViews() ).containsExactlyInAnyOrder( defaultView );
		assertThat( workpane.getDefaultView() ).isEqualTo( defaultView );
		assertThat( defaultView.getTools() ).containsExactly( tool2 );
		assertThat( workpane.getTools() ).hasSize( 1 );
	}

	@Test
	void addTool() {
		// given
		Workpane workpane = new Workpane();
		resolve( workpane );
		Tool tool = new MockTool( asset );

		// when
		Fx.run( () -> {
			WorkpaneView centerView = workpane.getDefaultView();
			workpane.addTool( tool, centerView );
		} );
		Fx.waitFor( TIMEOUT );

		// then
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		WorkpaneView centerView = workpane.getDefaultView();
		assertThat( tool.getParent().getParent().getParent() ).isSameAs( centerView );
	}

	@Test
	void addToolToNonDefaultView() {
		// given
		Workpane workpane = new Workpane();
		// If I move resolve() to here the test fails
		resolve( workpane );
		Tool tool = new MockTool( asset );

		// when
		Fx.run( () -> {
			WorkpaneView centerView = workpane.getDefaultView();
			WorkpaneView leftView = workpane.split( centerView, Side.LEFT );
			workpane.addTool( tool, leftView );
		} );
		Fx.waitFor( TIMEOUT );
		// If I move resolve() to here the test works
		// I assume this is because all the components get their skins
		//resolve( workpane );
		ThreadUtil.pause( 100 );

		// then
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		WorkpaneView leftView = workpane.getWallEdge( Side.LEFT ).getViews( Side.RIGHT ).iterator().next();
		assertThat( tool.getParent().getParent().getParent() ).isSameAs( leftView );
	}

	@Test
	void addToolWithMultipleViews() {
		// given
		Workpane workpane = resolve( new Workpane() );
		WorkpaneView view = workpane.getDefaultView();

		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );
		Tool tool3 = new MockTool( asset );

		// when
		Fx.run( () -> {
			WorkpaneView centerView = workpane.getDefaultView();
			workpane.addTool( tool1, centerView );
		} );
		Fx.waitForStability( TIMEOUT );

		// then
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		WorkpaneView centerView = workpane.getDefaultView();
		assertThat( tool1.getParent().getParent().getParent() ).isSameAs( centerView );

		// when
		Fx.run( () -> {
			WorkpaneView leftView = workpane.split( view, Side.LEFT );
			workpane.addTool( tool2, leftView );
		} );
		Fx.waitForStability( TIMEOUT );
		// There must be some other thread working for FX
		//Thread.yield();

		// then
		//		WorkpaneView leftView = workpane.getWallEdge( Side.LEFT ).getViews( Side.RIGHT ).iterator().next();
		//		assertThat( tool2.getParent().getParent().getParent() ).isSameAs( leftView );
		//		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		// FIXME Woohoo! Reproduced the problem
		// TODO If this can be fixed then the workaround in Workpane.dispatchEvents() may be removed.
		// This appears to be a test environment issue.

		// when
		Fx.run( () -> {
			WorkpaneView rightView = workpane.split( view, Side.RIGHT );
			workpane.addTool( tool3, rightView );
		} );
		Fx.waitForStability( TIMEOUT );

		// then
		WorkpaneView rightView = workpane.getWallEdge( Side.RIGHT ).getViews( Side.LEFT ).iterator().next();
		assertThat( tool3.getParent().getParent().getParent() ).isSameAs( rightView );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
	}

	private Tool getActiveToolTabTool( WorkpaneView view ) {
		return view.getToolTabPane().getSelectionModel().getSelectedItem().getTool();
	}

}
