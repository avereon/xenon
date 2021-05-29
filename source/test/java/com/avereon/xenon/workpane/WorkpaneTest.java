package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkpaneTest extends WorkpaneTestCase {

	@Test
	void testAddToolAllocateCheck() {
		MockTool tool = new MockTool( asset );
		tool.setWorkpane( workpane );
		workpane.addTool( tool, false );
		assertTrue( workpane.getTools().contains( tool ) );
		assertTrue( tool.canFindSelfFromWorkpane() );
		assertTrue( tool.canFindWorkpaneFromSelf() );
	}

	@Test
	void testAddRemoveToolEvents() {
		MockTool tool = new MockTool( asset );

		// Add the tool but do not set it active.
		workpane.addTool( tool, false );
		assertThat( tool, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		// Didn't choose to select the tool, so no activate event
		assertThat( tool.getEvents().size(), is( 2 ) );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool(), is( tool ) );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		// Didn't choose to select the tool, so no deactivate event
		assertThat( tool, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool.getEvents().size(), is( 4 ) );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView(), is( nullValue() ) );
		assertThat( view.getActiveTool(), is( nullValue() ) );
	}

	@Test
	void testAddSelectRemoveToolEvents() {
		MockTool tool = new MockTool( asset );
		assertThat( workpane.getActiveTool(), is( nullValue() ) );

		// Add the tool and set it active.
		workpane.addTool( tool, true );
		assertThat( tool, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool.getEvents().size(), is( 3 ) );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool(), is( tool ) );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		assertThat( tool, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool.getEvents().size(), is( 6 ) );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView(), is( nullValue() ) );
		assertThat( view.getActiveTool(), is( nullValue() ) );
	}

	@Test
	void testSelectToolEvents() {
		MockTool tool1 = new MockTool( asset );
		MockTool tool2 = new MockTool( asset );
		MockTool tool3 = new MockTool( asset );
		assertThat( tool1.getEvents().size(), is( 0 ) );
		assertThat( tool2.getEvents().size(), is( 0 ) );
		assertThat( tool3.getEvents().size(), is( 0 ) );

		// Add tool one but do not activate it.
		workpane.addTool( tool1, false );
		assertThat( tool1, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool1, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool1.getEvents().size(), is( 2 ) );

		// Add tool two but do not activate it.
		workpane.addTool( tool2, false );
		assertThat( tool2, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool2.getEvents().size(), is( 1 ) );

		// Add tool three and activate it.
		workpane.addTool( tool3, true );
		assertThat( tool1, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool1.getEvents().size(), is( 3 ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		//assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) ); // Extra
		assertThat( tool3, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool3.getEvents().size(), is( 3 ) );

		// Try to set tool three active again.
		workpane.setActiveTool( tool3 );
		assertThat( tool3, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool3.getEvents().size(), is( 5 ) );

		// Set tool two active.
		workpane.setActiveTool( tool2 );
		assertThat( tool3, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool3.getEvents().size(), is( 7 ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool2.getEvents().size(), is( 3 ) );

		// Remove tool one.
		// This tests the removal of an inactive tool.
		workpane.removeTool( tool1 );
		assertThat( tool1, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool1.getEvents().size(), is( 4 ) );

		// Remove tool two.
		// This tests the removal of an active tool when there is more than one tool.
		workpane.removeTool( tool2 );
		assertThat( tool2, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool2.getEvents().size(), is( 6 ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool3.getEvents().size(), is( 9 ) );
	}

	@Test
	void testSetActiveTool() {
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );
		Tool tool3 = new MockTool( asset );

		workpane.addTool( tool1, false );
		workpane.addTool( tool2, false );
		workpane.addTool( tool3, false );

		assertThat( getActiveTool( toolview ), is( tool1 ) );
		workpane.setActiveTool( tool2 );
		assertThat( getActiveTool( toolview ), is( tool2 ) );
		workpane.setActiveTool( tool3 );
		assertThat( getActiveTool( toolview ), is( tool3 ) );
		workpane.setActiveTool( tool1 );
		assertThat( getActiveTool( toolview ), is( tool1 ) );
	}

	@Test
	void testDockMode() {
		assertThat( workpane.getDockMode(), is( Workpane.DEFAULT_DOCK_MODE ) );

		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		assertThat( workpane.getDockMode(), is( Workpane.DockMode.PORTRAIT ) );

		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		assertThat( workpane.getDockMode(), is( Workpane.DockMode.LANDSCAPE ) );

		workpane.setDockMode( Workpane.DEFAULT_DOCK_MODE );
		assertThat( workpane.getDockMode(), is( Workpane.DEFAULT_DOCK_MODE ) );
	}

	@Test
	void testSplitNorth() {
		WorkpaneView view1 = workpane.split( Side.TOP );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitSouth() {
		WorkpaneView view1 = workpane.split( Side.BOTTOM );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitWest() {
		WorkpaneView view1 = workpane.split( Side.LEFT );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitEast() {
		WorkpaneView view1 = workpane.split( Side.RIGHT );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
	}

	@Test
	void testSplitNorthCompound() {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		WorkpaneView view2 = workpane.split( Side.TOP );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitSouthCompound() {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		WorkpaneView view2 = workpane.split( Side.BOTTOM );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitEastCompound() {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		WorkpaneView view2 = workpane.split( Side.RIGHT );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1.0 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );

		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitWestCompound() {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		WorkpaneView view2 = workpane.split( Side.LEFT );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
	}

	@Test
	void testSplitViewNorth() {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitViewNorthWithSize() {
		double size = 0.31;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( toolview, Side.TOP, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( workpane.getTopDockSize() ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( workpane.getTopDockSize() ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		double position = workpane.getTopDockSize() + (1 - toolview.getEdge( Side.TOP ).getPosition()) * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( toolview, Side.TOP, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( view1.getEdge( Side.BOTTOM ).getPosition() ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( position ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( position ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitViewSouth() {
		WorkpaneView view1 = workpane.split( toolview, Side.BOTTOM );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitViewSouthWithSize() {
		double size = 0.33;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( toolview, Side.BOTTOM, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 1 - workpane.getBottomDockSize() ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1 - workpane.getBottomDockSize() ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		double position = 1 - workpane.getBottomDockSize() - toolview.getEdge( Side.BOTTOM ).getPosition() * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( toolview, Side.BOTTOM, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( position ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( view1.getEdge( Side.TOP ).getPosition() ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( position ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitViewWest() {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
	}

	@Test
	void testSplitViewWestWithSize() {
		double size = 0.35;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( workpane.getLeftDockSize() ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( workpane.getLeftDockSize() ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		double position = workpane.getLeftDockSize() + (1 - toolview.getEdge( Side.LEFT ).getPosition()) * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( toolview, Side.LEFT, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( view1.getEdge( Side.RIGHT ).getPosition() ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( position ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( position ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitViewEast() {
		WorkpaneView view1 = workpane.split( toolview, Side.RIGHT );
		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

	@Test
	void testSplitViewEastWithSize() {
		double size = 0.37;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( toolview, Side.RIGHT, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view1.getEdge( Side.LEFT ).getPosition(), is( 1 - workpane.getRightDockSize() ) );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1 - workpane.getRightDockSize() ) );

		double position = 1 - workpane.getRightDockSize() - toolview.getEdge( Side.RIGHT ).getPosition() * size;

		WorkpaneView view2 = workpane.split( toolview, Side.RIGHT, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view2.getEdge( Side.LEFT ).getPosition(), is( position ) );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition(), is( view1.getEdge( Side.LEFT ).getPosition() ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( position ) );
	}

	private Tool getActiveTool( WorkpaneView view ) {
		ToolTabPane pane = (ToolTabPane)view.getChildren().get( 0 );

		int selectedIndex = pane.getSelectionModel().getSelectedIndex();
		ToolTab tab = pane.getTabs().get( selectedIndex );

		return tab.getTool();
	}

	private static <T> Matcher<MockTool> nextEvent( Matcher<T> valueMatcher ) {
		return new FeatureMatcher<>( valueMatcher, "Tool method call", "method call" ) {

			@Override
			@SuppressWarnings( "unchecked" )
			protected T featureValueOf( MockTool tool ) {
				try {
					return (T)tool.getNextEvent();
				} catch( ArrayIndexOutOfBoundsException exception ) {
					return null;
				}
			}

		};
	}

	//	private static Matcher<MockTool> hasEvent( int index ) {
	//		return hasEvent( index, not( nullValue() ) );
	//	}
	//
	//	private static <T> Matcher<MockTool> hasEvent( int index, Matcher<T> valueMatcher ) {
	//		return new FeatureMatcher<>( valueMatcher, "Tool method call", "method call" ) {
	//
	//			@Override
	//			@SuppressWarnings( "unchecked" )
	//			protected T featureValueOf( MockTool tool ) {
	//				try {
	//					return (T)tool.getEvents().get( index );
	//				} catch( ArrayIndexOutOfBoundsException exception ) {
	//					return null;
	//				}
	//			}
	//
	//		};
	//	}

	private static Matcher<MockTool.MethodCall> isMethod( String name ) {
		return new CustomTypeSafeMatcher<>( "matching " + name ) {

			@Override
			protected boolean matchesSafely( MockTool.MethodCall event ) {
				return event != null && event.method.equals( name );
			}

		};
	}

}
