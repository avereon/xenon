package com.avereon.xenon.test.workpane;

import com.avereon.xenon.workpane.*;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneTest extends WorkpaneTestCase {

	@Test
	void testAddToolAllocateCheck() {
		MockTool tool = new MockTool( asset );
		tool.setWorkpane( workpane );
		workpane.addTool( tool, false );
		assertThat( workpane.getTools().contains( tool ) ).isTrue();
		assertThat( tool.canFindSelfFromWorkpane() ).isTrue();
		assertThat( tool.canFindWorkpaneFromSelf() ).isTrue();
	}

	@Test
	void testAddRemoveToolEvents() {
		MockTool tool = new MockTool( asset );

		// Add the tool but do not set it active.
		workpane.addTool( tool, false );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.ALLOCATE ) ;
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.DISPLAY ) ;
		// Didn't choose to select the tool, so no activate event
		assertThat( tool.getEvents().size()).isEqualTo( 2 );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool()).isEqualTo( tool );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		// Didn't choose to select the tool, so no deactivate event
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.CONCEAL  );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.DEALLOCATE  );
		assertThat( tool.getEvents().size()).isEqualTo( 4 );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView()).isNull();
		assertThat( view.getActiveTool()).isNull();
	}

	@Test
	void testAddSelectRemoveToolEvents() {
		MockTool tool = new MockTool( asset );
		assertThat( workpane.getActiveTool()).isNull();

		// Add the tool and set it active.
		workpane.addTool( tool, true );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.ALLOCATE  );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.DISPLAY  );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.ACTIVATE  );
		assertThat( tool.getEvents().size()).isEqualTo( 3 );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool()).isEqualTo( tool );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.DEACTIVATE  );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.CONCEAL  );
		ToolEventAssert.assertThat( tool.getNextEvent()).hasMethod( MockTool.DEALLOCATE  );
		assertThat( tool.getEvents().size()).isEqualTo( 6 );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView()).isNull();
		assertThat( view.getActiveTool()).isNull();
	}

	@Test
	void testSelectToolEvents() {
		MockTool tool1 = new MockTool( asset );
		MockTool tool2 = new MockTool( asset );
		MockTool tool3 = new MockTool( asset );
		assertThat( tool1.getEvents().size()).isEqualTo( 0 );
		assertThat( tool2.getEvents().size()).isEqualTo( 0 );
		assertThat( tool3.getEvents().size()).isEqualTo( 0 );

		// Add tool one but do not activate it.
		workpane.addTool( tool1, false );
		ToolEventAssert.assertThat( tool1.getNextEvent()).hasMethod( MockTool.ALLOCATE  );
		ToolEventAssert.assertThat( tool1.getNextEvent()).hasMethod( MockTool.DISPLAY);
		assertThat( tool1.getEvents().size()).isEqualTo( 2 );

		// Add tool two but do not activate it.
		workpane.addTool( tool2, false );
		ToolEventAssert.assertThat( tool2.getNextEvent()).hasMethod( MockTool.ALLOCATE);
		assertThat( tool2.getEvents().size()).isEqualTo( 1 );

		// Add tool three and activate it.
		workpane.addTool( tool3, true );
		ToolEventAssert.assertThat( tool1.getNextEvent()).hasMethod( MockTool.CONCEAL);
		assertThat( tool1.getEvents().size()).isEqualTo( 3 );
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.ALLOCATE);
		//ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.ACTIVATE); // Extra
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.DISPLAY);
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.ACTIVATE);
		assertThat( tool3.getEvents().size()).isEqualTo( 3 );

		// Try to set tool three active again.
		workpane.setActiveTool( tool3 );
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.DEACTIVATE);
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.ACTIVATE);
		assertThat( tool3.getEvents().size()).isEqualTo( 5 );

		// Set tool two active.
		workpane.setActiveTool( tool2 );
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.DEACTIVATE);
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.CONCEAL);
		assertThat( tool3.getEvents().size()).isEqualTo( 7 );
		ToolEventAssert.assertThat( tool2.getNextEvent()).hasMethod( MockTool.DISPLAY);
		ToolEventAssert.assertThat( tool2.getNextEvent()).hasMethod( MockTool.ACTIVATE);
		assertThat( tool2.getEvents().size()).isEqualTo( 3 );

		// Remove tool one.
		// This tests the removal of an inactive tool.
		workpane.removeTool( tool1 );
		ToolEventAssert.assertThat( tool1.getNextEvent()).hasMethod( MockTool.DEALLOCATE);
		assertThat( tool1.getEvents().size()).isEqualTo( 4 );

		// Remove tool two.
		// This tests the removal of an active tool when there is more than one tool.
		workpane.removeTool( tool2 );
		ToolEventAssert.assertThat( tool2.getNextEvent()).hasMethod( MockTool.DEACTIVATE);
		ToolEventAssert.assertThat( tool2.getNextEvent()).hasMethod( MockTool.CONCEAL);
		ToolEventAssert.assertThat( tool2.getNextEvent()).hasMethod( MockTool.DEALLOCATE);
		assertThat( tool2.getEvents().size()).isEqualTo( 6 );
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.DISPLAY);
		ToolEventAssert.assertThat( tool3.getNextEvent()).hasMethod( MockTool.ACTIVATE);
		assertThat( tool3.getEvents().size()).isEqualTo( 9 );
	}

	@Test
	void testSetActiveTool() {
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );
		Tool tool3 = new MockTool( asset );

		workpane.addTool( tool1, false );
		workpane.addTool( tool2, false );
		workpane.addTool( tool3, false );

		assertThat( getActiveTool( view )).isEqualTo( tool1 );
		workpane.setActiveTool( tool2 );
		assertThat( getActiveTool( view )).isEqualTo( tool2 );
		workpane.setActiveTool( tool3 );
		assertThat( getActiveTool( view )).isEqualTo( tool3 );
		workpane.setActiveTool( tool1 );
		assertThat( getActiveTool( view )).isEqualTo( tool1 );
	}

	@Test
	void testDockMode() {
		assertThat( workpane.getDockMode()).isEqualTo( Workpane.DEFAULT_DOCK_MODE  );

		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		assertThat( workpane.getDockMode()).isEqualTo( Workpane.DockMode.PORTRAIT );

		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		assertThat( workpane.getDockMode()).isEqualTo( Workpane.DockMode.LANDSCAPE );

		workpane.setDockMode( Workpane.DEFAULT_DOCK_MODE );
		assertThat( workpane.getDockMode()).isEqualTo( Workpane.DEFAULT_DOCK_MODE );
	}

	@Test
	void testSplitNorth() {
		WorkpaneView view1 = workpane.split( Side.TOP );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitSouth() {
		WorkpaneView view1 = workpane.split( Side.BOTTOM );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitWest() {
		WorkpaneView view1 = workpane.split( Side.LEFT );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitEast() {
		WorkpaneView view1 = workpane.split( Side.RIGHT );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
	}

	@Test
	void testSplitNorthCompound() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		WorkpaneView view2 = workpane.split( Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitSouthCompound() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		WorkpaneView view2 = workpane.split( Side.BOTTOM );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitEastCompound() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		WorkpaneView view2 = workpane.split( Side.RIGHT );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1.0 - Workpane.DEFAULT_WALL_SPLIT_RATIO );

		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitWestCompound() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		WorkpaneView view2 = workpane.split( Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_WALL_SPLIT_RATIO );
	}

	@Test
	void testSplitViewNorth() {
		WorkpaneView view1 = workpane.split( view, Side.TOP );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitViewNorthWithSize() {
		double size = 0.31;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.TOP, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( workpane.getTopDockSize()  );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( workpane.getTopDockSize()  );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		double position = workpane.getTopDockSize() + (1 - view.getEdge( Side.TOP ).getPosition()) * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( view, Side.TOP, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( view1.getEdge( Side.BOTTOM ).getPosition() );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( position );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( position );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitViewSouth() {
		WorkpaneView view1 = workpane.split( view, Side.BOTTOM );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitViewSouthWithSize() {
		double size = 0.33;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.BOTTOM, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 1 - workpane.getBottomDockSize()  );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1 - workpane.getBottomDockSize()  );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		double position = 1 - workpane.getBottomDockSize() - view.getEdge( Side.BOTTOM ).getPosition() * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( view, Side.BOTTOM, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( position );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( view1.getEdge( Side.TOP ).getPosition() );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( position );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitViewWest() {
		WorkpaneView view1 = workpane.split( view, Side.LEFT );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( Workpane.DEFAULT_VIEW_SPLIT_RATIO );
	}

	@Test
	void testSplitViewWestWithSize() {
		double size = 0.35;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.LEFT, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( workpane.getLeftDockSize()  );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( workpane.getLeftDockSize()  );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		double position = workpane.getLeftDockSize() + (1 - view.getEdge( Side.LEFT ).getPosition()) * size;

		// The second split will return a new view
		WorkpaneView view2 = workpane.split( view, Side.LEFT, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( view1.getEdge( Side.RIGHT ).getPosition() );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( position );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( position );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitViewEast() {
		WorkpaneView view1 = workpane.split( view, Side.RIGHT );
		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );

		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );
	}

	@Test
	void testSplitViewEastWithSize() {
		double size = 0.37;

		// The first split will return the dock view regardless of the size
		WorkpaneView view1 = workpane.split( view, Side.RIGHT, size );
		assertThat( view1.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view1.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view1.getEdge( Side.LEFT ).getPosition()).isEqualTo( 1 - workpane.getRightDockSize()  );
		assertThat( view1.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1d );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( 1 - workpane.getRightDockSize()  );

		double position = 1 - workpane.getRightDockSize() - view.getEdge( Side.RIGHT ).getPosition() * size;

		WorkpaneView view2 = workpane.split( view, Side.RIGHT, size );
		assertThat( view2.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view2.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view2.getEdge( Side.LEFT ).getPosition()).isEqualTo( position );
		assertThat( view2.getEdge( Side.RIGHT ).getPosition()).isEqualTo( view1.getEdge( Side.LEFT ).getPosition() );

		assertThat( view.getEdge( Side.TOP ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition()).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition()).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition()).isEqualTo( position );
	}

	private Tool getActiveTool( WorkpaneView view ) {
		ToolTabPane pane = (ToolTabPane)view.getChildren().get( 0 );

		int selectedIndex = pane.getSelectionModel().getSelectedIndex();
		ToolTab tab = pane.getTabs().get( selectedIndex );

		return tab.getTool();
	}

}
