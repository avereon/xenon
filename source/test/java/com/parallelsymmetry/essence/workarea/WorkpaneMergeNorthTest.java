package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.worktool.Tool;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
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

	@Test
	public void testPushMergeNorthSingleTargetSingleSource() throws Exception {
		ToolView south = workpane.split( toolview, Side.BOTTOM );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getDefaultView(), is( toolview ) );
		assertThat( workpane.getActiveView(), is( toolview ) );
		assertFalse( south.isActive() );
		assertThat( south.getWorkPane(), is( workpane ) );
		south.getProperties().put( "name", "view1" );

		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		workpane.addTool( view, toolview );
		workpane.addTool( view1, south );

		workpane.setActiveView( south );
		workpane.setDefaultView( south );
		assertThat( workpane.getActiveView(), is( south ) );
		assertThat( workpane.getDefaultView(), is( south ) );

		workpane.pushMerge( south, Side.TOP );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( south.getWorkPane(), is( workpane ) );
		assertNull( toolview.getWorkPane() );
		assertThat( workpane.getDefaultView(), is( south ) );
		assertThat( workpane.getActiveView(), is( south ) );
		assertThat( south.getTools().size(), is( 2 ) );

		assertThat( south.northEdge.getPosition(), is( 0d ) );
		assertThat( south.southEdge.getPosition(), is( 1d ) );
		assertThat( south.westEdge.getPosition(), is( 0d ) );
		assertThat( south.eastEdge.getPosition(), is( 1d ) );

		assertEquals( south.getBoundsInLocal(), workpane.getBoundsInLocal() );
	}

	@Test
	public void testPushMergeNorthSingleTargetMultipleSource() throws Exception {
		ToolView south = workpane.split( toolview, Side.BOTTOM );
		workpane.split( toolview, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		workpane.setDefaultView( south );
		workpane.pushMerge( south, Side.TOP );
		assertThat( workpane.getViews().size(), is( 1 ) );
		assertThat( workpane.getEdges().size(), is( 0 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( south ) );
		assertThat( workpane.getDefaultView(), is( south ) );
	}

	@Test
	public void testPushMergeNorthMultipleTargetSingleSourceFromSouthwest() throws Exception {
		ToolView southwest = workpane.split( toolview, Side.BOTTOM );
		ToolView southeast = workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );
		assertThat( workpane.getViews(), hasItem( toolview ) );
		assertThat( workpane.getViews(), hasItem( southwest ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southwest );
		workpane.pushMerge( southwest, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( southwest ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );

		assertEquals( southwest.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.eastEdge, southeast.westEdge );

		assertEquals( southeast.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( southeast.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.westEdge, southwest.eastEdge );
		assertEquals( southeast.eastEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.eastEdge.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.eastEdge.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	public void testPushMergeNorthMultipleTargetSingleSourceFromSoutheast() throws Exception {
		ToolView southwest = workpane.split( toolview, Side.BOTTOM );
		ToolView southeast = workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size(), is( 3 ) );
		assertThat( workpane.getEdges().size(), is( 2 ) );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southeast );
		workpane.pushMerge( southeast, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertThat( workpane.getEdges().size(), is( 1 ) );
		assertThat( workpane.getViews(), not( hasItem( toolview ) ) );
		assertThat( workpane.getViews(), hasItem( southwest ) );
		assertThat( workpane.getViews(), hasItem( southeast ) );

		assertEquals( southwest.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southwest.westEdge, workpane.getWallEdge( Side.LEFT ) );
		assertEquals( southwest.eastEdge, southeast.westEdge );

		assertEquals( southeast.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( southeast.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
		assertEquals( southeast.westEdge, southwest.eastEdge );
		assertEquals( southeast.eastEdge, workpane.getWallEdge( Side.RIGHT ) );

		assertEquals( southwest.eastEdge.northEdge, workpane.getWallEdge( Side.TOP ) );
		assertEquals( southwest.eastEdge.southEdge, workpane.getWallEdge( Side.BOTTOM ) );
	}

	@Test
	public void testCanPullMergeNorthSingleTargetSingleSource() throws Exception {
		ToolView view = workpane.split( toolview, Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.TOP, false ) );
		assertTrue( workpane.canPullMerge( view, Side.TOP, false ) );
	}

	@Test
	public void testCanPullMergeNorthAcrossEditView() throws Exception {
		ToolView view = workpane.split( Side.TOP );
		assertThat( workpane.getViews().size(), is( 2 ) );
		assertFalse( workpane.canPullMerge( toolview, Side.TOP, false ) );
		assertTrue( workpane.canPullMerge( view, Side.TOP, false ) );
	}

	@Test
	public void testPullMergeNorthMultipleSourceEdgeTarget() throws Exception {
		ToolView westView = workpane.split( Side.LEFT );
		ToolView eastView = workpane.split( Side.RIGHT );
		ToolView northView = workpane.split( Side.TOP );
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
	public void testPullMergeNorthMultipleSourceMultipleTarget() throws Exception {
		workpane.split( toolview, Side.LEFT );
		workpane.split( toolview, Side.RIGHT );
		ToolView northView = workpane.split( toolview, Side.TOP );
		ToolView southWestView = workpane.split( toolview, Side.BOTTOM );
		ToolView southEastView = workpane.split( southWestView, Side.RIGHT );

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
	public void testAutoMergeNorth() throws Exception {
		ToolView view1 = workpane.split( toolview, Side.BOTTOM );
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
	public void testAutoMergeMergeNorthWithMultipleViews() {
		ToolView northeast = workpane.split( toolview, Side.RIGHT );
		ToolView southeast = workpane.split( northeast, Side.BOTTOM );
		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		Tool view2 = new MockTool( resource );

		workpane.addTool( view, toolview );
		workpane.addTool( view1, northeast );
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
