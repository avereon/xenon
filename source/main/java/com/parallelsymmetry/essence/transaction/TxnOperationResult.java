package com.parallelsymmetry.essence.transaction;

import java.util.ArrayList;
import java.util.List;

public class TxnOperationResult {

	private TxnOperation action;

	private List<TxnEvent> events;

	public TxnOperationResult( TxnOperation action ) {
		this.action = action;
	}

	public TxnOperation getOperation() {
		return action;
	}

	public List<TxnEvent> getEvents() {
		return events == null ? new ArrayList<>() : events;
	}

	public void addEvent( TxnEvent event ) {
		if( events == null ) events = new ArrayList<>();
		events.add( event );
	}

}
