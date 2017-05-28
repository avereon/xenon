package com.parallelsymmetry.essence.workarea;

import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class WorkpaneMergeSouthTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeSouthSingleTargetSingleSource() throws Exception {
		ToolView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
		assertFalse( workpane.canPushMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthSingleTargetSingleSourceOnEdge() throws Exception {
		ToolView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
		assertFalse( workpane.canPushMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthSingleTargetMultipleSource() throws Exception {
		ToolView north = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthMultipleTargetSingleSource() throws Exception {
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthMultipleTargetMultipleSource() throws Exception {
		ToolView north = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthComplex() throws Exception {
		ToolView north = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		ToolView northwest = workpane.split( north, Side.LEFT );
		ToolView northeast = workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( northwest, Side.TOP );
		workpane.split( northeast, Side.TOP );
		assertThat( workpane.getViews().size(), is( 8 ) );
		assertFalse( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
	}

	// TODO Continue implementing WorkpaneMergeSouth tests

	//	@Test
	//	public void testPushMergeSouthSingleTargetSingleSource() throws Exception {
	//		ToolView north = workpane.split( toolview, Side.TOP );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( toolview, workpane.getDefaultView() );
	//		assertEquals( toolview, workpane.getActiveView() );
	//		assertFalse( north.equals( workpane.getActiveView() ) );
	//
	//		workpane.setActiveView( north );
	//		workpane.setDefaultView( north );
	//		assertEquals( north, workpane.getActiveView() );
	//		assertEquals( north, workpane.getDefaultView() );
	//
	//		Tool view = new MockTool();
	//		Tool view1 = new MockTool();
	//		workpane.addTool( view, toolview );
	//		workpane.addTool( view1, north );
	//
	//		workpane.pushMerge( north, Side.BOTTOM );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( workpane, north.getWorkPane() );
	//		assertNull( toolview.getWorkPane() );
	//		assertEquals( north, workpane.getDefaultView() );
	//		assertEquals( north, workpane.getActiveView() );
	//		assertEquals( 2, north.getTools().size() );
	//
	//		assertEquals( 0.0, north.northEdge.position );
	//		assertEquals( 1.0, north.southEdge.position );
	//		assertEquals( 0.0, north.westEdge.position );
	//		assertEquals( 1.0, north.eastEdge.position );
	//		assertEquals( north.getSize(), workpane.getSize() );
	//	}
	//
	//	@Test
	//	public void testPushMergeSouthSingleTargetMultipleSource() throws Exception {
	//		ToolView north = workpane.split( toolview, Side.TOP );
	//		workpane.split( toolview, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		workpane.setDefaultView( north );
	//
	//		workpane.pushMerge( north, Side.BOTTOM );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( 0, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( north ) );
	//		assertEquals( north, workpane.getDefaultView() );
	//	}
	//
	//	@Test
	//	public void testPushMergeSouthMultipleTargetSingleSourceFromNorthwest() throws Exception {
	//		ToolView northwest = workpane.split( toolview, Side.TOP );
	//		ToolView northeast = workpane.split( northwest, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( northwest );
	//
	//		workpane.pushMerge( northwest, Side.BOTTOM );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northwest ) );
	//		assertTrue( workpane.getViews().contains( northeast ) );
	//
	//		assertEquals( northwest.northEdge, workpane.northEdge );
	//		assertEquals( northwest.southEdge, workpane.southEdge );
	//		assertEquals( northwest.westEdge, workpane.westEdge );
	//		assertEquals( northwest.eastEdge, northeast.westEdge );
	//
	//		assertEquals( northeast.northEdge, workpane.northEdge );
	//		assertEquals( northeast.southEdge, workpane.southEdge );
	//		assertEquals( northeast.westEdge, northwest.eastEdge );
	//		assertEquals( northeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( northwest.eastEdge.northEdge, workpane.northEdge );
	//		assertEquals( northwest.eastEdge.southEdge, workpane.southEdge );
	//	}
	//
	//	@Test
	//	public void testPushMergeSouthMultipleTargetSingleSourceFromNortheast() throws Exception {
	//		ToolView northwest = workpane.split( toolview, Side.TOP );
	//		ToolView northeast = workpane.split( northwest, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( northeast );
	//
	//		workpane.pushMerge( northeast, Side.BOTTOM );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northwest ) );
	//		assertTrue( workpane.getViews().contains( northeast ) );
	//
	//		assertEquals( northwest.northEdge, workpane.northEdge );
	//		assertEquals( northwest.southEdge, workpane.southEdge );
	//		assertEquals( northwest.westEdge, workpane.westEdge );
	//		assertEquals( northwest.eastEdge, northeast.westEdge );
	//
	//		assertEquals( northeast.northEdge, workpane.northEdge );
	//		assertEquals( northeast.southEdge, workpane.southEdge );
	//		assertEquals( northeast.westEdge, northwest.eastEdge );
	//		assertEquals( northeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( northwest.eastEdge.northEdge, workpane.northEdge );
	//		assertEquals( northwest.eastEdge.southEdge, workpane.southEdge );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeSouthSingleTargetSingleSource() throws Exception {
	//		ToolView view = workpane.split( toolview, Side.BOTTOM );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.BOTTOM, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.BOTTOM, false ) );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeSouthAcrossEditView() throws Exception {
	//		ToolView view = workpane.split( Side.BOTTOM );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.BOTTOM, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.BOTTOM, false ) );
	//	}
	//
	//	@Test
	//	public void testPullMergeSouthMultipleSourceEdgeTarget() throws Exception {
	//		ToolView westView = workpane.split( Side.LEFT );
	//		ToolView eastView = workpane.split( Side.RIGHT );
	//		ToolView southView = workpane.split( Side.BOTTOM );
	//		assertEquals( 4, workpane.getViews().size() );
	//		assertEquals( 3, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", southView.getEdge( Side.TOP ), westView.getEdge( Side.BOTTOM ) );
	//		assertEquals( "Common edge not linked.", southView.getEdge( Side.TOP ), eastView.getEdge( Side.BOTTOM ) );
	//		assertEquals( "Common edge not linked.", southView.getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
	//
	//		workpane.pullMerge( southView, Side.BOTTOM );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.BOTTOM ), westView.getEdge( Side.BOTTOM ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.BOTTOM ), eastView.getEdge( Side.BOTTOM ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.BOTTOM ), toolview.getEdge( Side.BOTTOM ) );
	//		assertEquals( 3, workpane.getEndEdge( Side.BOTTOM ).getViews( Side.TOP ).size() );
	//	}
	//
	//	@Test
	//	public void testPullMergeSouthMultipleSourceMultipleTarget() throws Exception {
	//		workpane.split( toolview, Side.LEFT );
	//		workpane.split( toolview, Side.RIGHT );
	//		ToolView northWestView = workpane.split( toolview, Side.TOP );
	//		ToolView northEastView = workpane.split( northWestView, Side.RIGHT );
	//		ToolView southView = workpane.split( toolview, Side.BOTTOM );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( southView );
	//
	//		int initialViewCount = 6;
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 1, workpane.getToolViewEdges().size() );
	//
	//		// Check the north west view.
	//		assertEquals( northWestView.getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//
	//		// Check the north east view.
	//		assertEquals( northEastView.getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//
	//		// Merge the north views into the tool view area.
	//		workpane.pullMerge( toolview, Side.BOTTOM );
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount - 1, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 2, workpane.getToolViewEdges().size() );
	//
	//		// Check the north west view.
	//		assertEquals( northWestView.getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
	//		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//
	//		// Check the north east view.
	//		assertEquals( northEastView.getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//	}
	//
	//	@Test
	//	public void testAutoMergeSouth() throws Exception {
	//		ToolView view1 = workpane.split( toolview, Side.TOP );
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
	//	public void testAutoMergeMergeSouthWithMultipleViews() {
	//		ToolView northeast = workpane.split( toolview, Side.RIGHT );
	//		ToolView southeast = workpane.split( northeast, Side.BOTTOM );
	//		Tool view = new MockTool();
	//		Tool view1 = new MockTool();
	//		Tool view2 = new MockTool();
	//		workpane.addTool( view, toolview );
	//		workpane.addTool( view1, northeast );
	//		workpane.addTool( view2, southeast );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		workpane.closeTool( view2 );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//		assertTrue( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( northeast ) );
	//	}

}
