package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneMergeNorthTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeNorthSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.TOP, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeNorthSingleTargetSingleSourceOnEdge() {
		WorkpaneView view = workpane.split( this.view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.TOP, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeNorthSingleTargetMultipleSource() {
		WorkpaneView north = workpane.split( view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeNorthMultipleTargetSingleSource() {
		workpane.split( view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeNorthMultipleTargetMultipleSource() {
		WorkpaneView north = workpane.split( view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeNorthComplex() {
		WorkpaneView north = workpane.split( view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		WorkpaneView northwest = workpane.split( north, Side.LEFT );
		WorkpaneView northeast = workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isTrue();

		workpane.split( northwest, Side.BOTTOM );
		workpane.split( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 8 );
		assertThat( workpane.canPushMerge( view, Side.TOP, false ) ).isFalse();
	}

	@Test
	void testPushMergeNorthSingleTargetSingleSource() {
		WorkpaneView south = workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getDefaultView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isEqualTo( view );
		assertThat( south.isActive() ).isFalse();
		assertThat( south.getWorkpane() ).isEqualTo( workpane );
		south.getProperties().put( "name", "view1" );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, this.view );
		workpane.addTool( view1, south );

		workpane.setActiveView( south );
		workpane.setDefaultView( south );
		assertThat( workpane.getActiveView() ).isEqualTo( south );
		assertThat( workpane.getDefaultView() ).isEqualTo( south );

		workpane.pushMerge( south, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( south.getWorkpane() ).isEqualTo( workpane );
		assertThat( this.view.getWorkpane() ).isNull();
		assertThat( workpane.getDefaultView() ).isEqualTo( south );
		assertThat( workpane.getActiveView() ).isEqualTo( south );
		assertThat( south.getTools().size() ).isEqualTo( 2 );

		assertThat( south.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( south.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( south.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( south.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getBoundsInLocal() ).isEqualTo( south.getBoundsInLocal() );
	}

	@Test
	void testPushMergeNorthSingleTargetMultipleSource() {
		WorkpaneView south = workpane.split( view, Side.BOTTOM );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.setDefaultView( south );
		workpane.pushMerge( south, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( south );
		assertThat( workpane.getDefaultView() ).isEqualTo( south );
	}

	@Test
	void testPushMergeNorthMultipleTargetSingleSourceFromSouthwest() {
		WorkpaneView southwest = workpane.split( view, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );
		assertThat( workpane.getViews() ).contains( view, southwest, southeast );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southwest );
		workpane.pushMerge( southwest, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( southwest, southeast );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southwest.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southwest.getEdge( Side.LEFT ) );
		assertThat( southeast.getEdge( Side.LEFT ) ).isEqualTo( southwest.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southeast.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southeast.getEdge( Side.BOTTOM ) );
		assertThat( southwest.getEdge( Side.RIGHT ) ).isEqualTo( southeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southeast.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
	}

	@Test
	void testPushMergeNorthMultipleTargetSingleSourceFromSoutheast() {
		WorkpaneView southwest = workpane.split( view, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southeast );
		workpane.pushMerge( southeast, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( southwest, southeast );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southwest.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southwest.getEdge( Side.LEFT ) );
		assertThat( southeast.getEdge( Side.LEFT ) ).isEqualTo( southwest.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southeast.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southeast.getEdge( Side.BOTTOM ) );
		assertThat( southwest.getEdge( Side.RIGHT ) ).isEqualTo( southeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southeast.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
	}

	@Test
	void testCanPullMergeNorthSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.TOP, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.TOP, false ) ).isTrue();
	}

	@Test
	void testCanPullMergeNorthAcrossEditView() {
		WorkpaneView view = workpane.split( Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.TOP, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.TOP, false ) ).isTrue();
	}

	@Test
	void testPullMergeNorthMultipleSourceEdgeTarget() {
		WorkpaneView westView = workpane.split( Side.LEFT );
		WorkpaneView eastView = workpane.split( Side.RIGHT );
		WorkpaneView northView = workpane.split( Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 3 );
		assertThat( westView.getEdge( Side.TOP ) ).withFailMessage( "Common edge not linked." ).isEqualTo( northView.getEdge( Side.BOTTOM ) );
		assertThat( eastView.getEdge( Side.TOP ) ).withFailMessage( "Common edge not linked." ).isEqualTo( northView.getEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.TOP ) ).withFailMessage( "Common edge not linked." ).isEqualTo( northView.getEdge( Side.BOTTOM ) );

		workpane.pullMerge( northView, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );
		assertThat( westView.getEdge( Side.TOP ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( eastView.getEdge( Side.TOP ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.TOP ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.TOP ).getViews( Side.BOTTOM ).size() ).isEqualTo( 3 );
	}

	@Test
	void testPullMergeNorthMultipleSourceMultipleTarget() {
		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		WorkpaneView northView = workpane.split( view, Side.TOP );
		WorkpaneView southWestView = workpane.split( view, Side.BOTTOM );
		WorkpaneView southEastView = workpane.split( southWestView, Side.RIGHT );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 1 );

		// Check the south west view.
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );

		// Check the south east view.
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );

		// Merge the south views into the tool view area.
		workpane.pullMerge( view, Side.TOP );

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount - 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 2 );

		// Check the south west view.
		assertThat( northView.getEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( northView.getEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );

		// Check the south east view.
		assertThat( northView.getEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.BOTTOM ) );
		assertThat( northView.getEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( southEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
	}

	@Test
	void testAutoMergeNorth() {
		WorkpaneView view1 = workpane.split( view, Side.BOTTOM );
		Tool tool = new MockTool( asset );
		Tool tool1 = new MockTool( asset );

		workpane.addTool( tool, view );
		workpane.addTool( tool1, view1 );
		workpane.closeTool( tool1 );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( view.getTools().size() ).isEqualTo( 1 );
	}

	@Test
	void testAutoMergeMergeNorthWithMultipleViews() {
		WorkpaneView northeast = workpane.split( view, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		Tool view2 = new MockTool( asset );

		workpane.addTool( view, this.view );
		workpane.addTool( view1, northeast );
		workpane.addTool( view2, southeast );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.closeTool( view1 );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).contains( this.view, southeast );
	}

}
