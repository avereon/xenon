package com.avereon.xenon.test.workpane;

import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.WorkpaneView;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneMergeSouthTest extends WorkpaneTestCase {

	@Test
	void testCanPushMergeSouthSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.BOTTOM, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeSouthSingleTargetSingleSourceOnEdge() {
		WorkpaneView view = workpane.split( this.view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( this.view, Side.BOTTOM, false ) ).isTrue();
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isFalse();
	}

	@Test
	void testCanPushMergeSouthSingleTargetMultipleSource() {
		WorkpaneView north = workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeSouthMultipleTargetSingleSource() {
		workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeSouthMultipleTargetMultipleSource() {
		WorkpaneView north = workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		workpane.split( north, Side.LEFT );
		workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();
	}

	@Test
	void testCanPushMergeSouthComplex() {
		WorkpaneView north = workpane.split( view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		WorkpaneView northwest = workpane.split( north, Side.LEFT );
		WorkpaneView northeast = workpane.split( north, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 6 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isTrue();

		workpane.split( northwest, Side.TOP );
		workpane.split( northeast, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 8 );
		assertThat( workpane.canPushMerge( view, Side.BOTTOM, false ) ).isFalse();
	}

	@Test
	void testPushMergeSouthSingleTargetSingleSource() {
		WorkpaneView north = workpane.split( view, Side.TOP );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getDefaultView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isEqualTo( view );
		assertThat( workpane.getActiveView() ).isNotEqualTo( north );

		workpane.setActiveView( north );
		workpane.setDefaultView( north );
		assertThat( workpane.getActiveView() ).isEqualTo( north );
		assertThat( workpane.getDefaultView() ).isEqualTo( north );

		Tool view = new MockTool( asset );
		Tool view1 = new MockTool( asset );
		workpane.addTool( view, this.view );
		workpane.addTool( view1, north );

		workpane.pushMerge( north, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( north.getWorkpane() ).isEqualTo( workpane );
		assertThat( this.view.getWorkpane() ).isNull();
		assertThat( workpane.getDefaultView() ).isEqualTo( north );
		assertThat( workpane.getActiveView() ).isEqualTo( north );
		assertThat( north.getTools().size() ).isEqualTo( 2 );

		assertThat( north.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( north.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( north.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( north.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		assertThat( workpane.getBoundsInLocal() ).isEqualTo( north.getBoundsInLocal() );
	}

	@Test
	void testPushMergeSouthSingleTargetMultipleSource() {
		WorkpaneView north = workpane.split( view, Side.TOP );
		workpane.split( view, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		workpane.setDefaultView( north );
		workpane.pushMerge( north, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 0 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( north );
		assertThat( workpane.getDefaultView() ).isEqualTo( north );
	}

	@Test
	void testPushMergeSouthMultipleTargetSingleSourceFromNorthwest() {
		WorkpaneView northwest = workpane.split( view, Side.TOP );
		WorkpaneView northeast = workpane.split( northwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northwest );
		workpane.pushMerge( northwest, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( northwest, northeast );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.LEFT ) );
		assertThat( northeast.getEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northeast.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ) );
		assertThat( northwest.getEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
	}

	@Test
	void testPushMergeSouthMultipleTargetSingleSourceFromNortheast() {
		WorkpaneView northwest = workpane.split( view, Side.TOP );
		WorkpaneView northeast = workpane.split( northwest, Side.RIGHT );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( northeast );
		workpane.pushMerge( northeast, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).doesNotContain( view );
		assertThat( workpane.getViews() ).contains( northwest, northeast );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northwest.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.LEFT ) );
		assertThat( northeast.getEdge( Side.LEFT ) ).isEqualTo( northwest.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northeast.getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northeast.getEdge( Side.BOTTOM ) );
		assertThat( northwest.getEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.LEFT ) );
		assertThat( workpane.getWallEdge( Side.RIGHT ) ).isEqualTo( northeast.getEdge( Side.RIGHT ) );

		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northwest.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northwest.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
	}

	@Test
	void testCanPullMergeSouthSingleTargetSingleSource() {
		WorkpaneView view = workpane.split( this.view, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.BOTTOM, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.BOTTOM, false ) ).isTrue();
	}

	@Test
	void testCanPullMergeSouthAcrossEditView() {
		WorkpaneView view = workpane.split( Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.canPullMerge( this.view, Side.BOTTOM, false ) ).isFalse();
		assertThat( workpane.canPullMerge( view, Side.BOTTOM, false ) ).isTrue();
	}

	@Test
	void testPullMergeSouthMultipleSourceEdgeTarget() {
		WorkpaneView westView = workpane.split( Side.LEFT );
		WorkpaneView eastView = workpane.split( Side.RIGHT );
		WorkpaneView southView = workpane.split( Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 4 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 3 );
		assertThat( westView.getEdge( Side.BOTTOM ) ).withFailMessage( "Common edge not linked." ).isEqualTo( southView.getEdge( Side.TOP ) );
		assertThat( eastView.getEdge( Side.BOTTOM ) ).withFailMessage( "Common edge not linked." ).isEqualTo( southView.getEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).withFailMessage( "Common edge not linked." ).isEqualTo( southView.getEdge( Side.TOP ) );

		workpane.pullMerge( southView, Side.BOTTOM );
		assertThat( workpane.getViews().size() ).isEqualTo( 3 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 2 );
		assertThat( westView.getEdge( Side.BOTTOM ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( eastView.getEdge( Side.BOTTOM ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).withFailMessage( "Common edge not linked." ).isEqualTo( workpane.getWallEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ).getViews( Side.TOP ).size() ).isEqualTo( 3 );
	}

	@Test
	void testPullMergeSouthMultipleSourceMultipleTarget() {
		workpane.split( view, Side.LEFT );
		workpane.split( view, Side.RIGHT );
		WorkpaneView northWestView = workpane.split( view, Side.TOP );
		WorkpaneView northEastView = workpane.split( northWestView, Side.RIGHT );
		WorkpaneView southView = workpane.split( view, Side.BOTTOM );

		// Change the default view so the merge can happen.
		workpane.setDefaultView( southView );

		int initialViewCount = 6;

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 1 );

		// Check the north west view.
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );

		// Check the north east view.
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );

		// Merge the north views into the tool view area.
		workpane.pullMerge( view, Side.BOTTOM );

		// Check the view and edge counts.
		assertThat( workpane.getViews().size() ).isEqualTo( initialViewCount - 1 );
		assertThat( workpane.getEdges().size() ).isEqualTo( initialViewCount - 2 );

		// Check the north west view.
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.TOP ) );
		assertThat( southView.getEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northWestView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( southView.getEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northWestView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );

		// Check the north east view.
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.TOP ) );
		assertThat( southView.getEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.BOTTOM ) );
		assertThat( southView.getEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.LEFT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.LEFT ).getEdge( Side.TOP ) );
		assertThat( workpane.getWallEdge( Side.BOTTOM ) ).isEqualTo( northEastView.getEdge( Side.RIGHT ).getEdge( Side.BOTTOM ) );
		assertThat( workpane.getWallEdge( Side.TOP ) ).isEqualTo( northEastView.getEdge( Side.RIGHT ).getEdge( Side.TOP ) );
	}

	@Test
	void testAutoMergeSouth() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
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
	void testAutoMergeMergeSouthWithMultipleViews() {
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

		workpane.closeTool( view2 );
		assertThat( workpane.getViews().size() ).isEqualTo( 2 );
		assertThat( workpane.getEdges().size() ).isEqualTo( 1 );
		assertThat( workpane.getViews() ).contains( this.view, northeast );
	}

}
