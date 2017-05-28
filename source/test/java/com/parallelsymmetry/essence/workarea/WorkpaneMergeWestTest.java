package com.parallelsymmetry.essence.workarea;

import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class WorkpaneMergeWestTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeWestSingleTargetSingleSource() throws Exception {
		ToolView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeWestSingleTargetSingleSourceOnEdge() throws Exception {
		ToolView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.LEFT, false ) );
	}

	@Test
	public void testCanPushMergeWestSingleTargetMultipleSource() throws Exception {
		ToolView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( west, Side.TOP );
		workpane.split( west, Side.BOTTOM );
		assertEquals( 4, workpane.getViews().size() );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
	}

	@Test
	public void testCanPushMergeWestMultipleTargetSingleSource() throws Exception {
		workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
	}

	@Test
	public void testCanPushMergeWestMultipleTargetMultipleSource() throws Exception {
		ToolView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( west, Side.TOP );
		workpane.split( west, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
	}

	@Test
	public void testCanPushMergeWestComplex() throws Exception {
		ToolView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		ToolView northwest = workpane.split( west, Side.TOP );
		ToolView southwest = workpane.split( west, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( northwest, Side.RIGHT );
		workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 8 ) );
		assertFalse( workpane.canPushMerge( toolview, Side.LEFT, false ) );
	}

	// TODO Continue implementing WorkpaneMergeWest tests

	//	@Test
	//	public void testPushMergeWestSingleTargetSingleSource() throws Exception {
	//		ToolView east = workpane.split( toolview, Side.RIGHT );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( toolview, workpane.getDefaultView() );
	//		assertEquals( toolview, workpane.getActiveView() );
	//		assertFalse( east.equals( workpane.getActiveView() ) );
	//
	//		workpane.setActiveView( east );
	//		workpane.setDefaultView( east );
	//		assertEquals( east, workpane.getActiveView() );
	//		assertEquals( east, workpane.getDefaultView() );
	//
	//		Tool view = new MockTool();
	//		Tool view1 = new MockTool();
	//		workpane.addTool( view, toolview );
	//		workpane.addTool( view1, east );
	//
	//		workpane.pushMerge( east, Side.LEFT );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( workpane, east.getWorkPane() );
	//		assertNull( toolview.getWorkPane() );
	//		assertEquals( east, workpane.getDefaultView() );
	//		assertEquals( east, workpane.getActiveView() );
	//		assertEquals( 2, east.getTools().size() );
	//
	//		assertEquals( 0.0, east.northEdge.position );
	//		assertEquals( 1.0, east.southEdge.position );
	//		assertEquals( 0.0, east.westEdge.position );
	//		assertEquals( 1.0, east.eastEdge.position );
	//		assertEquals( east.getSize(), workpane.getSize() );
	//	}
	//
	//	@Test
	//	public void testPushMergeWestSingleTargetMultipleSource() throws Exception {
	//		ToolView east = workpane.split( toolview, Side.RIGHT );
	//		workpane.split( toolview, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		workpane.setDefaultView( east );
	//
	//		workpane.pushMerge( east, Side.LEFT );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( 0, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( east ) );
	//		assertEquals( east, workpane.getDefaultView() );
	//	}
	//
	//	@Test
	//	public void testPushMergeWestMultipleTargetSingleSourceFromNortheast() throws Exception {
	//		ToolView northeast = workpane.split( toolview, Side.RIGHT );
	//		ToolView southeast = workpane.split( northeast, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( northeast );
	//
	//		workpane.pushMerge( northeast, Side.LEFT );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northeast ) );
	//		assertTrue( workpane.getViews().contains( southeast ) );
	//
	//		assertEquals( northeast.northEdge, workpane.northEdge );
	//		assertEquals( northeast.southEdge, southeast.northEdge );
	//		assertEquals( northeast.westEdge, workpane.westEdge );
	//		assertEquals( northeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( southeast.northEdge, northeast.southEdge );
	//		assertEquals( southeast.southEdge, workpane.southEdge );
	//		assertEquals( southeast.westEdge, workpane.westEdge );
	//		assertEquals( southeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( northeast.southEdge.westEdge, workpane.westEdge );
	//		assertEquals( northeast.southEdge.eastEdge, workpane.eastEdge );
	//	}
	//
	//	@Test
	//	public void testPushMergeWestMultipleTargetSingleSourceFromSoutheast() throws Exception {
	//		ToolView northeast = workpane.split( toolview, Side.RIGHT );
	//		ToolView southeast = workpane.split( northeast, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( southeast );
	//
	//		workpane.pushMerge( southeast, Side.LEFT );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northeast ) );
	//		assertTrue( workpane.getViews().contains( southeast ) );
	//
	//		assertEquals( northeast.northEdge, workpane.northEdge );
	//		assertEquals( northeast.southEdge, southeast.northEdge );
	//		assertEquals( northeast.westEdge, workpane.westEdge );
	//		assertEquals( northeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( southeast.northEdge, northeast.southEdge );
	//		assertEquals( southeast.southEdge, workpane.southEdge );
	//		assertEquals( southeast.westEdge, workpane.westEdge );
	//		assertEquals( southeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( northeast.southEdge.westEdge, workpane.westEdge );
	//		assertEquals( northeast.southEdge.eastEdge, workpane.eastEdge );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeWestSingleTargetSingleSource() throws Exception {
	//		ToolView view = workpane.split( toolview, Side.LEFT );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.LEFT, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.LEFT, false ) );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeWestAcrossEditView() throws Exception {
	//		ToolView view = workpane.split( Side.LEFT );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.LEFT, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.LEFT, false ) );
	//	}
	//
	//	@Test
	//	public void testPullMergeWestMultipleSourceEdgeTarget() throws Exception {
	//		ToolView northView = workpane.split( Side.TOP );
	//		ToolView southView = workpane.split( Side.BOTTOM );
	//		ToolView westView = workpane.split( Side.LEFT );
	//		assertEquals( 4, workpane.getViews().size() );
	//		assertEquals( 3, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", westView.getEdge( Side.RIGHT ), northView.getEdge( Side.LEFT ) );
	//		assertEquals( "Common edge not linked.", westView.getEdge( Side.RIGHT ), southView.getEdge( Side.LEFT ) );
	//		assertEquals( "Common edge not linked.", westView.getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
	//
	//		workpane.pullMerge( westView, Side.LEFT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.LEFT ), northView.getEdge( Side.LEFT ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.LEFT ), southView.getEdge( Side.LEFT ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.LEFT ), toolview.getEdge( Side.LEFT ) );
	//		assertEquals( 3, workpane.getEndEdge( Side.LEFT ).getViews( Side.RIGHT ).size() );
	//	}
	//
	//	@Test
	//	public void testPullMergeWestMultipleSourceMultipleTarget() throws Exception {
	//		workpane.split( toolview, Side.TOP );
	//		workpane.split( toolview, Side.BOTTOM );
	//		ToolView westView = workpane.split( toolview, Side.LEFT );
	//		ToolView northEastView = workpane.split( toolview, Side.RIGHT );
	//		ToolView southEastView = workpane.split( northEastView, Side.BOTTOM );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( westView );
	//
	//		int initialViewCount = 6;
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 1, workpane.getToolViewEdges().size() );
	//
	//		// Check the north east view.
	//		assertEquals( northEastView.getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//
	//		// Check the south east view.
	//		assertEquals( southEastView.getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//
	//		// Merge the east views into the tool view area.
	//		workpane.pullMerge( toolview, Side.LEFT );
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount - 1, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 2, workpane.getToolViewEdges().size() );
	//
	//		// Check the north east view.
	//		assertEquals( northEastView.getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
	//		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//
	//		// Check the south east view.
	//		assertEquals( southEastView.getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//	}
	//
	//	@Test
	//	public void testAutoMergeWest() throws Exception {
	//		ToolView view1 = workpane.split( toolview, Side.RIGHT );
	//		Tool tool = new MockTool();
	//		Tool tool1 = new MockTool();
	//		workpane.addTool( tool, toolview );
	//		workpane.addTool( tool1, view1 );
	//
	//		workpane.closeTool( tool1 );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( 0, workpane.getToolViewEdges().size() );
	//		assertEquals( 1, toolview.getTools().size() );
	//	}
	//
	//	@Test
	//	public void testAutoMergeMergeWestWithMultipleViews() {
	//		ToolView southwest = workpane.split( toolview, Side.BOTTOM );
	//		ToolView southeast = workpane.split( southwest, Side.RIGHT );
	//		Tool view = new MockTool();
	//		Tool view1 = new MockTool();
	//		Tool view2 = new MockTool();
	//		workpane.addTool( view, toolview );
	//		workpane.addTool( view1, southwest );
	//		workpane.addTool( view2, southeast );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		workpane.closeTool( view1 );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//		assertTrue( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( southeast ) );
	//	}

}
