package com.avereon.xenon.transaction;

import java.util.ArrayList;
import java.util.List;

public class  TxnOperationResult {

	private TxnOperation action;

	private List<TxnEvent> events = new ArrayList<>();

	TxnOperationResult( TxnOperation action ) {
		this.action = action;
	}

	public TxnOperation getOperation() {
		return action;
	}

	public List<TxnEvent> getEvents() {
		return events;
	}

	public void addEvent( TxnEvent event ) {
		events.add( event );
	}

	public void removeEvent( TxnEvent event ) {
		events.remove( event );
	}

}
