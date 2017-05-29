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

	@Test
	public void testSetActiveView() {
		Workpane area = new Workpane();
		ToolView south = area.getDefaultView();
		ToolView north = area.split( south, Side.TOP );
		area.setActiveView( south );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );
		area.setActiveView( north );
		assertThat( workAreaEventCounter.events.size(), is( 3 ) );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, south, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, north, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	}

	@Test
	public void testSetActiveViewSame() {
		Workpane area = new Workpane();
		ToolView south = area.getDefaultView();
		area.setActiveView( south );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );
		area.setActiveView( south );
		assertThat( workAreaEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testSetActiveViewWithViewFromOtherArea() {
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		ToolView view0 = area0.getDefaultView();
		ToolView view1 = area1.getDefaultView();
		area0.setActiveView( view0 );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area0.addWorkPaneListener( workAreaEventCounter );
		area0.setActiveView( view1 );
		assertThat( workAreaEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testSetDefaultView() {
		Workpane area = new Workpane();
		ToolView south = area.getDefaultView();
		ToolView north = area.split( south, Side.TOP );
		area.setDefaultView( south );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );
		area.setDefaultView( north );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 1 ) );
	}

	@Test
	public void testSetDefaultViewSame() {
		Workpane area = new Workpane();
		ToolView south = area.getDefaultView();
		area.setDefaultView( south );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );
		area.setDefaultView( south );
		assertThat( workAreaEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testSetMaximizedView() {
		Workpane area = new Workpane();
		area.setMaximizedView( null );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );
		area.setMaximizedView( area.getDefaultView() );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 1 ) );
	}

	@Test
	public void testSplit() {
		Workpane area = new Workpane();

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		ToolView north = area.split( Side.TOP );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_ADDED, area, north, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_SPLIT, area, null, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 3 ) );
	}

	@Test
	public void testSplitWithView() {
		Workpane area = new Workpane();
		ToolView view = area.getDefaultView();

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		ToolView north = area.split( view, Side.TOP );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_WILL_SPLIT, area, view, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_ADDED, area, north, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.VIEW_SPLIT, area, view, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 3 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 4 ) );
	}

	@Test
	public void testMerge() {
		Workpane area = new Workpane();
		ToolView view = area.getDefaultView();
		ToolView north = area.split( view, Side.TOP );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		area.pushMerge( view, Side.TOP );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.VIEW_WILL_MERGE, area, view, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_REMOVED, area, north, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.VIEW_MERGED, area, view, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 3 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 4 ) );
	}

	@Test
	public void testAddTool() {
		Workpane area = new Workpane();
		ToolView view = area.getDefaultView();
		Tool tool = new MockTool( resource );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.addTool( tool, view );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_ADDED, area, view, tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, view, tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 3 ) );
		assertThat( toolEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testRemoveTool() {
		Workpane area = new Workpane();
		ToolView view = area.getDefaultView();
		Tool tool = new MockTool( resource );
		area.addTool( tool, view );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.removeTool( tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, view, tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.TOOL_REMOVED, area, view, tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 3 ) );
		assertThat( toolEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testCloseTool() {
		Workpane area = new Workpane();
		ToolView view = area.getDefaultView();
		Tool tool = new MockTool( resource );
		area.addTool( tool, view );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.closeTool( tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, view, tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.TOOL_REMOVED, area, view, tool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertToolEvent( toolEventCounter.events.get( 0 ), ToolEvent.Type.TOOL_CLOSING, tool );
		assertToolEvent( toolEventCounter.events.get( 1 ), ToolEvent.Type.TOOL_CLOSED, tool );
		assertThat( workAreaEventCounter.events.size(), is( 3 ) );
		assertThat( toolEventCounter.events.size(), is( 2 ) );
	}

	@Test
	public void testSetActiveTool() {
		Workpane area = new Workpane();
		ToolView southView = area.getDefaultView();
		ToolView northView = area.split( southView, Side.TOP );
		Tool northTool = new MockTool( resource );
		Tool southTool = new MockTool( resource );
		area.addTool( northTool, northView );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		area.setActiveTool( northTool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, southView, southTool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 1 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, southView, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 2 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, northView, null );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 3 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, northView, northTool );
		assertWorkAreaEvent( workAreaEventCounter.events.get( 4 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workAreaEventCounter.events.size(), is( 5 ) );
	}

	@Test
	public void testSetActiveToolWithToolNotAdded() {
		Workpane area = new Workpane();
		ToolView southView = area.getDefaultView();
		Tool northTool = new MockTool( resource );
		Tool southTool = new MockTool( resource );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area.addWorkPaneListener( workAreaEventCounter );

		area.setActiveTool( northTool );
		assertThat( workAreaEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testSetActiveToolWithToolFromOtherArea() {
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		ToolView view0 = area0.getDefaultView();
		ToolView view1 = area1.getDefaultView();
		Tool tool0 = new MockTool( resource );
		Tool tool1 = new MockTool( resource );
		area0.addTool( tool0, view0 );
		area1.addTool( tool1, view1 );
		area0.setActiveTool( tool0 );

		WorkPaneEventCounter workAreaEventCounter = new WorkPaneEventCounter();
		area0.addWorkPaneListener( workAreaEventCounter );

		area0.setActiveTool( tool1 );
		assertThat( workAreaEventCounter.events.size(), is( 0 ) );
	}

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
