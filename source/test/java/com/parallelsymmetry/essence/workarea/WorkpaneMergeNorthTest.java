package com.parallelsymmetry.essence.workarea;

import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class WorkpaneMergeNorthTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeNorthSingleTargetSingleSource() throws Exception {
		ToolView view = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
		assertFalse( workpane.canPushMerge( view, Side.TOP, false ) );
	}

	@Test
	public void testCanPushMergeNorthSingleTargetSingleSourceOnEdge() throws Exception {
		ToolView view = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
		assertFalse( workpane.canPushMerge( view, Side.TOP, false ) );
	}

	@Test
	public void testCanPushMergeNorthSingleTargetMultipleSource() throws Exception {
		ToolView north = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
	}

	@Test
	public void testCanPushMergeNorthMultipleTargetSingleSource() throws Exception {
		workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
	}

	@Test
	public void testCanPushMergeNorthMultipleTargetMultipleSource() throws Exception {
		ToolView north = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 6 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
	}

		@Test
		public void testCanPushMergeNorthComplex() throws Exception {
			ToolView north = workpane.split( toolview, Side.TOP );
			assertThat( workpane.getViews().size(), is( 2 ) );
			assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

			ToolView northwest = workpane.split( north, Side.LEFT );
			ToolView northeast = workpane.split( north, Side.RIGHT );
			assertThat( workpane.getViews().size(), is( 4 ) );
			assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

			workpane.split( toolview, Side.LEFT );
			workpane.split( toolview, Side.RIGHT );
			assertThat( workpane.getViews().size(), is( 6 ) );
			assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

			workpane.split( northwest, Side.BOTTOM );
			workpane.split( northeast, Side.BOTTOM );
			assertThat( workpane.getViews().size(), is( 8 ) );
			assertFalse( workpane.canPushMerge( toolview, Side.TOP, false ) );
		}

	// TODO Continue implementing WorkpaneMergeNorth tests

	//	@Test
	//	public void testPushMergeNorthSingleTargetSingleSource() throws Exception {
	//		ToolView south = workpane.split( toolview, Side.BOTTOM );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( toolview, workpane.getDefaultView() );
	//		assertEquals( toolview, workpane.getActiveView() );
	//		assertFalse( south.isActive() );
	//		assertEquals( workpane, south.getWorkPane() );
	//		south.putClientProperty( "name", "view1" );
	//
	//		Tool view = new MockTool();
	//		Tool view1 = new MockTool();
	//		workpane.addTool( view, toolview );
	//		workpane.addTool( view1, south );
	//
	//		workpane.setActiveView( south );
	//		workpane.setDefaultView( south );
	//		assertEquals( south, workpane.getActiveView() );
	//		assertEquals( south, workpane.getDefaultView() );
	//
	//		workpane.pushMerge( south, Side.TOP );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( workpane, south.getWorkPane() );
	//		assertNull( toolview.getWorkPane() );
	//		assertEquals( south, workpane.getDefaultView() );
	//		assertEquals( south, workpane.getActiveView() );
	//		assertEquals( 2, south.getTools().size() );
	//
	//		assertEquals( 0.0, south.northEdge.position );
	//		assertEquals( 1.0, south.southEdge.position );
	//		assertEquals( 0.0, south.westEdge.position );
	//		assertEquals( 1.0, south.eastEdge.position );
	//	}
	//
	//	@Test
	//	public void testPushMergeNorthSingleTargetMultipleSource() throws Exception {
	//		ToolView south = workpane.split( toolview, Side.BOTTOM );
	//		workpane.split( toolview, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		workpane.setDefaultView( south );
	//
	//		workpane.pushMerge( south, Side.TOP );
	//		assertEquals( 1, workpane.getViews().size() );
	//		assertEquals( 0, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( south ) );
	//		assertEquals( south, workpane.getDefaultView() );
	//	}
	//
	//	@Test
	//	public void testPushMergeNorthMultipleTargetSingleSourceFromSouthwest() throws Exception {
	//		ToolView southwest = workpane.split( toolview, Side.BOTTOM );
	//		ToolView southeast = workpane.split( southwest, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( southwest );
	//
	//		workpane.pushMerge( southwest, Side.TOP );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( southwest ) );
	//		assertTrue( workpane.getViews().contains( southeast ) );
	//
	//		assertEquals( southwest.northEdge, workpane.northEdge );
	//		assertEquals( southwest.southEdge, workpane.southEdge );
	//		assertEquals( southwest.westEdge, workpane.westEdge );
	//		assertEquals( southwest.eastEdge, southeast.westEdge );
	//
	//		assertEquals( southeast.northEdge, workpane.northEdge );
	//		assertEquals( southeast.southEdge, workpane.southEdge );
	//		assertEquals( southeast.westEdge, southwest.eastEdge );
	//		assertEquals( southeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( southwest.eastEdge.northEdge, workpane.northEdge );
	//		assertEquals( southwest.eastEdge.southEdge, workpane.southEdge );
	//	}
	//
	//	@Test
	//	public void testPushMergeNorthMultipleTargetSingleSourceFromSoutheast() throws Exception {
	//		ToolView southwest = workpane.split( toolview, Side.BOTTOM );
	//		ToolView southeast = workpane.split( southwest, Side.RIGHT );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( southeast );
	//
	//		workpane.pushMerge( southeast, Side.TOP );
	//
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//
	//		assertFalse( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( southwest ) );
	//		assertTrue( workpane.getViews().contains( southeast ) );
	//
	//		assertEquals( southwest.northEdge, workpane.northEdge );
	//		assertEquals( southwest.southEdge, workpane.southEdge );
	//		assertEquals( southwest.westEdge, workpane.westEdge );
	//		assertEquals( southwest.eastEdge, southeast.westEdge );
	//
	//		assertEquals( southeast.northEdge, workpane.northEdge );
	//		assertEquals( southeast.southEdge, workpane.southEdge );
	//		assertEquals( southeast.westEdge, southwest.eastEdge );
	//		assertEquals( southeast.eastEdge, workpane.eastEdge );
	//
	//		assertEquals( southwest.eastEdge.northEdge, workpane.northEdge );
	//		assertEquals( southwest.eastEdge.southEdge, workpane.southEdge );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeNorthSingleTargetSingleSource() throws Exception {
	//		ToolView view = workpane.split( toolview, Side.TOP );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.TOP, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.TOP, false ) );
	//	}
	//
	//	@Test
	//	public void testCanPullMergeNorthAcrossEditView() throws Exception {
	//		ToolView view = workpane.split( Side.TOP );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertFalse( workpane.canPullMerge( toolview, Side.TOP, false ) );
	//		assertTrue( workpane.canPullMerge( view, Side.TOP, false ) );
	//	}
	//
	//	@Test
	//	public void testPullMergeNorthMultipleSourceEdgeTarget() throws Exception {
	//		ToolView westView = workpane.split( Side.LEFT );
	//		ToolView eastView = workpane.split( Side.RIGHT );
	//		ToolView northView = workpane.split( Side.TOP );
	//		assertEquals( 4, workpane.getViews().size() );
	//		assertEquals( 3, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", northView.getEdge( Side.BOTTOM ), westView.getEdge( Side.TOP ) );
	//		assertEquals( "Common edge not linked.", northView.getEdge( Side.BOTTOM ), eastView.getEdge( Side.TOP ) );
	//		assertEquals( "Common edge not linked.", northView.getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
	//
	//		workpane.pullMerge( northView, Side.TOP );
	//		assertEquals( 3, workpane.getViews().size() );
	//		assertEquals( 2, workpane.getToolViewEdges().size() );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.TOP ), westView.getEdge( Side.TOP ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.TOP ), eastView.getEdge( Side.TOP ) );
	//		assertEquals( "Common edge not linked.", workpane.getEndEdge( Side.TOP ), toolview.getEdge( Side.TOP ) );
	//		assertEquals( 3, workpane.getEndEdge( Side.TOP ).getViews( Side.BOTTOM ).size() );
	//	}
	//
	//	@Test
	//	public void testPullMergeNorthMultipleSourceMultipleTarget() throws Exception {
	//		workpane.split( toolview, Side.LEFT );
	//		workpane.split( toolview, Side.RIGHT );
	//		ToolView northView = workpane.split( toolview, Side.TOP );
	//		ToolView southWestView = workpane.split( toolview, Side.BOTTOM );
	//		ToolView southEastView = workpane.split( southWestView, Side.RIGHT );
	//
	//		// Change the default view so the merge can happen.
	//		workpane.setDefaultView( northView );
	//
	//		int initialViewCount = 6;
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 1, workpane.getToolViewEdges().size() );
	//
	//		// Check the south west view.
	//		assertEquals( southWestView.getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//
	//		// Check the south east view.
	//		assertEquals( southEastView.getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//
	//		// Merge the south views into the tool view area.
	//		workpane.pullMerge( toolview, Side.TOP );
	//
	//		// Check the view and edge counts.
	//		assertEquals( initialViewCount - 1, workpane.getViews().size() );
	//		assertEquals( initialViewCount - 2, workpane.getToolViewEdges().size() );
	//
	//		// Check the south west view.
	//		assertEquals( southWestView.getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
	//		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//
	//		// Check the south east view.
	//		assertEquals( southEastView.getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getEndEdge( Side.TOP ) );
	//		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getEndEdge( Side.BOTTOM ) );
	//	}
	//
	//	@Test
	//	public void testAutoMergeNorth() throws Exception {
	//		ToolView view1 = workpane.split( toolview, Side.BOTTOM );
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
	//	public void testAutoMergeMergeNorthWithMultipleViews() {
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
	//		workpane.closeTool( view1 );
	//		assertEquals( 2, workpane.getViews().size() );
	//		assertEquals( 1, workpane.getToolViewEdges().size() );
	//		assertTrue( workpane.getViews().contains( toolview ) );
	//		assertTrue( workpane.getViews().contains( southeast ) );
	//	}

}
