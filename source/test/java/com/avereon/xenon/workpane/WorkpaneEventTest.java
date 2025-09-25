package com.avereon.xenon.workpane;

import com.avereon.zerra.event.FxEventWatcher;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneEventTest extends WorkpaneTestCase {

	@Test
	void testMoveEdge() {
		WorkpaneView west = workpane.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		workpane.moveEdge( edge, 25 );

		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 0 ), EdgeEvent.MOVED, workpane, edge );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 1 ), WorkpaneEvent.CHANGED, workpane );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 2 );
	}

	@Test
	void testMoveEdgeNowhere() {
		Workpane area = new Workpane();
		WorkpaneView west = area.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.moveEdge( edge, 0 );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	@Test
	void testSetActiveView() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		WorkpaneView north = area.split( south, Side.TOP );
		area.setActiveView( south );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setActiveView( north );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 3 );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 0 ), ViewEvent.DEACTIVATED, area, south );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.ACTIVATED, area, north );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 2 ), WorkpaneEvent.CHANGED, area );
	}

	@Test
	void testSetActiveViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setActiveView( south );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setActiveView( south );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	@Test
	void testSetActiveViewWithViewFromOtherArea() {
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		WorkpaneView view0 = area0.getDefaultView();
		WorkpaneView view1 = area1.getDefaultView();
		area0.setActiveView( view0 );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area0.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area0.setActiveView( view1 );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	@Test
	void testSetDefaultView() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		WorkpaneView north = area.split( south, Side.TOP );
		area.setDefaultView( south );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setDefaultView( north );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().getFirst(), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 1 );
	}

	@Test
	void testSetDefaultViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setDefaultView( south );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setDefaultView( south );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	@Test
	void testSetMaximizedView() {
		Workpane area = new Workpane();
		area.setMaximizedView( null );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setMaximizedView( area.getDefaultView() );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().getFirst(), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 1 );
	}

	@Test
	void testSplit() {
		Workpane area = new Workpane();

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		WorkpaneView north = area.split( Side.TOP );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 0 ), EdgeEvent.ADDED, area, north.getEdge( Side.BOTTOM ) );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.ADDED, area, north );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 2 ), ViewEvent.SPLIT, area, null );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 3 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 4 );
	}

	@Test
	void testSplitWithView() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		WorkpaneView north = area.split( view, Side.TOP );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 0 ), ViewEvent.SPLITTING, area, view );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 1 ), EdgeEvent.ADDED, area, view.getEdge( Side.TOP ) );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 2 ), ViewEvent.ADDED, area, north );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 3 ), ViewEvent.SPLIT, area, view );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 5 );
	}

	@Test
	void testMerge() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		WorkpaneView north = area.split( view, Side.TOP );
		WorkpaneEdge edge = view.getEdge( Side.TOP );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		// when
		area.pushMerge( view, Side.TOP );

		// then
		int workpaneEventCount = 0;
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ViewEvent.MERGING, area, view );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ViewEvent.REMOVED, area, north );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), EdgeEvent.REMOVED, area, edge );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ViewEvent.MERGED, area, view );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( workpaneEventCount );
	}

	@Test
	void testOpenTool() {
		// given
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		FxEventWatcher<ToolEvent> toolEventWatcher = new FxEventWatcher<>();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		// when
		area.openTool( tool, view );

		// then
		int toolEventCount = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.OPENING, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.REORDERED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.DISPLAYED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.ACTIVATED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.OPENED, tool );
		assertThat( toolEventWatcher.getEvents().size() ).isEqualTo( toolEventCount );

		int workpaneEventCount = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.ADDED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.DISPLAYED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.ACTIVATED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( workpaneEventCount );
	}

	@Test
	void testAddTool() {
		// given
		Tool tool = new MockTool( asset );
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();

		FxEventWatcher<ToolEvent> toolEventWatcher = new FxEventWatcher<>();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		// when
		area.addTool( tool, view );

		// then
		int toolEventCount = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.REORDERED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.DISPLAYED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.ACTIVATED, tool );
		assertThat( toolEventWatcher.getEvents().size() ).isEqualTo( toolEventCount );

		int workpaneEventCount = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.ADDED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.DISPLAYED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.ACTIVATED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( workpaneEventCount );
	}

	@Test
	void testRemoveTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		FxEventWatcher<ToolEvent> toolEventWatcher = new FxEventWatcher<>();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.removeTool( tool );

		// then
		int toolEventCount = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.CONCEALED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.REMOVED, tool );
		assertThat( toolEventWatcher.getEvents().size() ).isEqualTo( toolEventCount );

		int workpaneEventCount = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.CONCEALED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.REMOVED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( workpaneEventCount );
	}

	@Test
	void testCloseTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		FxEventWatcher<ToolEvent> toolEventWatcher = new FxEventWatcher<>();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.closeTool( tool );

		//then
		int toolEventCount = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.CLOSING, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.CONCEALED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.REMOVED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( toolEventCount++ ), ToolEvent.CLOSED, tool );
		assertThat( toolEventWatcher.getEvents().size() ).isEqualTo( toolEventCount );

		int workpaneEventCount = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.CONCEALED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), ToolEvent.REMOVED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( workpaneEventCount++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( workpaneEventCount );
	}

	@Test
	void testSetActiveTool() {
		// given
		Workpane area = new Workpane();
		WorkpaneView southView = area.getDefaultView();
		WorkpaneView northView = area.split( southView, Side.TOP );
		Tool northTool = new MockTool( asset );
		Tool southTool = new MockTool( asset );
		area.addTool( northTool, northView );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		// when
		area.setActiveTool( northTool );

		// then
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( 0 ), ToolEvent.DEACTIVATED, area, southTool );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.DEACTIVATED, area, southView );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 2 ), ViewEvent.ACTIVATED, area, northView );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( 3 ), ToolEvent.ACTIVATED, area, northTool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 5 );
	}

	@Test
	void testSetActiveToolWithToolNotAdded() {
		Workpane area = new Workpane();
		WorkpaneView southView = area.getDefaultView();
		Tool northTool = new MockTool( asset );
		Tool southTool = new MockTool( asset );
		area.addTool( southTool, southView );
		area.setActiveTool( southTool );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area.setActiveTool( northTool );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	@Test
	void testSetActiveToolWithToolFromOtherArea() {
		// given
		Workpane area0 = new Workpane();
		Workpane area1 = new Workpane();
		WorkpaneView view0 = area0.getDefaultView();
		WorkpaneView view1 = area1.getDefaultView();
		Tool tool0 = new MockTool( asset );
		Tool tool1 = new MockTool( asset );
		area0.addTool( tool0, view0 );
		area1.addTool( tool1, view1 );
		area0.setActiveTool( tool0 );

		FxEventWatcher<Event> workpaneWatcher = new FxEventWatcher<>();
		area0.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		// when
		area0.setActiveTool( tool1 );

		// then
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	private void assertWorkpaneEvent( WorkpaneEvent event, EventType<WorkpaneEvent> type, Workpane area ) {
		assertThat( event.getEventType() ).withFailMessage( "Event type" ).isEqualTo( type );
		assertThat( event.getWorkpane() ).withFailMessage( "Work area" ).isEqualTo( area );
	}

	private void assertWorkpaneEdgeEvent( EdgeEvent event, EventType<EdgeEvent> type, Workpane area, WorkpaneEdge edge ) {
		assertThat( event.getEventType() ).withFailMessage( "Event type" ).isEqualTo( type );
		assertThat( event.getWorkpane() ).withFailMessage( "Work area" ).isEqualTo( area );
		assertThat( event.getEdge() ).withFailMessage( "Tool edge" ).isEqualTo( edge );
	}

	private void assertWorkpaneViewEvent( ViewEvent event, EventType<ViewEvent> type, Workpane pane, WorkpaneView view ) {
		assertThat( event.getEventType() ).withFailMessage( "Event type" ).isEqualTo( type );
		assertThat( event.getWorkpane() ).withFailMessage( "Work area" ).isEqualTo( pane );
		assertThat( event.getView() ).withFailMessage( "Tool view" ).isEqualTo( view );
	}

	private void assertWorkpaneToolEvent( ToolEvent event, EventType<ToolEvent> type, Workpane area, Tool tool ) {
		assertThat( event.getEventType() ).withFailMessage( "Event type" ).isEqualTo( type );
		assertThat( event.getWorkpane() ).withFailMessage( "Work pane" ).isEqualTo( area );
		assertThat( event.getTool() ).withFailMessage( "Tool" ).isEqualTo( tool );
	}

	private void assertToolEvent( ToolEvent event, javafx.event.EventType<ToolEvent> type, Tool tool ) {
		assertThat( event.getEventType() ).withFailMessage( "Event type" ).isEqualTo( type );
		assertThat( event.getTool() ).withFailMessage( "Tool" ).isEqualTo( tool );
	}

}
