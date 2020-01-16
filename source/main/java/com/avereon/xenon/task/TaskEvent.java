package com.avereon.xenon.task;

import com.avereon.event.EventType;

public class TaskEvent extends TaskManagerEvent {

	public static final EventType<TaskEvent> TASK = new EventType<>( TaskManagerEvent.ANY, "TASK" );

	public static final EventType<TaskEvent> ANY = TASK;

	public static final EventType<TaskEvent> SUBMITTED = new EventType<>( TASK, "SUBMITTED" );

	public static final EventType<TaskEvent> START = new EventType<>( TASK, "START" );

	public static final EventType<TaskEvent> PROGRESS = new EventType<>( TASK, "PROGRESS" );

	public static final EventType<TaskEvent> FINISH = new EventType<>( TASK, "FINISH" );

	private Task<?> task;

	public TaskEvent( Object source, EventType<? extends TaskEvent> type, Task<?> task ) {
		super( source, type );
		this.task = task;
	}

	public Task<?> getTask() {
		return task;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends TaskEvent> getEventType() {
		return (EventType<TaskEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + task;
	}

}
