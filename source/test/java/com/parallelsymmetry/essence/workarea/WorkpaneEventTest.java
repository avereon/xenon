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

public class WorkpaneEventTest extends WorkpaneTestCase {

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
		WorkpaneView west = workpane.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		workpane.addWorkpaneListener( workpaneWatcher );
		workpane.moveEdge( edge, 25 );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.CHANGED, workpane, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	public void testMoveEdgeNowhere() {
		Workpane area = new Workpane();
		WorkpaneView west = area.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.moveEdge( edge, 0 );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	public void testSetActiveView() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		WorkpaneView north = area.split( south, Side.TOP );
		area.setActiveView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setActiveView( north );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, south, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, north, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
	}

	@Test
	public void testSetActiveViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setActiveView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setActiveView( south );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	public void testSetActiveViewWithViewFromOtherArea() {
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		WorkpaneView view0 = area0.getDefaultView();
		WorkpaneView view1 = area1.getDefaultView();
		area0.setActiveView( view0 );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area0.addWorkpaneListener( workpaneWatcher );
		area0.setActiveView( view1 );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	public void testSetDefaultView() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		WorkpaneView north = area.split( south, Side.TOP );
		area.setDefaultView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setDefaultView( north );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	public void testSetDefaultViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setDefaultView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setDefaultView( south );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	public void testSetMaximizedView() {
		Workpane area = new Workpane();
		area.setMaximizedView( null );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setMaximizedView( area.getDefaultView() );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	public void testSplit() {
		Workpane area = new Workpane();

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		WorkpaneView north = area.split( Side.TOP );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_ADDED, area, north, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_SPLIT, area, null, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
	}

	@Test
	public void testSplitWithView() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		WorkpaneView north = area.split( view, Side.TOP );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_WILL_SPLIT, area, view, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_ADDED, area, north, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.VIEW_SPLIT, area, view, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 4 ) );
	}

	@Test
	public void testMerge() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		WorkpaneView north = area.split( view, Side.TOP );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		area.pushMerge( view, Side.TOP );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_WILL_MERGE, area, view, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_REMOVED, area, north, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.VIEW_MERGED, area, view, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 4 ) );
	}

	@Test
	public void testAddTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( resource );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.addTool( tool, view );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.TOOL_ADDED, area, view, tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, view, tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
		assertThat( toolEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testRemoveTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( resource );
		area.addTool( tool, view );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.removeTool( tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, view, tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.TOOL_REMOVED, area, view, tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
		assertThat( toolEventCounter.events.size(), is( 0 ) );
	}

	@Test
	public void testCloseTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( resource );
		area.addTool( tool, view );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.closeTool( tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, view, tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.TOOL_REMOVED, area, view, tool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertToolEvent( toolEventCounter.events.get( 0 ), ToolEvent.Type.TOOL_CLOSING, tool );
		assertToolEvent( toolEventCounter.events.get( 1 ), ToolEvent.Type.TOOL_CLOSED, tool );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
		assertThat( toolEventCounter.events.size(), is( 2 ) );
	}

	@Test
	public void testSetActiveTool() {
		Workpane area = new Workpane();
		WorkpaneView southView = area.getDefaultView();
		WorkpaneView northView = area.split( southView, Side.TOP );
		Tool northTool = new MockTool( resource );
		Tool southTool = new MockTool( resource );
		area.addTool( northTool, northView );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		area.setActiveTool( northTool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, southView, southTool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, southView, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, northView, null );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, northView, northTool );
		assertWorkAreaEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.Type.CHANGED, area, null, null );
		assertThat( workpaneWatcher.getEvents().size(), is( 5 ) );
	}

	@Test
	public void testSetActiveToolWithToolNotAdded() {
		Workpane area = new Workpane();
		WorkpaneView southView = area.getDefaultView();
		Tool northTool = new MockTool( resource );
		Tool southTool = new MockTool( resource );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		area.setActiveTool( northTool );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	public void testSetActiveToolWithToolFromOtherArea() {
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		WorkpaneView view0 = area0.getDefaultView();
		WorkpaneView view1 = area1.getDefaultView();
		Tool tool0 = new MockTool( resource );
		Tool tool1 = new MockTool( resource );
		area0.addTool( tool0, view0 );
		area1.addTool( tool1, view1 );
		area0.setActiveTool( tool0 );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area0.addWorkpaneListener( workpaneWatcher );

		area0.setActiveTool( tool1 );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	private void assertWorkAreaEvent( WorkpaneEvent event, WorkpaneEvent.Type type, Workpane area, WorkpaneView view, Tool tool ) {
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

}
