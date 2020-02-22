package com.avereon.xenon.workpane;

import javafx.event.EventType;

public class EdgeEvent extends WorkpaneEvent {

	public static final EventType<EdgeEvent> EDGE = new EventType<>( WorkpaneEvent.ANY, "EDGE" );

	public static final EventType<EdgeEvent> ANY = EDGE;

	public static final EventType<EdgeEvent> ADDED = new EventType<>( EDGE, "ADDED" );

	public static final EventType<EdgeEvent> REMOVED = new EventType<>( EDGE, "REMOVED" );

	public static final EventType<EdgeEvent> MOVED = new EventType<>( EDGE, "MOVED" );

	private WorkpaneEdge edge;

	private double position;

	public EdgeEvent( Object source, EventType<? extends EdgeEvent> eventType, Workpane workpane, WorkpaneEdge edge) {
		super( source, eventType, workpane );
		this.edge = edge;
		this.position = edge.getPosition();
	}

	public WorkpaneEdge getEdge() {
		return edge;
	}

	public double getPosition() {
		return position;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends EdgeEvent> getEventType() {
		return (EventType<? extends EdgeEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + getEdge();
	}

}
