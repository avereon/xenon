package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.worktool.Tool;
import com.parallelsymmetry.essence.worktool.ToolEvent;
import com.parallelsymmetry.essence.worktool.ToolListener;
import javafx.geometry.Side;
import javafx.scene.Scene;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkPaneEventTest extends WorkpaneTestCase {

	private Workpane workpane;

	@Before
	public void setup() throws Exception {
		super.setup();
		workpane = new Workpane();

		// Workpane size must be set for move methods to work correctly.
		Scene scene = new Scene( workpane, 1000000, 1000000 );
		assertThat( workpane.getWidth(), is( 1000000d ) );
		assertThat( workpane.getHeight(), is( 1000000d ) );

		// Layout the workpane
		workpane.layout();
	}

	@Test
	public void testMoveEdge() {
		ToolView west = workpane.split( Side.LEFT );
		Workpane.Edge edge = west.getEdge( Side.RIGHT );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		workpane.addWorkPaneListener( workAreaEventCounter );

		workpane.moveEdge( edge, 25 );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.CHANGED, workpane, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 1 ) );
	}

	@Test
	public void testMoveEdgeNowhere() {
		Workpane area = new Workpane();
		ToolView west = area.split( Side.LEFT );
		Workpane.Edge edge = west.getEdge( Side.RIGHT );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		area.moveEdge( edge, 0 );
		assertThat( workAreaEventCounter.events.size(), is( 0 ) );
	}

	//	public void testSetActiveView() {
	//		Workpane area = new Workpane();
	//		ToolView south = area.getDefaultView();
	//		ToolView north = area.split( south, Workpane.NORTH );
	//		area.setActiveView( south );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//		area.setActiveView( north );
	//		assertEquals( 3, workAreaEventCounter.events.size() );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, south, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, north, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//	}
	//
	//	public void testSetActiveViewSame() {
	//		Workpane area = new Workpane();
	//		ToolView south = area.getDefaultView();
	//		area.setActiveView( south );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		area.setActiveView( south );
	//		assertEquals( 0, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSetActiveViewWithViewFromOtherArea() {
	//		Workpane area0 = new Workpane();
	//		Workpane area1 = new Workpane();
	//		ToolView view0 = area0.getDefaultView();
	//		ToolView view1 = area1.getDefaultView();
	//		area0.setActiveView( view0 );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area0.addWorkPaneListener( workAreaEventCounter );
	//
	//		area0.setActiveView( view1 );
	//		assertEquals( 0, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSetDefaultView() {
	//		Workpane area = new Workpane();
	//		ToolView south = area.getDefaultView();
	//		ToolView north = area.split( south, Workpane.NORTH );
	//		area.setDefaultView( south );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//		area.setDefaultView( north );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//		assertEquals( 1, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSetDefaultViewSame() {
	//		Workpane area = new Workpane();
	//		ToolView south = area.getDefaultView();
	//		area.setDefaultView( south );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//		area.setDefaultView( south );
	//		assertEquals( 0, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSetMaximizedView() {
	//		Workpane area = new Workpane();
	//		area.setMaximizedView( null );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		area.setMaximizedView( area.getDefaultView() );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//		assertEquals( 1, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSplit() {
	//		Workpane area = new Workpane();
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		ToolView north = area.split( Workpane.NORTH );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_ADDED, area, north, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_SPLIT, area, null, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//		assertEquals( 3, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSplitWithView() {
	//		Workpane area = new Workpane();
	//		ToolView view = area.getDefaultView();
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		ToolView north = area.split( view, Workpane.NORTH );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_WILL_SPLIT, area, view, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_ADDED, area, north, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.VIEW_SPLIT, area, view, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 3 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//		assertEquals( 4, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testMerge() {
	//		Workpane area = new Workpane();
	//		ToolView view = area.getDefaultView();
	//		ToolView north = area.split( view, Workpane.NORTH );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		area.pushMerge( view, Workpane.NORTH );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_WILL_MERGE, area, view, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_REMOVED, area, north, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.VIEW_MERGED, area, view, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 3 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//		assertEquals( 4, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testAddTool() {
	//		Workpane area = new Workpane();
	//		ToolView view = area.getDefaultView();
	//		Tool tool = new MockTool();
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		ToolEventCounter toolEventCounter = new ToolEventCounter();
	//		tool.addToolListener( toolEventCounter );
	//
	//		area.addTool( tool, view );
	//		assertEquals( 3, workAreaEventCounter.events.size() );
	//		assertEquals( 0, toolEventCounter.events.size() );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_ADDED, area, view, tool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, view, tool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//	}
	//
	//	public void testRemoveTool() {
	//		Workpane area = new Workpane();
	//		ToolView view = area.getDefaultView();
	//		Tool tool = new MockTool();
	//		area.addTool( tool, view );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		ToolEventCounter toolEventCounter = new ToolEventCounter();
	//		tool.addToolListener( toolEventCounter );
	//
	//		area.removeTool( tool );
	//		assertEquals( 3, workAreaEventCounter.events.size() );
	//		assertEquals( 0, toolEventCounter.events.size() );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, view, tool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.TOOL_REMOVED, area, view, tool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//	}
	//
	//	public void testCloseTool() {
	//		Workpane area = new Workpane();
	//		ToolView view = area.getDefaultView();
	//		Tool tool = new MockTool();
	//		area.addTool( tool, view );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		ToolEventCounter toolEventCounter = new ToolEventCounter();
	//		tool.addToolListener( toolEventCounter );
	//
	//		area.closeTool( tool );
	//		assertEquals( 3, workAreaEventCounter.events.size() );
	//		assertEquals( 2, toolEventCounter.events.size() );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, view, tool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.TOOL_REMOVED, area, view, tool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//		assertToolEvent( toolEventCounter.events.get( 0 ), ToolEvent.Type.TOOL_CLOSING, tool );
	//		assertToolEvent( toolEventCounter.events.get( 1 ), ToolEvent.Type.TOOL_CLOSED, tool );
	//	}
	//
	//	public void testSetActiveTool() {
	//		Workpane area = new Workpane();
	//		ToolView southView = area.getDefaultView();
	//		ToolView northView = area.split( southView, Workpane.NORTH );
	//		Tool northTool = new MockTool();
	//		Tool southTool = new MockTool();
	//		area.addTool( northTool, northView );
	//		area.addTool( southTool, southView );
	//		area.setActiveTool( southTool );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		area.setActiveTool( northTool );
	//		assertEquals( 5, workAreaEventCounter.events.size() );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, southView, southTool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, southView, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, northView, null );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 3 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, northView, northTool );
	//		assertWorkAreaEvent( workAreaEventCounter.events.get( 4 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	//	}
	//
	//	public void testSetActiveToolWithToolNotAdded() {
	//		Workpane area = new Workpane();
	//		ToolView southView = area.getDefaultView();
	//		Tool northTool = new MockTool();
	//		Tool southTool = new MockTool();
	//		area.addTool( southTool, southView );
	//		area.setActiveTool( southTool );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area.addWorkPaneListener( workAreaEventCounter );
	//
	//		area.setActiveTool( northTool );
	//		assertEquals( 0, workAreaEventCounter.events.size() );
	//	}
	//
	//	public void testSetActiveToolWithToolFromOtherArea() {
	//		Workpane area0 = new Workpane();
	//		Workpane area1 = new Workpane();
	//		ToolView view0 = area0.getDefaultView();
	//		ToolView view1 = area1.getDefaultView();
	//		Tool tool0 = new MockTool();
	//		Tool tool1 = new MockTool();
	//		area0.addTool( tool0, view0 );
	//		area1.addTool( tool1, view1 );
	//		area0.setActiveTool( tool0 );
	//
	//		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
	//		area0.addWorkPaneListener( workAreaEventCounter );
	//
	//		area0.setActiveTool( tool1 );
	//		assertEquals( 0, workAreaEventCounter.events.size() );
	//	}

	private void assertWorkAreaEvent( WorkpaneEvent event, WorkpaneEvent.Type type, Workpane area, ToolView view, Tool tool ) {
		assertThat( "Event type", event.getType(), is( type ) );
		assertThat( "Work area", event.getWorkPane(), is( area ) );
		assertThat( "Tool view", event.getToolView(), is( view ) );
		assertThat( "Tool", event.getTool(), is( tool ) );
	}

	private void assertToolEvent( ToolEvent event, ToolEvent.Type type, Tool tool ) {
		assertThat( "Event type", event.getType(), is( type ) );
		assertThat( "Tool", event.getTool(), is( tool ) );
	}

	private static class ToolEventCounter implements ToolListener {

		public List<ToolEvent> events = new CopyOnWriteArrayList<ToolEvent>();

		@Override
		public void toolClosing( ToolEvent event ) {
			events.add( event );
		}

		@Override
		public void toolClosed( ToolEvent event ) {
			events.add( event );
		}

	}

	private static class WorkPaneEventCounter implements WorkpaneListener {

		public List<WorkpaneEvent> events = new CopyOnWriteArrayList<WorkpaneEvent>();

		@Override
		public void paneChanged( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void viewAdded( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void viewRemoved( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void viewActivated( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void viewDeactivated( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void viewWillSplit( WorkpaneEvent event ) throws WorkpaneVetoException {
			events.add( event );
		}

		@Override
		public void viewSplit( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void viewWillMerge( WorkpaneEvent event ) throws WorkpaneVetoException {
			events.add( event );
		}

		@Override
		public void viewMerged( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void toolAdded( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void toolRemoved( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void toolActivated( WorkpaneEvent event ) {
			events.add( event );
		}

		@Override
		public void toolDeactivated( WorkpaneEvent event ) {
			events.add( event );
		}

	}

}
