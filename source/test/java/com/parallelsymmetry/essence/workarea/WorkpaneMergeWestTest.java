package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.worktool.Tool;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class WorkpaneMergeWestTest extends WorkpaneTestCase {

	@Test
	public void testCanPushMergeWestSingleTargetSingleSource() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.RIGHT, false ) );
	}

	@Test
	public void testCanPushMergeWestSingleTargetSingleSourceOnEdge() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertTrue( workpane.canPushMerge( toolview, Side.LEFT, false ) );
		assertFalse( workpane.canPushMerge( view, Side.LEFT, false ) );
	}

	@Test
	public void testCanPushMergeWestSingleTargetMultipleSource() throws Exception {
		WorkpaneView west = workpane.split( toolview, Side.LEFT );
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
	public void testCanPushMergeWestComplex() throws Exception {
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
	public void testPushMergeWestSingleTargetSingleSource() throws Exception {
		WorkpaneView east = workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertFalse( east.equals( workpane.getActiveView() ) );

		workpane.setActiveView( east );
		workpane.setDefaultView( east );
		assertThat( workpane.getActiveView(), is( east ) );
		assertThat( workpane.getDefaultView(), is( east ) );

		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, east );

		workpane.pushMerge( east, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( east.getWorkpane(), is( workpane ) );
		assertNull( toolview.getWorkpane() );
		assertThat( workpane.getDefaultView(), is( east ) );
		assertThat( workpane.getActiveView(), is( east ) );
		assertThat( east.getTools().size(), is( 2 ) );

		assertThat( east.northEdge.getPosition(), is( 0d ) );
		assertThat( east.southEdge.getPosition(), is( 1d ) );
		assertThat( east.westEdge.getPosition(), is( 0d ) );
		assertThat( east.eastEdge.getPosition(), is( 1d ) );

		assertEquals( east.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	public void testPushMergeWestSingleTargetMultipleSource() throws Exception {
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
	public void testPushMergeWestMultipleTargetSingleSourceFromNortheast() throws Exception {
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

		assertEquals( northeast.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.southEdge, southeast.northEdge );
		assertEquals( northeast.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.eastEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southeast.northEdge, northeast.southEdge );
		assertEquals( southeast.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southeast.eastEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northeast.southEdge.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.southEdge.eastEdge, workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	public void testPushMergeWestMultipleTargetSingleSourceFromSoutheast() throws Exception {
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

		assertEquals( northeast.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( northeast.southEdge, southeast.northEdge );
		assertEquals( northeast.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.eastEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southeast.northEdge, northeast.southEdge );
		assertEquals( southeast.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southeast.eastEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( northeast.southEdge.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( northeast.southEdge.eastEdge, workpane.getWallEdge( Side.RIGHT ) );
	}

	@Test
	public void testCanPullMergeWestSingleTargetSingleSource() throws Exception {
		WorkpaneView view = workpane.split( toolview, Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.LEFT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.LEFT, false ) );
	}

	@Test
	public void testCanPullMergeWestAcrossEditView() throws Exception {
		WorkpaneView view = workpane.split( Side.LEFT );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.LEFT, false ) );
		assertTrue( workpane.canPullMerge( view, Side.LEFT, false ) );
	}

	@Test
	public void testPullMergeWestMultipleSourceEdgeTarget() throws Exception {
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
	public void testPullMergeWestMultipleSourceMultipleTarget() throws Exception {
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
	public void testAutoMergeWest() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.RIGHT );
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
	public void testAutoMergeMergeWestWithMultipleViews() {
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

		workpane.closeTool( view1 );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), hasItem( toolview ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );
	}

}
