package com.xeomar.xenon.workarea;

import com.xeomar.xenon.worktool.Tool;
import javafx.geometry.Side;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class WorkpaneMergeSouthTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeSouthSingleTargetSingleSource() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
		assertFalse( workpane.canPushMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthSingleTargetSingleSourceOnEdge() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.BOTTOM, false ) );
		assertFalse( workpane.canPushMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPushMergeSouthSingleTargetMultipleSource() throws Exception {
		WorkpaneView north = workpane.split( toolview, Side.BOTTOM );
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
	public void testCanPushMergeSouthComplex() throws Exception {
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
	public void testPushMergeSouthSingleTargetSingleSource() throws Exception {
		WorkpaneView north = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertFalse( north.equals( workpane.getActiveView() ) );

		workpane.setActiveView( north );
		workpane.setDefaultView( north );
		assertThat( workpane.getActiveView(), is( north ) );
		assertThat( workpane.getDefaultView(), is( north ) );

		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, north );

		workpane.pushMerge( north, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( north.getWorkpane(), is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( north ) );
		assertThat( workpane.getActiveView(), is( north ) );
		assertThat( north.getTools().size(), is( 2 ) );

		assertThat( north.topEdge.getPosition(), is( 0d ) );
		assertThat( north.bottomEdge.getPosition(), is( 1d ) );
		assertThat( north.leftEdge.getPosition(), is( 0d ) );
		assertThat( north.rightEdge.getPosition(), is( 1d ) );

		assertEquals( north.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	public void testPushMergeSouthSingleTargetMultipleSource() throws Exception {
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
	public void testPushMergeSouthMultipleTargetSingleSourceFromNorthwest() throws Exception {
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

		assertEquals( northwest.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northwest.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.rightEdge, northeast.leftEdge );

		assertEquals( northeast.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northeast.leftEdge, northwest.rightEdge );
		assertEquals( northeast.rightEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.rightEdge.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.rightEdge.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	public void testPushMergeSouthMultipleTargetSingleSourceFromNortheast() throws Exception {
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

		assertEquals( northwest.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northwest.leftEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northwest.rightEdge, northeast.leftEdge );

		assertEquals( northeast.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( northeast.leftEdge, northwest.rightEdge );
		assertEquals( northeast.rightEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northwest.rightEdge.topEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northwest.rightEdge.bottomEdge, workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	public void testCanPullMergeSouthSingleTargetSingleSource() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.BOTTOM, false ) );
		assertTrue( workpane.canPullMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	public void testCanPullMergeSouthAcrossEditView() throws Exception {
		WorkpaneView view = workpane.split( Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.BOTTOM, false ) );
		assertTrue( workpane.canPullMerge( view, Side.BOTTOM, false ) );
	}

	@Test
	public void testPullMergeSouthMultipleSourceEdgeTarget() throws Exception {
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
	public void testPullMergeSouthMultipleSourceMultipleTarget() throws Exception {
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
	public void testAutoMergeSouth() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
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
	public void testAutoMergeMergeSouthWithMultipleViews() {
		WorkpaneView northeast = workpane.split( toolview, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		Tool view2 = new MockTool( resource );
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
