package com.avereon.xenon.workpane;

import com.avereon.zarra.event.FxEventWatcher;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class WorkpaneEventTest extends WorkpaneTestCase {

	@Test
	void testMoveEdge() {
		WorkpaneView west = workpane.split( Side.LEFT );
		WorkpaneEdge edge = west.getEdge( Side.RIGHT );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setDefaultView( north );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 1 );
	}

	@Test
	void testSetDefaultViewSame() {
		Workpane area = new Workpane();
		WorkpaneView south = area.getDefaultView();
		area.setDefaultView( south );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setDefaultView( south );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
	}

	@Test
	void testSetMaximizedView() {
		Workpane area = new Workpane();
		area.setMaximizedView( null );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		area.setMaximizedView( area.getDefaultView() );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 0 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 1 );
	}

	@Test
	void testSplit() {
		Workpane area = new Workpane();

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area.pushMerge( view, Side.TOP );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 0 ), ViewEvent.MERGING, area, view );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 1 ), ViewEvent.REMOVED, area, north );
		assertWorkpaneEdgeEvent( (EdgeEvent)workpaneWatcher.getEvents().get( 2 ), EdgeEvent.REMOVED, area, edge );
		assertWorkpaneViewEvent( (ViewEvent)workpaneWatcher.getEvents().get( 3 ), ViewEvent.MERGED, area, view );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( 4 ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 5 );
	}

	@Test
	void testOpenTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.openTool( tool, view );
		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ADDED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( count );

		count = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.OPENING, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ORDERED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.OPENED, tool );
		assertThat( toolEventWatcher.events.size() ).isEqualTo( count );
	}

	@Test
	void testAddTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.addTool( tool, view );
		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ADDED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( count );

		count = 0;
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ORDERED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ADDED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.DISPLAYED, tool );
		assertToolEvent( toolEventWatcher.getEvents().get( count++ ), ToolEvent.ACTIVATED, tool );
		assertThat( toolEventWatcher.events.size() ).isEqualTo( count );
	}

	@Test
	void testRemoveTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.removeTool( tool );

		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.CONCEALED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.REMOVED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( count );

		count = 0;
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CONCEALED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.REMOVED, tool );
		assertThat( toolEventWatcher.events.size() ).isEqualTo( count );
	}

	@Test
	void testCloseTool() {
		Workpane area = new Workpane();
		WorkpaneView view = area.getDefaultView();
		Tool tool = new MockTool( asset );
		area.addTool( tool, view );

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		ToolEventWatcher toolEventWatcher = new ToolEventWatcher();
		tool.addEventHandler( ToolEvent.ANY, toolEventWatcher );

		area.closeTool( tool );

		int count = 0;
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.DEACTIVATED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.CONCEALED, area, tool );
		assertWorkpaneToolEvent( (ToolEvent)workpaneWatcher.getEvents().get( count++ ), ToolEvent.REMOVED, area, tool );
		assertWorkpaneEvent( (WorkpaneEvent)workpaneWatcher.getEvents().get( count++ ), WorkpaneEvent.CHANGED, area );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( count );

		count = 0;
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CLOSING, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.DEACTIVATED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CONCEALED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.REMOVED, tool );
		assertToolEvent( toolEventWatcher.events.get( count++ ), ToolEvent.CLOSED, tool );
		assertThat( toolEventWatcher.events.size() ).isEqualTo( count );
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area.setActiveTool( northTool );
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area.setActiveTool( northTool );
		assertThat( workpaneWatcher.getEvents().size() ).isEqualTo( 0 );
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

		FxEventWatcher workpaneWatcher = new FxEventWatcher();
		area0.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );

		area0.setActiveTool( tool1 );
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
