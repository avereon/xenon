package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneMergeEastTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeEastSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.RIGHT, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeEastSingleTargetSingleSourceOnEdge() {
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeEastSingleTargetMultipleSource() {
		WorkpaneView east = workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		workpane.split( east, Side.TOP );
		workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeEastMultipleTargetSingleSource() {
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeEastMultipleTargetMultipleSource() {
		WorkpaneView east = workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		workpane.split( east, Side.TOP );
		workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeEastComplex() {
		WorkpaneView east = workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		WorkpaneView northeast = workpane.split( east, Side.TOP );
		WorkpaneView southeast = workpane.split( east, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isTrue();

		workpane.split( northeast, Side.RIGHT );
		workpane.split( southeast, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 8 );
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isFalse();
	}

	@Test
	void testPushMergeEastSingleTargetSingleSource() {
		WorkpaneView west = workpane.split( view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getDefaultView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isNotEqualTo( west );

		workpane.setActiveView( west );
		workpane.setDefaultView( west );
		assertThat( workpane.getActiveView() ).isEqualTo( west );
		assertThat( workpane.getDefaultView() ).isEqualTo( west );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, this.view );
		workpane.addTool( view1, west );

		workpane.pushMerge( west, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( west.getWorkpane() ).isEqualTo( workpane );
		assertThat( this.view.getWorkpane() ).isNull();
		assertThat( workpane.getDefaultView() ).isEqualTo( west );
		assertThat( workpane.getActiveView() ).isEqualTo( west );
		assertThat( west.getToolTabPane().size() ).isEqualTo( 2 );

		assertThat( west.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( west.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( west.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( west.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getBoundsInLocal() ).isEqualTo( west.getBoundsInLocal() );
	}

	@Test
	void testPushMergeEastSingleTargetMultipleSource() {
		WorkpaneView west = workpane.split( view, Side.LEFT );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.setDefaultView( west );
		workpane.pushMerge( west, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( west );
		assertThat( workpane.getDefaultView() ).isEqualTo( west );
	}

	@Test
	void testPushMergeEastMultipleTargetSingleSourceFromNorthwest() {
		WorkpaneView northwest = workpane.split( view, Side.LEFT );
		WorkpaneView southwest = workpane.split( northwest, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northwest );
		workpane.pushMerge( northwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( northwest, southwest );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.TOP ) );
		assertThat( southwest.getEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northwest.getEdge( Side.RIGHT ) );

		assertThat( northwest.getEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southwest.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southwest.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );
	}

	@Test
	void testPushMergeEastMultipleTargetSingleSourceFromSouthwest() {
		WorkpaneView northwest = workpane.split( view, Side.LEFT );
		WorkpaneView southwest = workpane.split( northwest, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southwest );
		workpane.pushMerge( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( northwest, southwest );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.TOP ) );
		assertThat( southwest.getEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northwest.getEdge( Side.RIGHT ) );

		assertThat( northwest.getEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southwest.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southwest.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );
	}

	@Test
	void testCanPullMergeEastSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.RIGHT, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.RIGHT, false ) ).isTrue();
	}

	@Test
	void testCanPullMergeEastAcrossEditView() {
		WorkpaneView view = workpane.split( Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.RIGHT, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.RIGHT, false ) ).isTrue();
	}

	@Test
	void testPullMergeEastMultipleSourceEdgeTarget() {
		WorkpaneView northView = workpane.split( Side.TOP );
		WorkpaneView southView = workpane.split( Side.BOTTOM );
		WorkpaneView eastView = workpane.split( Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 3 );
		assertThat( northView.getEdge( Side.RIGHT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( eastView.getEdge( Side.LEFT ) );
		assertThat( southView.getEdge( Side.RIGHT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( eastView.getEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.RIGHT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( eastView.getEdge( Side.LEFT ) );

		workpane.pullMerge( eastView, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );
		assertThat( northView.getEdge( Side.RIGHT ) ).withFailMessage( "Common edge not linked." ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( southView.getEdge( Side.RIGHT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.RIGHT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ).getViews( Side.LEFT ).size() ).isEqualTo( 3 );
	}

	@Test
	void testPullMergeEastMultipleSourceMultipleTarget() {
		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		WorkpaneView eastView = workpane.split( view, Side.RIGHT );
		WorkpaneView northWestView = workpane.split( view, Side.LEFT );
		WorkpaneView southWestView = workpane.split( northWestView, Side.BOTTOM );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( eastView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 1 );

		// Check the north west view.
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );

		// Check the south west view.
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );

		// Merge the west views into the tool view area.
		workpane.pullMerge( view, Side.RIGHT );

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount - 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 2 );

		// Check the north west view.
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.LEFT ) );
		assertThat( eastView.getEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( eastView.getEdge( Side.LEFT ) ).isEqualTo( northWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );

		// Check the south west view.
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.LEFT ) );
		assertThat( eastView.getEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( eastView.getEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southWestView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );
	}

	@Test
	void testAutoMergeEast() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		Tool tool = new MockTool( asset );
		Tool tool1 = new MockTool( asset );

		workpane.addTool( tool, view );
		workpane.addTool( tool1, view1 );
		workpane.closeTool( tool1 );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( view.getToolTabPane().size() ).isEqualTo( 1 );
	}

	@Test
	void testAutoMergeMergeEastWithMultipleViews() {
		WorkpaneView southwest = workpane.split( view, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		Tool view2 = new MockTool( asset );
		workpane.addTool( view, this.view );
		workpane.addTool( view1, southwest );
		workpane.addTool( view2, southeast );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.closeTool( view2 );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).contains( this.view, southwest );
	}

}
