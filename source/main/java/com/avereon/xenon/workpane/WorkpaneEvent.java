package com.avereon.xenon.workpane;

import com.avereon.util.JavaUtil;
import javafx.event.Event;
import javafx.event.EventType;
import lombok.Getter;

@Getter
public class WorkpaneEvent extends Event {

	public static final EventType<WorkpaneEvent> WORKPANE = new EventType<>( Event.ANY, "WORKPANE" );

	public static final EventType<WorkpaneEvent> ANY = WORKPANE;

	public static final EventType<WorkpaneEvent> CHANGED = new EventType<>( WORKPANE, "CHANGED" );

	private final Workpane workpane;

	public WorkpaneEvent( Object source, EventType<? extends WorkpaneEvent> eventType, Workpane workpane ) {
		super( source, null, eventType );
		this.workpane = workpane;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends WorkpaneEvent> getEventType() {
		return (EventType<? extends WorkpaneEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		String sourceClass = JavaUtil.getClassName( source.getClass() );
		String eventClass = JavaUtil.getClassName( this.getClass() );
		return sourceClass + " > " + eventClass + " : " + getEventType().getName();
	}

}
