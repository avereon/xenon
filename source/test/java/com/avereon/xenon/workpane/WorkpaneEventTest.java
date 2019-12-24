package com.avereon.xenon.workpane;

import com.avereon.event.EventType;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkpaneEventTest extends WorkpaneTestCase {

	@Test
	void testMoveEdge() {
		WorkpaneView west = workpane.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		workpane.addWorkpaneListener( workpaneWatcher );
		workpane.moveEdge( edge, 25 );
		assertWorkpaneEdgeEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.EDGE_MOVED, workpane, edge );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.CHANGED, workpane );
		assertThat( workpaneWatcher.getEvents().size(), is( 2 ) );
	}

	@Test
	void testMoveEdgeNowhere() {
		Workpane area = new Workpane();
		WorkpaneView west = area.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.moveEdge( edge, 0 );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	void testSetActiveView() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		WorkpaneView north = area.split( south, Side.TOP );
		area.setActiveView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setActiveView( north );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, south );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, north );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.CHANGED, area );
	}

	@Test
	void testSetActiveViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setActiveView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setActiveView( south );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	void testSetActiveViewWithViewFromOtherArea() {
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
	void testSetDefaultView() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		WorkpaneView north = area.split( south, Side.TOP );
		area.setDefaultView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setDefaultView( north );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	void testSetDefaultViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setDefaultView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setDefaultView( south );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	void testSetMaximizedView() {
		Workpane area = new Workpane();
		area.setMaximizedView( null );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );
		area.setMaximizedView( area.getDefaultView() );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	void testSplit() {
		Workpane area = new Workpane();

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		WorkpaneView north = area.split( Side.TOP );
		assertWorkpaneEdgeEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.EDGE_ADDED, area, north.getEdge( Side.BOTTOM ) );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_ADDED, area, north );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.VIEW_SPLIT, area, null );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 4 ) );
	}

	@Test
	void testSplitWithView() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		WorkpaneView north = area.split( view, Side.TOP );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_WILL_SPLIT, area, view );
		assertWorkpaneEdgeEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.EDGE_ADDED, area, view.getEdge( Side.TOP ) );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.VIEW_ADDED, area, north );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.VIEW_SPLIT, area, view );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 5 ) );
	}

	@Test
	void testMerge() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		WorkpaneView north = area.split( view, Side.TOP );
		WorkpaneEdge edge = view.getEdge( Side.TOP );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		area.pushMerge( view, Side.TOP );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.VIEW_WILL_MERGE, area, view );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_REMOVED, area, north );
		assertWorkpaneEdgeEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.EDGE_REMOVED, area, edge );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.VIEW_MERGED, area, view );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 5 ) );
	}

	@Test
	void testOpenTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.openTool( tool, view );
		int count = 0;
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_ADDED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_DISPLAYED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.OPENING, tool );
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.ORDERED, tool );
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.ACTIVATED, tool );
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.OPENED, tool );
		assertThat( toolEventCounter.events.size(), is( count ) );
	}

	@Test
	void testAddTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.addTool( tool, view );
		int count = 0;
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_ADDED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_DISPLAYED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.ORDERED, tool );
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventCounter.getEvents().get( count++ ), ToolEvent.ACTIVATED, tool );
		assertThat( toolEventCounter.events.size(), is( count ) );
	}

	@Test
	void testRemoveTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.removeTool( tool );

		int count = 0;
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_CONCEALED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_REMOVED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventCounter.events.get( count++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventCounter.events.get( count++ ), ToolEvent.REMOVED, tool );
		assertThat( toolEventCounter.events.size(), is( count ) );
	}

	@Test
	void testCloseTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		ToolEventCounter toolEventCounter = new ToolEventCounter();
		tool.addToolListener( toolEventCounter );

		area.closeTool( tool );

		int count = 0;
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_CONCEALED, area, tool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.TOOL_REMOVED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventCounter.events.get( count++ ), ToolEvent.CLOSING, tool );
		assertToolEvent( toolEventCounter.events.get( count++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventCounter.events.get( count++ ), ToolEvent.REMOVED, tool );
		assertToolEvent( toolEventCounter.events.get( count++ ), ToolEvent.CLOSED, tool );
		assertThat( toolEventCounter.events.size(), is( count ) );
	}

	@Test
	void testSetActiveTool() {
		Workpane area = new Workpane();
		WorkpaneView southView = area.getDefaultView();
		WorkpaneView northView = area.split( southView, Side.TOP );
		Tool northTool = new MockTool( asset );
		Tool southTool = new MockTool( asset );
		area.addTool( northTool, northView );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		area.setActiveTool( northTool );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.Type.TOOL_DEACTIVATED, area, southTool );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.Type.VIEW_DEACTIVATED, area, southView );
		assertWorkpaneViewEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.Type.VIEW_ACTIVATED, area, northView );
		assertWorkpaneToolEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.Type.TOOL_ACTIVATED, area, northTool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.Type.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 5 ) );
	}

	@Test
	void testSetActiveToolWithToolNotAdded() {
		Workpane area = new Workpane();
		WorkpaneView southView = area.getDefaultView();
		Tool northTool = new MockTool( asset );
		Tool southTool = new MockTool( asset );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addWorkpaneListener( workpaneWatcher );

		area.setActiveTool( northTool );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	void testSetActiveToolWithToolFromOtherArea() {
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		WorkpaneView view0 = area0.getDefaultView();
		WorkpaneView view1 = area1.getDefaultView();
		Tool tool0 = new MockTool( asset );
		Tool tool1 = new MockTool( asset );
		area0.addTool( tool0, view0 );
		area1.addTool( tool1, view1 );
		area0.setActiveTool( tool0 );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area0.addWorkpaneListener( workpaneWatcher );

		area0.setActiveTool( tool1 );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@SuppressWarnings( "SameParameterValue" )
	private void assertWorkpaneEvent( WorkpaneEvent event, WorkpaneEvent.Type type, Workpane area ) {
		assertThat( "Event type", event.getType(), is( type ) );
		assertThat( "Work area", event.getWorkPane(), is( area ) );
	}

	private void assertWorkpaneEdgeEvent( WorkpaneEvent event, WorkpaneEvent.Type type, Workpane area, WorkpaneEdge edge ) {
		assertThat( "Event type", event.getType(), is( type ) );
		assertThat( "Work area", event.getWorkPane(), is( area ) );
		assertThat( "Tool edge", ((WorkpaneEdgeEvent)event).getEdge(), is( edge ) );
	}

	private void assertWorkpaneViewEvent( WorkpaneEvent event, WorkpaneEvent.Type type, Workpane area, WorkpaneView view ) {
		assertThat( "Event type", event.getType(), is( type ) );
		assertThat( "Work area", event.getWorkPane(), is( area ) );
		assertThat( "Tool view", ((WorkpaneViewEvent)event).getView(), is( view ) );
	}

	private void assertWorkpaneToolEvent( WorkpaneEvent event, WorkpaneEvent.Type type, Workpane area, Tool tool ) {
		assertThat( "Event type", event.getType(), is( type ) );
		assertThat( "Work area", event.getWorkPane(), is( area ) );
		assertThat( "Tool", ((WorkpaneToolEvent)event).getTool(), is( tool ) );
	}

	private void assertToolEvent( ToolEvent event, EventType<ToolEvent> type, Tool tool ) {
		assertThat( "Event type", event.getEventType(), is( type ) );
		assertThat( "Tool", event.getTool(), is( tool ) );
	}

	private static class ToolEventCounter implements ToolListener {

		List<ToolEvent> events = new CopyOnWriteArrayList<>();

		@Override
		public void handle( ToolEvent event ) {
			events.add( event );
		}

		public List<ToolEvent> getEvents() {
			return new ArrayList<>( events );
		}
	}

}
