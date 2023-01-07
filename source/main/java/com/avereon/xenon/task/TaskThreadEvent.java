package com.avereon.xenon.task;

import com.avereon.event.EventType;

public class TaskThreadEvent extends TaskManagerEvent {

	public static final EventType<TaskThreadEvent> TASK_THREAD = new EventType<>( TaskManagerEvent.ANY, "TASK_THREAD" );

	public static final EventType<TaskThreadEvent> ANY = TASK_THREAD;

	public static final EventType<TaskThreadEvent> CREATE = new EventType<>( TASK_THREAD, "CREATE" );

	public static final EventType<TaskThreadEvent> FINISH = new EventType<>( TASK_THREAD, "FINISH" );

	private final Thread thread;

	public TaskThreadEvent( Object source, EventType<? extends TaskThreadEvent> type, Thread thread ) {
		super( source, type );
		this.thread = thread;
	}

	public Thread getThread() {
		return thread;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends TaskThreadEvent> getEventType() {
		return (EventType<TaskThreadEvent>)super.getEventType();
	}

}
