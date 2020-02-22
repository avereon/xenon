package com.avereon.xenon.task;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public abstract class TaskManagerEvent extends Event {

	public static final EventType<TaskManagerEvent> TASK_MANAGER = new EventType<>( Event.ANY, "TASK_MANAGER" );

	public static final EventType<TaskManagerEvent> ANY = TASK_MANAGER;

	public TaskManagerEvent( Object source, EventType<? extends TaskManagerEvent> type ) {
		super( source, type );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends TaskManagerEvent> getEventType() {
		return (EventType<TaskManagerEvent>)super.getEventType();
	}

}
