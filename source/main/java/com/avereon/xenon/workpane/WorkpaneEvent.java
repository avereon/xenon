package com.avereon.xenon.workpane;

import javafx.event.Event;
import javafx.event.EventType;

public class WorkpaneEvent extends Event {

	public static final EventType<WorkpaneEvent> WORKPANE = new EventType<>( Event.ANY, "WORKPANE" );

	public static final EventType<WorkpaneEvent> ANY = WORKPANE;

	public static final EventType<WorkpaneEvent> CHANGED = new EventType<>( WORKPANE, "CHANGED" );

	private Workpane workpane;

	public WorkpaneEvent( Object source, EventType<? extends WorkpaneEvent> eventType, Workpane workpane ) {
		super( source, null, eventType );
		this.workpane = workpane;
	}

	public Workpane getWorkpane() {
		return workpane;
	}

}
