package com.avereon.xenon.test.workpane;

import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.WorkpaneView;
import javafx.geometry.Side;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

class WorkpaneMergeSouthTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeSouthSingleTargetSingleSource()  {
		WorkpaneView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
		assertFalse( workpane.canPushMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	void testCanPushMergeSouthSingleTargetSingleSourceOnEdge()  {
		WorkpaneView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
		assertFalse( workpane.canPushMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	void testCanPushMergeSouthSingleTargetMultipleSource()  {
		WorkpaneView north = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
	}

	@Test
	void testCanPushMergeSouthMultipleTargetSingleSource()  {
		workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
	}

	@Test
	void testCanPushMergeSouthMultipleTargetMultipleSource()  {
		WorkpaneView north = workpane.split( toolview, Side.BOTTOM );
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
	void testCanPushMergeSouthComplex()  {
		WorkpaneView north = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );

		WorkpaneView northwest = workpane.split( north, Side.LEFT );
		WorkpaneView northeast = workpane.split( north, Side.RIGHT );
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

	@Test
	void testPushMergeSouthSingleTargetSingleSource()  {
		WorkpaneView north = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), Matchers.is( toolview ) );
		assertThat( workpane.getActiveView(), Matchers.is( toolview ) );
		assertNotEquals( north, workpane.getActiveView() );

		workpane.setActiveView( north );
		workpane.setDefaultView( north );
		assertThat( workpane.getActiveView(), is( north ) );
		assertThat( workpane.getDefaultView(), is( north ) );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, north );

		workpane.pushMerge( north, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( north.getWorkpane(), Matchers.is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( north ) );
		assertThat( workpane.getActiveView(), is( north ) );
		assertThat( north.getTools().size(), is( 2 ) );

		assertThat( north.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( north.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( north.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( north.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		assertEquals( north.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	void testPushMergeSouthSingleTargetMultipleSource()  {
		WorkpaneView north = workpane.split( toolview, Side.TOP );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.setDefaultView( north );
		workpane.pushMerge( north, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( workpane.getViews(), not( Matchers.hasItem( toolview ) ) );
		assertThat( workpane.getViews(), Matchers.hasItem( north ) );
		assertThat( workpane.getDefaultView(), is( north ) );
	}

	@Test
	void testPushMergeSouthMultipleTargetSingleSourceFromNorthwest()  {
		WorkpaneView northwest = workpane.split( toolview, Side.TOP );
		WorkpaneView northeast = workpane.split( northwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northwest );
		workpane.pushMerge( northwest, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( Matchers.hasItem( toolview ) ) );
		assertThat( workpane.getViews(), Matchers.hasItem( northwest ) );
		assertThat( workpane.getViews(), Matchers.hasItem( northeast ) );

		assertEquals( northwest.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.getEdge( Side.RIGHT ), northeast.getEdge( Side.LEFT ) );

		assertEquals( northeast.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northeast.getEdge( Side.LEFT ), northwest.getEdge( Side.RIGHT ) );
		assertEquals( northeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testPushMergeSouthMultipleTargetSingleSourceFromNortheast()  {
		WorkpaneView northwest = workpane.split( toolview, Side.TOP );
		WorkpaneView northeast = workpane.split( northwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northeast );
		workpane.pushMerge( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( Matchers.hasItem( toolview ) ) );
		assertThat( workpane.getViews(), Matchers.hasItem( northwest ) );
		assertThat( workpane.getViews(), Matchers.hasItem( northeast ) );

		assertEquals( northwest.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northwest.getEdge( Side.LEFT ), workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.getEdge( Side.RIGHT ), northeast.getEdge( Side.LEFT ) );

		assertEquals( northeast.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northeast.getEdge( Side.LEFT ), northwest.getEdge( Side.RIGHT ) );
		assertEquals( northeast.getEdge( Side.RIGHT ), workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	void testCanPullMergeSouthSingleTargetSingleSource()  {
		WorkpaneView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.BOTTOM, false ) );
		assertTrue( workpane.canPullMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	void testCanPullMergeSouthAcrossEditView()  {
		WorkpaneView view = workpane.split( Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.BOTTOM, false ) );
		assertTrue( workpane.canPullMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	void testPullMergeSouthMultipleSourceEdgeTarget()  {
		WorkpaneView westView = workpane.split( Side.LEFT );
		WorkpaneView eastView = workpane.split( Side.RIGHT );
		WorkpaneView southView = workpane.split( Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 4 ) );
		assertThat( workpane.getEdges().size(), is( 3 ) );
		assertThat( "Common edge not linked.", westView.getEdge( Side.BOTTOM ), is( southView.getEdge( Side.TOP ) ) );
		assertThat( "Common edge not linked.", eastView.getEdge( Side.BOTTOM ), is( southView.getEdge( Side.TOP ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.BOTTOM ), is( southView.getEdge( Side.TOP ) ) );

		workpane.pullMerge( southView, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );
		assertThat( "Common edge not linked.", westView.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( "Common edge not linked.", eastView.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( "Common edge not linked.", toolview.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ).getViews( Side.TOP ).size(), is( 3 ) );
	}

	@Test
	void testPullMergeSouthMultipleSourceMultipleTarget()  {
		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		WorkpaneView northWestView = workpane.split( toolview, Side.TOP );
		WorkpaneView northEastView = workpane.split( northWestView, Side.RIGHT );
		WorkpaneView southView = workpane.split( toolview, Side.BOTTOM );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertEquals( initialViewCount, workpane.getViews().size() );
		assertEquals( initialViewCount - 1, workpane.getEdges().size() );

		// Check the north west view.
		assertEquals( northWestView.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );

		// Check the north east view.
		assertEquals( northEastView.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), toolview.getEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );

		// Merge the north views into the tool view area.
		workpane.pullMerge( toolview, Side.BOTTOM );

		// Check the view and edge counts.
		assertEquals( initialViewCount - 1, workpane.getViews().size() );
		assertEquals( initialViewCount - 2, workpane.getEdges().size() );

		// Check the north west view.
		assertEquals( northWestView.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
		assertEquals( northWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );

		// Check the north east view.
		assertEquals( northEastView.getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ), southView.getEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ), workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ), workpane.getWallEdge( Side.TOP ) );
	}

	@Test
	void testAutoMergeSouth()  {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
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
	void testAutoMergeMergeSouthWithMultipleViews() {
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

		workpane.closeTool( view2 );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), Matchers.hasItem( toolview ) );
		assertThat( workpane.getViews(), Matchers.hasItem( northeast ) );
	}

}
