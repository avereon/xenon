package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkpaneMergeWestTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeWestSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.RIGHT, false ) );
	}

	@Test
	void testCanPushMergeWestSingleTargetSingleSourceOnEdge() {
		WorkpaneView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.LEFT, false ) );
	}

	@Test
	void testCanPushMergeWestSingleTargetMultipleSource() {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( west, Side.TOP );
		workpane.split( west, Side.BOTTOM );
		assertEquals( 4, workpane.getViews().size() );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
	}

	@Test
	void testCanPushMergeWestMultipleTargetSingleSource() {
		workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
	}

	@Test
	void testCanPushMergeWestMultipleTargetMultipleSource() {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
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
	void testCanPushMergeWestComplex() {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );

		WorkpaneView northwest = workpane.split( west, Side.TOP );
		WorkpaneView southwest = workpane.split( west, Side.BOTTOM );
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

	@Test
	void testPushMergeWestSingleTargetSingleSource() {
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertNotEquals( east, workpane.getActiveView() );

		workpane.setActiveView( east );
		workpane.setDefaultView( east );
		assertThat( workpane.getActiveView(), is( east ) );
		assertThat( workpane.getDefaultView(), is( east ) );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, east );

		workpane.pushMerge( east, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( east.getWorkpane(), is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( east ) );
		assertThat( workpane.getActiveView(), is( east ) );
		assertThat( east.getTools().size(), is( 2 ) );

		assertThat( east.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( east.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( east.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( east.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertEquals( east.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	void testPushMergeWestSingleTargetMultipleSource() {
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.setDefaultView( east );
		workpane.pushMerge( east, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( east ) );
		assertThat( workpane.getDefaultView(), is( east ) );
	}

	@Test
	void testPushMergeWestMultipleTargetSingleSourceFromNortheast() {
		WorkpaneView northeast = workpane.split( toolview, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northeast );
		workpane.pushMerge( northeast, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( northeast ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );

		assertEquals( northeast.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.getEdge( Side.BOTTOM ), southeast.getEdge( Side.TOP ) );
		assertEquals( northeast.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southeast.getEdge( Side.TOP ), northeast.getEdge( Side.BOTTOM ) );
		assertEquals( southeast.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northeast.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testPushMergeWestMultipleTargetSingleSourceFromSoutheast() {
		WorkpaneView northeast = workpane.split( toolview, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southeast );
		workpane.pushMerge( southeast, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( northeast ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );

		assertEquals( northeast.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.getEdge( Side.BOTTOM ), southeast.getEdge( Side.TOP ) );
		assertEquals( northeast.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southeast.getEdge( Side.TOP ), northeast.getEdge( Side.BOTTOM ) );
		assertEquals( southeast.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northeast.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testCanPullMergeWestSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.LEFT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.LEFT, false ) );
	}

	@Test
	void testCanPullMergeWestAcrossEditView() {
		WorkpaneView view = workpane.split( Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.LEFT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.LEFT, false ) );
	}

	@Test
	void testPullMergeWestMultipleSourceEdgeTarget() {
		WorkpaneView northView = workpane.split( Side.TOP );
		WorkpaneView southView = workpane.split( Side.BOTTOM );
		WorkpaneView westView = workpane.split( Side.LEFT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertThat( workpane.getEdges().size(), is( 3 ) );
		assertThat( "Common edge not linked.", northView.getEdge( Side.LEFT ), is( westView.getEdge( Side.RIGHT ) ) );
		assertThat( "Common edge not linked.", southView.getEdge( Side.LEFT ), is( westView.getEdge( Side.RIGHT ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.LEFT ), is( westView.getEdge( Side.RIGHT ) ) );

		workpane.pullMerge( westView, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );
		assertThat( "Common edge not linked.", northView.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( "Common edge not linked.", southView.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( workpane.getWallEdge( Side.LEFT ).getViews( Side.RIGHT ).size(), is( 3 ) );
	}

	@Test
	void testPullMergeWestMultipleSourceMultipleTarget() {
		workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.BOTTOM );
		WorkpaneView westView = workpane.split( toolview, Side.LEFT );
		WorkpaneView northEastView = workpane.split( toolview, Side.RIGHT );
		WorkpaneView southEastView = workpane.split( northEastView, Side.BOTTOM );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( westView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertEquals( initialViewCount, workpane.getViews().size() );
		assertEquals( initialViewCount - 1, workpane.getEdges().size() );

		// Check the north east view.
		assertEquals( northEastView.getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		// Check the south east view.
		assertEquals( southEastView.getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), toolview.getEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		// Merge the east views into the tool view area.
		workpane.pullMerge( toolview, Side.LEFT );

		// Check the view and edge counts.
		assertEquals( initialViewCount - 1, workpane.getViews().size() );
		assertEquals( initialViewCount - 2, workpane.getEdges().size() );

		// Check the north east view.
		assertEquals( northEastView.getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
		assertEquals( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		// Check the south east view.
		assertEquals( southEastView.getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ), westView.getEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	void testAutoMergeWest() {
		WorkpaneView view1 = workpane.split( toolview, Side.RIGHT );
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
	void testAutoMergeMergeWestWithMultipleViews() {
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

		workpane.closeTool( view1 );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), hasItem( toolview ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );
	}

}
