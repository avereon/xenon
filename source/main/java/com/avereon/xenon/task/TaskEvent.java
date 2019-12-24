package com.avereon.xenon.task;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class TaskEvent extends Event {

	public TaskEvent( Object source, EventType<? extends Event> type ) {
		super( source, type );
	}

}
