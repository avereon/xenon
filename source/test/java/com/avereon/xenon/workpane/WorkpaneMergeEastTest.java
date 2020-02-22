package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkpaneMergeEastTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeEastSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.RIGHT, false ) );
	}

	@Test
	void testCanPushMergeEastSingleTargetSingleSourceOnEdge() {
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	void testCanPushMergeEastSingleTargetMultipleSource() {
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( east, Side.TOP );
		workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	void testCanPushMergeEastMultipleTargetSingleSource() {
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.RIGHT, false ) );
	}

	@Test
	void testCanPushMergeEastMultipleTargetMultipleSource() {
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
	void testCanPushMergeEastComplex() {
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
	void testPushMergeEastSingleTargetSingleSource() {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertNotEquals( west, workpane.getActiveView() );

		workpane.setActiveView( west );
		workpane.setDefaultView( west );
		assertThat( workpane.getActiveView(), is( west ) );
		assertThat( workpane.getDefaultView(), is( west ) );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, west );

		workpane.pushMerge( west, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( west.getWorkpane(), is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( west ) );
		assertThat( workpane.getActiveView(), is( west ) );
		assertThat( west.getTools().size(), is( 2 ) );

		assertThat( west.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( west.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( west.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( west.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertEquals( west.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	void testPushMergeEastSingleTargetMultipleSource() {
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
	void testPushMergeEastMultipleTargetSingleSourceFromNorthwest() {
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

		assertEquals( northwest.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.BOTTOM ), southwest.getEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.getEdge( Side.TOP ), northwest.getEdge( Side.BOTTOM ) );
		assertEquals( southwest.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testPushMergeEastMultipleTargetSingleSourceFromSouthwest() {
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

		assertEquals( northwest.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.BOTTOM ), southwest.getEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.getEdge( Side.TOP ), northwest.getEdge( Side.BOTTOM ) );
		assertEquals( southwest.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testCanPullMergeEastSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.RIGHT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.RIGHT, false ) );
	}

	@Test
	void testCanPullMergeEastAcrossEditView() {
		WorkpaneView view = workpane.split( Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.RIGHT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.RIGHT, false ) );
	}

	@Test
	void testPullMergeEastMultipleSourceEdgeTarget() {
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
	void testPullMergeEastMultipleSourceMultipleTarget() {
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
	void testAutoMergeEast() {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		Tool tool = new MockTool( asset );
		Tool tool1 = new MockTool( asset );

		workpane.addTool( tool, toolview );
		workpane.addTool( tool1, view1 );
		workpane.closeTool( tool1 );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( toolview.getTools().size(), is( 1 ) );
	}

	@Test
	void testAutoMergeMergeEastWithMultipleViews() {
		WorkpaneView southwest = workpane.split( toolview, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		Tool view2 = new MockTool( asset );
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
