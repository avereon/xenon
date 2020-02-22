package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

class WorkpaneMergeNorthTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeNorthSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
		assertFalse( workpane.canPushMerge( view, Side.TOP, false ) );
	}

	@Test
	void testCanPushMergeNorthSingleTargetSingleSourceOnEdge() {
		WorkpaneView view = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
		assertFalse( workpane.canPushMerge( view, Side.TOP, false ) );
	}

	@Test
	void testCanPushMergeNorthSingleTargetMultipleSource() {
		WorkpaneView north = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
	}

	@Test
	void testCanPushMergeNorthMultipleTargetSingleSource() {
		workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );
	}

	@Test
	void testCanPushMergeNorthMultipleTargetMultipleSource() {
		WorkpaneView north = workpane.split( toolview, Side.TOP );
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
	void testCanPushMergeNorthComplex() {
		WorkpaneView north = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.TOP, false ) );

		WorkpaneView northwest = workpane.split( north, Side.LEFT );
		WorkpaneView northeast = workpane.split( north, Side.RIGHT );
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

	@Test
	void testPushMergeNorthSingleTargetSingleSource() {
		WorkpaneView south = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertFalse( south.isActive() );
		assertThat( south.getWorkpane(), is( workpane ) );
		south.getProperties().put( "name", "view1" );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, south );

		workpane.setActiveView( south );
		workpane.setDefaultView( south );
		assertThat( workpane.getActiveView(), is( south ) );
		assertThat( workpane.getDefaultView(), is( south ) );

		workpane.pushMerge( south, Side.TOP );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( south.getWorkpane(), is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( south ) );
		assertThat( workpane.getActiveView(), is( south ) );
		assertThat( south.getTools().size(), is( 2 ) );

		assertThat( south.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( south.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( south.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( south.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertEquals( south.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	void testPushMergeNorthSingleTargetMultipleSource() {
		WorkpaneView south = workpane.split( toolview, Side.BOTTOM );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.setDefaultView( south );
		workpane.pushMerge( south, Side.TOP );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( workpane.getViews(), not( Matchers.hasItem( toolview ) ) );
		assertThat( workpane.getViews(), Matchers.hasItem( south ) );
		assertThat( workpane.getDefaultView(), is( south ) );
	}

	@Test
	void testPushMergeNorthMultipleTargetSingleSourceFromSouthwest() {
		WorkpaneView southwest = workpane.split( toolview, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );
		assertThat( workpane.getViews(), Matchers.hasItem( toolview ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southwest ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southeast ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southwest );
		workpane.pushMerge( southwest, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( Matchers.hasItem( toolview ) ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southwest ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southeast ) );

		assertEquals( southwest.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.getEdge( Side.RIGHT ), southeast.getEdge( Side.LEFT ) );

		assertEquals( southeast.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southeast.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.getEdge( Side.LEFT ), southwest.getEdge( Side.RIGHT ) );
		assertEquals( southeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testPushMergeNorthMultipleTargetSingleSourceFromSoutheast() {
		WorkpaneView southwest = workpane.split( toolview, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southeast );
		workpane.pushMerge( southeast, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( Matchers.hasItem( toolview ) ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southwest ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southeast ) );

		assertEquals( southwest.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.getEdge( Side.RIGHT ), southeast.getEdge( Side.LEFT ) );

		assertEquals( southeast.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southeast.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.getEdge( Side.LEFT ), southwest.getEdge( Side.RIGHT ) );
		assertEquals( southeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testCanPullMergeNorthSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.TOP, false ) );
		assertTrue( workpane.canPullMerge( view, Side.TOP, false ) );
	}

	@Test
	void testCanPullMergeNorthAcrossEditView() {
		WorkpaneView view = workpane.split( Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.TOP, false ) );
		assertTrue( workpane.canPullMerge( view, Side.TOP, false ) );
	}

	@Test
	void testPullMergeNorthMultipleSourceEdgeTarget() {
		WorkpaneView westView = workpane.split( Side.LEFT );
		WorkpaneView eastView = workpane.split( Side.RIGHT );
		WorkpaneView northView = workpane.split( Side.TOP );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertThat( workpane.getEdges().size(), is( 3 ) );
		assertThat( "Common edge not linked.", westView.getEdge( Side.TOP ), is( northView.getEdge( Side.BOTTOM ) ) );
		assertThat( "Common edge not linked.", eastView.getEdge( Side.TOP ), is( northView.getEdge( Side.BOTTOM ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.TOP ), is( northView.getEdge( Side.BOTTOM ) ) );

		workpane.pullMerge( northView, Side.TOP );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );
		assertThat( "Common edge not linked.", westView.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( "Common edge not linked.", eastView.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( workpane.getWallEdge( Side.TOP ).getViews( Side.BOTTOM ).size(), is( 3 ) );
	}

	@Test
	void testPullMergeNorthMultipleSourceMultipleTarget() {
		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		WorkpaneView northView = workpane.split( toolview, Side.TOP );
		WorkpaneView southWestView = workpane.split( toolview, Side.BOTTOM );
		WorkpaneView southEastView = workpane.split( southWestView, Side.RIGHT );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertEquals( initialViewCount, workpane.getViews().size() );
		assertEquals( initialViewCount - 1, workpane.getEdges().size() );

		// Check the south west view.
		assertEquals( southWestView.getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );

		// Check the south east view.
		assertEquals( southEastView.getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), toolview.getEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );

		// Merge the south views into the tool view area.
		workpane.pullMerge( toolview, Side.TOP );

		// Check the view and edge counts.
		assertEquals( initialViewCount - 1, workpane.getViews().size() );
		assertEquals( initialViewCount - 2, workpane.getEdges().size() );

		// Check the south west view.
		assertEquals( southWestView.getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
		assertEquals( southWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );

		// Check the south east view.
		assertEquals( southEastView.getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), northView.getEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( southEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testAutoMergeNorth() {
		WorkpaneView view1 = workpane.split( toolview, Side.BOTTOM );
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
	void testAutoMergeMergeNorthWithMultipleViews() {
		WorkpaneView northeast = workpane.split( toolview, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		Tool view2 = new MockTool( asset );

		workpane.addTool( view, toolview );
		workpane.addTool( view1, northeast );
		workpane.addTool( view2, southeast );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.closeTool( view1 );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), Matchers.hasItem( toolview ) );
		assertThat( workpane.getViews(), Matchers.hasItem( southeast ) );
	}

}
