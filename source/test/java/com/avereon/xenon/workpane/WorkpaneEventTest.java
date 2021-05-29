package com.avereon.xenon.workpane;

import javafx.event.EventHandler;
import javafx.event.EventType;
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
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		workpane.moveEdge( edge, 25 );

		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 0 ), EdgeEvent.MOVED, workpane, edge );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.CHANGED, workpane );
		assertThat( workpaneWatcher.getEvents().size(), is( 2 ) );
	}

	@Test
	void testMoveEdgeNowhere() {
		Workpane area = new Workpane();
		WorkpaneView west = area.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
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
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setActiveView( north );
		assertThat( workpaneWatcher.getEvents().size(), is( 3 ) );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 0 ), ViewEvent.DEACTIVATED, area, south );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.ACTIVATED, area, north );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.CHANGED, area );
	}

	@Test
	void testSetActiveViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setActiveView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
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
		area0.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
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
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setDefaultView( north );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	void testSetDefaultViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setDefaultView( south );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setDefaultView( south );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	@Test
	void testSetMaximizedView() {
		Workpane area = new Workpane();
		area.setMaximizedView( null );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setMaximizedView( area.getDefaultView() );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 1 ) );
	}

	@Test
	void testSplit() {
		Workpane area = new Workpane();

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		WorkpaneView north = area.split( Side.TOP );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 0 ), EdgeEvent.ADDED, area, north.getEdge( Side.BOTTOM ) );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.ADDED, area, north );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 2 ), ViewEvent.SPLIT, area, null );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 4 ) );
	}

	@Test
	void testSplitWithView() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		WorkpaneView north = area.split( view, Side.TOP );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 0 ), ViewEvent.SPLITTING, area, view );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 1 ), EdgeEvent.ADDED, area, view.getEdge( Side.TOP ) );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 2 ), ViewEvent.ADDED, area, north );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 3 ), ViewEvent.SPLIT, area, view );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 5 ) );
	}

	@Test
	void testMerge() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		WorkpaneView north = area.split( view, Side.TOP );
		WorkpaneEdge edge = view.getEdge( Side.TOP );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area.pushMerge( view, Side.TOP );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 0 ), ViewEvent.MERGING, area, view );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.REMOVED, area, north );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 2 ), EdgeEvent.REMOVED, area, edge );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 3 ), ViewEvent.MERGED, area, view );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( 5 ) );
	}

	@Test
	void testOpenTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.openTool( tool, view );
		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ADDED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.OPENING, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ORDERED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.OPENED, tool );
		assertThat( toolEventWatcher.events.size(), is( count ) );
	}

	@Test
	void testAddTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.addTool( tool, view );
		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ADDED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ORDERED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, tool );
		assertThat( toolEventWatcher.events.size(), is( count ) );
	}

	@Test
	void testRemoveTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.removeTool( tool );

		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.CONCEALED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.REMOVED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CONCEALED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.REMOVED, tool );
		assertThat( toolEventWatcher.events.size(), is( count ) );
	}

	@Test
	void testCloseTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.closeTool( tool );

		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.CONCEALED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.REMOVED, area, tool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size(), is( count ) );

		count = 0;
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CLOSING, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CONCEALED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.REMOVED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CLOSED, tool );
		assertThat( toolEventWatcher.events.size(), is( count ) );
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
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area.setActiveTool( northTool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( 0 ), ToolEvent.DEACTIVATED, area, southTool );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.DEACTIVATED, area, southView );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 2 ), ViewEvent.ACTIVATED, area, northView );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( 3 ), ToolEvent.ACTIVATED, area, northTool );
		assertWorkpaneEvent( workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.CHANGED, area );
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
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

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
		area0.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area0.setActiveTool( tool1 );
		assertThat( workpaneWatcher.getEvents().size(), is( 0 ) );
	}

	private void assertWorkpaneEvent( WorkpaneEvent event, EventType<WorkpaneEvent> type, Workpane area ) {
		assertThat( "Event type", event.getEventType(), is( type ) );
		assertThat( "Work area", event.getWorkpane(), is( area ) );
	}

	private void assertWorkpaneEdgeEvent( EdgeEvent event, EventType<EdgeEvent> type, Workpane area, WorkpaneEdge edge ) {
		assertThat( "Event type", event.getEventType(), is( type ) );
		assertThat( "Work area", event.getWorkpane(), is( area ) );
		assertThat( "Tool edge", event.getEdge(), is( edge ) );
	}

	private void assertWorkpaneViewEvent( ViewEvent event, EventType<ViewEvent> type, Workpane pane, WorkpaneView view ) {
		assertThat( "Event type", event.getEventType(), is( type ) );
		assertThat( "Work area", event.getWorkpane(), is( pane ) );
		assertThat( "Tool view", event.getView(), is( view ) );
	}

	private void assertWorkpaneToolEvent( ToolEvent event, EventType<ToolEvent> type, Workpane area, Tool tool ) {
		assertThat( "Event type", event.getEventType(), is( type ) );
		assertThat( "Work area", event.getWorkpane(), is( area ) );
		assertThat( "Tool", event.getTool(), is( tool ) );
	}

	private void assertToolEvent( ToolEvent event, javafx.event.EventType<ToolEvent> type, Tool tool ) {
		assertThat( "Event type", event.getEventType(), is( type ) );
		assertThat( "Tool", event.getTool(), is( tool ) );
	}

	private static class ToolEventWatcher implements EventHandler<ToolEvent> {

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
