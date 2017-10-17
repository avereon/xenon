package com.xeomar.xenon.workarea;

import com.xeomar.xenon.worktool.Tool;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class WorkpaneMergeEastTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeEastSingleTargetSingleSource() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.RIGHT );
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
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
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
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
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
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		WorkpaneView northeast = workpane.split( east, Side.TOP );
		WorkpaneView southeast = workpane.split( east, Side.BOTTOM );
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

	@Test
	public void testPushMergeEastSingleTargetSingleSource() throws Exception {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertFalse( west.equals( workpane.getActiveView() ) );

		workpane.setActiveView( west );
		workpane.setDefaultView( west );
		assertThat( workpane.getActiveView(), is( west ) );
		assertThat( workpane.getDefaultView(), is( west ) );

		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, west );

		workpane.pushMerge( west, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( west.getWorkpane(), is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( west ) );
		assertThat( workpane.getActiveView(), is( west ) );
		assertThat( west.getTools().size(), is( 2 ) );

		assertThat( west.topEdge.getPosition(), is( 0d ) );
		assertThat( west.bottomEdge.getPosition(), is( 1d ) );
		assertThat( west.leftEdge.getPosition(), is( 0d ) );
		assertThat( west.rightEdge.getPosition(), is( 1d ) );

		assertEquals( west.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	public void testPushMergeEastSingleTargetMultipleSource() throws Exception {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.setDefaultView( west );
		workpane.pushMerge( west, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( west ) );
		assertThat( workpane.getDefaultView(), is( west ) );
	}

	@Test
	public void testPushMergeEastMultipleTargetSingleSourceFromNorthwest() throws Exception {
		WorkpaneView northwest = workpane.split( toolview, Side.LEFT );
		WorkpaneView southwest = workpane.split( northwest, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northwest );
		workpane.pushMerge( northwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( northwest ) );
		assertThat( workpane.getViews(), hasItem( southwest ) );

		assertEquals( northwest.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.bottomEdge, southwest.topEdge );
		assertEquals( northwest.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.rightEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.topEdge, northwest.bottomEdge );
		assertEquals( southwest.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.rightEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.bottomEdge.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.bottomEdge.rightEdge, workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	public void testPushMergeEastMultipleTargetSingleSourceFromSouthwest() throws Exception {
		WorkpaneView northwest = workpane.split( toolview, Side.LEFT );
		WorkpaneView southwest = workpane.split( northwest, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southwest );
		workpane.pushMerge( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( northwest ) );
		assertThat( workpane.getViews(), hasItem( southwest ) );

		assertEquals( northwest.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.bottomEdge, southwest.topEdge );
		assertEquals( northwest.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.rightEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.topEdge, northwest.bottomEdge );
		assertEquals( southwest.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.rightEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.bottomEdge.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.bottomEdge.rightEdge, workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	public void testCanPullMergeEastSingleTargetSingleSource() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.RIGHT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPullMergeEastAcrossEditView() throws Exception {
		WorkpaneView view = workpane.split( Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.RIGHT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.RIGHT, false ) );
	}

	@Test
	public void testPullMergeEastMultipleSourceEdgeTarget() throws Exception {
		WorkpaneView northView = workpane.split( Side.TOP );
		WorkpaneView southView = workpane.split( Side.BOTTOM );
		WorkpaneView eastView = workpane.split( Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertThat( workpane.getEdges().size(), is( 3 ) );
		assertThat( "Common edge not linked.", northView.getEdge( Side.RIGHT ), is( eastView.getEdge( Side.LEFT ) ) );
		assertThat( "Common edge not linked.", southView.getEdge( Side.RIGHT ), is( eastView.getEdge( Side.LEFT ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.RIGHT ), is( eastView.getEdge( Side.LEFT ) ) );

		workpane.pullMerge( eastView, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );
		assertThat( "Common edge not linked.", northView.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( "Common edge not linked.", southView.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ).getViews( Side.LEFT ).size(), is( 3 ) );
	}

	@Test
	public void testPullMergeEastMultipleSourceMultipleTarget() throws Exception {
		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		WorkpaneView eastView = workpane.split( toolview, Side.RIGHT );
		WorkpaneView northWestView = workpane.split( toolview, Side.LEFT );
		WorkpaneView southWestView = workpane.split( northWestView, Side.BOTTOM );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( eastView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertEquals( initialViewCount, workpane.getViews().size() );
		assertEquals( initialViewCount - 1, workpane.getEdges().size() );

		// Check the north west view.
		assertEquals( northWestView.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );

		// Check the south west view.
		assertEquals( southWestView.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), toolview.getEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		// Merge the west views into the tool view area.
		workpane.pullMerge( toolview, Side.RIGHT );

		// Check the view and edge counts.
		assertEquals( initialViewCount - 1, workpane.getViews().size() );
		assertEquals( initialViewCount - 2, workpane.getEdges().size() );

		// Check the north west view.
		assertEquals( northWestView.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );

		// Check the south west view.
		assertEquals( southWestView.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), eastView.getEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	public void testAutoMergeEast() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		Tool tool = new MockTool( resource );
		Tool tool1 = new MockTool( resource );

		workpane.addTool( tool, toolview );
		workpane.addTool( tool1, view1 );
		workpane.closeTool( tool1 );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( toolview.getTools().size(), is( 1 ) );
	}

	@Test
	public void testAutoMergeMergeEastWithMultipleViews() {
		WorkpaneView southwest = workpane.split( toolview, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		Tool view2 = new MockTool( resource );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, southwest );
		workpane.addTool( view2, southeast );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.closeTool( view2 );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), hasItem( toolview ) );
		assertThat( workpane.getViews(), hasItem( southwest ) );
	}

}
