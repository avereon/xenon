package com.avereon.xenon.workpane;

import javafx.event.EventType;

public class ViewEvent extends WorkpaneEvent {

	public static final EventType<ViewEvent> VIEW = new EventType<>( WorkpaneEvent.ANY, "VIEW" );

	public static final EventType<ViewEvent> ANY = VIEW;

	public static final EventType<ViewEvent> ADDED = new EventType<>( VIEW, "ADDED" );

	public static final EventType<ViewEvent> REMOVED = new EventType<>( VIEW, "REMOVED" );

	public static final EventType<ViewEvent> SPLITTING = new EventType<>( VIEW, "SPLITTING" );

	public static final EventType<ViewEvent> SPLIT = new EventType<>( VIEW, "SPLIT" );

	public static final EventType<ViewEvent> MERGING = new EventType<>( VIEW, "MERGING" );

	public static final EventType<ViewEvent> MERGED = new EventType<>( VIEW, "MERGED" );

	public static final EventType<ViewEvent> ACTIVATED = new EventType<>( VIEW, "ACTIVATED" );

	public static final EventType<ViewEvent> DEACTIVATED = new EventType<>( VIEW, "DEACTIVATED" );

	public static final EventType<ViewEvent> DROP = new EventType<>( VIEW, "DROP" );

	private WorkpaneView view;

	public ViewEvent( Object source, EventType<? extends ViewEvent> eventType, Workpane workpane, WorkpaneView view ) {
		super( source, eventType, workpane );
		this.view = view;
	}

	public WorkpaneView getView() {
		return view;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends ViewEvent> getEventType() {
		return (EventType<? extends ViewEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + getView();
	}

}
