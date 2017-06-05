package com.parallelsymmetry.essence.data;

import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OperationResult {

	private Operation action;

	private List<EventObject> events;

	public OperationResult( Operation action ) {
		this.action = action;
		this.events = new CopyOnWriteArrayList<>();
	}

	public Operation getOperation() {
		return action;
	}

	public List<EventObject> getEvents() {
		return events;
	}

	public void addEvent( EventObject event ) {
		events.add( event );
	}

}
