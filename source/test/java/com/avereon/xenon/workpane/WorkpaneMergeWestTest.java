package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneMergeWestTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeWestSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.LEFT, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.RIGHT, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeWestSingleTargetSingleSourceOnEdge() {
		WorkpaneView view = workpane.split( this.view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.LEFT, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeWestSingleTargetMultipleSource() {
		WorkpaneView west = workpane.split( view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		workpane.split( west, Side.TOP );
		workpane.split( west, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeWestMultipleTargetSingleSource() {
		workpane.split( view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeWestMultipleTargetMultipleSource() {
		WorkpaneView west = workpane.split( view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		workpane.split( west, Side.TOP );
		workpane.split( west, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeWestComplex() {
		WorkpaneView west = workpane.split( view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		WorkpaneView northwest = workpane.split( west, Side.TOP );
		WorkpaneView southwest = workpane.split( west, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isTrue();

		workpane.split( northwest, Side.RIGHT );
		workpane.split( southwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 8 );
		assertThat( workpane.canPushMerge( view, Side.LEFT, false ) ).isFalse();
	}

	@Test
	void testPushMergeWestSingleTargetSingleSource() {
		WorkpaneView east = workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getDefaultView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isNotEqualTo( east );

		workpane.setActiveView( east );
		workpane.setDefaultView( east );
		assertThat( workpane.getActiveView() ).isEqualTo( east );
		assertThat( workpane.getDefaultView() ).isEqualTo( east );

		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		workpane.addTool( view, this.view );
		workpane.addTool( view1, east );

		workpane.pushMerge( east, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( east.getWorkpane() ).isEqualTo( workpane );
		assertThat( this.view.getWorkpane() ).isNull();
		assertThat( workpane.getDefaultView() ).isEqualTo( east );
		assertThat( workpane.getActiveView() ).isEqualTo( east );
		assertThat( east.getTools().size() ).isEqualTo( 2 );

		assertThat( east.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( east.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( east.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( east.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getBoundsInLocal() ).isEqualTo( east.getBoundsInLocal() );
	}

	@Test
	void testPushMergeWestSingleTargetMultipleSource() {
		WorkpaneView east = workpane.split( view, Side.RIGHT );
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.setDefaultView( east );
		workpane.pushMerge( east, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( east );
		assertThat( workpane.getDefaultView() ).isEqualTo( east );
	}

	@Test
	void testPushMergeWestMultipleTargetSingleSourceFromNortheast() {
		WorkpaneView northeast = workpane.split( view, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northeast );
		workpane.pushMerge( northeast, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( northeast, southeast );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northeast.getEdge( Side.TOP ) );
		assertThat( southeast.getEdge( Side.TOP ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.RIGHT ) );

		assertThat( northeast.getEdge( Side.BOTTOM ) ).isEqualTo( southeast.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southeast.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southeast.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );
	}

	@Test
	void testPushMergeWestMultipleTargetSingleSourceFromSoutheast() {
		WorkpaneView northeast = workpane.split( view, Side.RIGHT );
		WorkpaneView southeast = workpane.split( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southeast );
		workpane.pushMerge( southeast, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( northeast, southeast );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northeast.getEdge( Side.TOP ) );
		assertThat( southeast.getEdge( Side.TOP ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.RIGHT ) );

		assertThat( northeast.getEdge( Side.BOTTOM ) ).isEqualTo( southeast.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( southeast.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southeast.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );
	}

	@Test
	void testCanPullMergeWestSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.LEFT, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.LEFT, false ) ).isTrue();
	}

	@Test
	void testCanPullMergeWestAcrossEditView() {
		WorkpaneView view = workpane.split( Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.LEFT, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.LEFT, false ) ).isTrue();
	}

	@Test
	void testPullMergeWestMultipleSourceEdgeTarget() {
		WorkpaneView northView = workpane.split( Side.TOP );
		WorkpaneView southView = workpane.split( Side.BOTTOM );
		WorkpaneView westView = workpane.split( Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 3 );
		assertThat( northView.getEdge( Side.LEFT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( westView.getEdge( Side.RIGHT ) );
		assertThat( southView.getEdge( Side.LEFT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( westView.getEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.LEFT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( westView.getEdge( Side.RIGHT ) );

		workpane.pullMerge( westView, Side.LEFT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );
		assertThat( northView.getEdge( Side.LEFT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( southView.getEdge( Side.LEFT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.LEFT ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ).getViews( Side.RIGHT ).size() ).isEqualTo( 3 );
	}

	@Test
	void testPullMergeWestMultipleSourceMultipleTarget() {
		workpane.split( view, Side.TOP );
		workpane.split( view, Side.BOTTOM );
		WorkpaneView westView = workpane.split( view, Side.LEFT );
		WorkpaneView northEastView = workpane.split( view, Side.RIGHT );
		WorkpaneView southEastView = workpane.split( northEastView, Side.BOTTOM );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( westView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 1 );

		// Check the north east view.
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );

		// Check the south east view.
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.RIGHT ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );

		// Merge the east views into the tool view area.
		workpane.pullMerge( view, Side.LEFT );

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount - 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 2 );

		// Check the north east view.
		assertThat( westView.getEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( westView.getEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );

		// Check the south east view.
		assertThat( westView.getEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.RIGHT ) );
		assertThat( westView.getEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.TOP ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.TOP ).getEdge( Side.RIGHT ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( southEastView.getEdge( Side.BOTTOM ).getEdge( Side.RIGHT ) );
	}

	@Test
	void testAutoMergeWest() {
		WorkpaneView view1 = workpane.split( view, Side.RIGHT );
		Tool tool = new MockTool( resource );
		Tool tool1 = new MockTool( resource );

		workpane.addTool( tool, view );
		workpane.addTool( tool1, view1 );
		workpane.closeTool( tool1 );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( view.getTools().size() ).isEqualTo( 1 );
	}

	@Test
	void testAutoMergeMergeWestWithMultipleViews() {
		WorkpaneView southwest = workpane.split( view, Side.BOTTOM );
		WorkpaneView southeast = workpane.split( southwest, Side.RIGHT );
		Tool view = new MockTool( resource );
		Tool view1 = new MockTool( resource );
		Tool view2 = new MockTool( resource );
		workpane.addTool( view, this.view );
		workpane.addTool( view1, southwest );
		workpane.addTool( view2, southeast );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.closeTool( view1 );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).contains( this.view, southeast );
	}

}
