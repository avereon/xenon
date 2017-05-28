package com.parallelsymmetry.essence.workarea;

import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class WorkpaneMergeEastTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeEastSingleTargetSingleSource() throws Exception {
		ToolView view = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeEastSingleTargetSingleSourceOnEdge() throws Exception {
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeEastSingleTargetMultipleSource() throws Exception {
		ToolView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( east, Side.TOP );
		workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeEastMultipleTargetSingleSource() throws Exception {
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeEastMultipleTargetMultipleSource() throws Exception {
		ToolView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( east, Side.TOP );
		workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeEastComplex() throws Exception {
		ToolView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		ToolView northeast = workpane.split( east, Side.TOP );
		ToolView southeast = workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( northeast, Side.RIGHT );
		workpane.split( southeast, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 8 ) );
		assertFalse( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	// TODO Continue implementing WorkpaneMergeEast tests

	//	@Test
	//	public void testPushMergeEastSingleTargetSingleSource() throws Exception {
	//		ToolView west = workpane.split( toolview, Side.LEFT );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( toolview, workpane.getDefaultView() );
	//		assertEquals( toolview, workpane.getActiveView() );
	//		assertFalse( west.equals( workpane.getActiveView() ) );
	//
	//		workpane.setActiveView( west );
	//		workpane.setDefaultView( west );
	//		assertEquals( west, workpane.getActiveView() );
	//		assertEquals( west, workpane.getDefaultView() );
	//
	//		Tool view = new MockTool();
	//		Tool view1 = new MockTool();
	//		workpane.addTool( view, toolview );
	//		workpane.addTool( view1, west );
	//
	//		workpane.pushMerge( west, Side.RIGHT );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( workpane, west.getWorkPane() );
	//		assertNull( toolview.getWorkPane() );
	//		assertEquals( west, workpane.getDefaultView() );
	//		assertEquals( west, workpane.getActiveView() );
	//		assertEquals( 2, west.getTools().size() );
	//
	//		assertEquals( 0.0, west.northEdge.position );
	//		assertEquals( 1.0, west.southEdge.position );
	//		assertEquals( 0.0, west.westEdge.position );
	//		assertEquals( 1.0, west.eastEdge.position );
	//		assertEquals( west.getSize(), workpane.getSize() );
	//	}
	//
	//	@Test
	//	public void testPushMergeEastSingleTargetMultipleSource() throws Exception {
	//		ToolView west = workpane.split( toolview, Side.LEFT );
	//		workpane.split( toolview, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		workpane.setDefaultView( west );
	//
	//		workpane.pushMerge( west, Side.RIGHT );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( 0, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( west ) );
	//		assertEquals( west, workpane.getDefaultView() );
	//	}
	//
	//	@Test
	//	public void testPushMergeEastMultipleTargetSingleSourceFromNorthwest() throws Exception {
	//		ToolView northwest = workpane.split( toolview, Side.LEFT );
	//		ToolView southwest = workpane.split( northwest, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( northwest );
	//
	//		workpane.pushMerge( northwest, Side.RIGHT );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northwest ) );
	//		assertTrue( workpane.getViews().contains( southwest ) );
	//
	//		assertEquals( northwest.northEdge, workpane.northEdge );
	//		assertEquals( northwest.southEdge, southwest.northEdge );
	//		assertEquals( northwest.westEdge, workpane.westEdge );
	//		assertEquals( northwest.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( southwest.northEdge, northwest.southEdge );
	//		assertEquals( southwest.southEdge, workpane.southEdge );
	//		assertEquals( southwest.westEdge, workpane.westEdge );
	//		assertEquals( southwest.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( northwest.southEdge.westEdge, workpane.westEdge );
	//		assertEquals( northwest.southEdge.eastEdge, workpane.eastEdge );
	//	}
	//
	//	@Test
	//	public void testPushMergeEastMultipleTargetSingleSourceFromSouthwest() throws Exception {
	//		ToolView northwest = workpane.split( toolview, Side.LEFT );
	//		ToolView southwest = workpane.split( northwest, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( southwest );
	//
	//		workpane.pushMerge( southwest, Side.RIGHT );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northwest ) );
	//		assertTrue( workpane.getViews().contains( southwest ) );
	//
	//		assertEquals( northwest.northEdge, workpane.northEdge );
	//		assertEquals( northwest.southEdge, southwest.northEdge );
	//		assertEquals( northwest.westEdge, workpane.westEdge );
	//		assertEquals( northwest.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( southwest.northEdge, northwest.southEdge );
	//		assertEquals( southwest.southEdge, workpane.southEdge );
	//		assertEquals( southwest.westEdge, workpane.westEdge );
	//		assertEquals( southwest.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( northwest.southEdge.westEdge, workpane.westEdge );
	//		assertEquals( northwest.southEdge.eastEdge, workpane.eastEdge );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeEastSingleTargetSingleSource() throws Exception {
	//		ToolView view = workpane.split( toolview, Side.RIGHT );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.RIGHT, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.RIGHT, false ) );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeEastAcrossEditView() throws Exception {
	//		ToolView view = workpane.split( Side.RIGHT );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.RIGHT, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.RIGHT, false ) );
	//	}
	//
	//	@Test
	//	public void testPullMergeEastMultipleSourceEdgeTarget() throws Exception {
	//		ToolView northView = workpane.split( Side.TOP );
	//		ToolView southView = workpane.split( Side.BOTTOM );
	//		ToolView eastView = workpane.split( Side.RIGHT );
	//		assertEquals( 4, workpane.getViews().size() );
	//		assertEquals( 3, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", eastView.getEdge( Side.LEFT ), northView.getEdge( Side.RIGHT ) );
	//		assertEquals( "Common edge not linked.", eastView.getEdge( Side.LEFT ), southView.getEdge( Side.RIGHT ) );
	//		assertEquals( "Common edge not linked.", eastView.getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
	//
	//		workpane.pullMerge( eastView, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.RIGHT ), northView.getEdge( Side.RIGHT ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.RIGHT ), southView.getEdge( Side.RIGHT ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.RIGHT ), toolview.getEdge( Side.RIGHT ) );
	//		assertEquals( 3, workpane.getEndEdge( Side.RIGHT ).getViews( Side.LEFT ).size() );
	//	}
	//
	//	@Test
	//	public void testPullMergeEastMultipleSourceMultipleTarget() throws Exception {
	//		workpane.split( toolview, Side.TOP );
	//		workpane.split( toolview, Side.BOTTOM );
	//		ToolView eastView = workpane.split( toolview, Side.RIGHT );
	//		ToolView northWestView = workpane.split( toolview, Side.LEFT );
	//		ToolView southWestView = workpane.split( northWestView, Side.BOTTOM );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( eastView );
	//
	//		int initialViewCount = 6;
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 1, workpane.getToolViewEdges().size() );
	//
	//		// Check the north west view.
	//		assertEquals( northWestView.getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
	//
	//		// Check the south west view.
	//		assertEquals( southWestView.getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//
	//		// Merge the west views into the tool view area.
	//		workpane.pullMerge( toolview, Side.RIGHT );
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount - 1, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 2, workpane.getToolViewEdges().size() );
	//
	//		// Check the north west view.
	//		assertEquals( northWestView.getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
	//
	//		// Check the south west view.
	//		assertEquals( southWestView.getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getEndEdge( Side.LEFT ) );
	//		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getEndEdge( Side.RIGHT ) );
	//	}
	//
	//	@Test
	//	public void testAutoMergeEast() throws Exception {
	//		ToolView view1 = workpane.split( toolview, Side.LEFT );
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
	//	public void testAutoMergeMergeEastWithMultipleViews() {
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
	//		workpane.closeTool( view2 );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//		assertTrue( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( southwest ) );
	//	}

}
