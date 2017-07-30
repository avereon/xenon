package com.xeomar.xenon.transaction;

import java.util.EventObject;

public class TxnEvent extends EventObject implements Comparable<TxnEvent> {

	private TxnEventDispatcher dispatcher;

	public TxnEvent( TxnEventDispatcher dispatcher ) {
		this( dispatcher, dispatcher );
	}

	public TxnEvent( Object source, TxnEventDispatcher dispatcher ) {
		super( source );
		this.dispatcher = dispatcher;
	}

	public TxnEventDispatcher getDispatcher() {
		return dispatcher;
	}

	@Override
	public int compareTo( TxnEvent event ) {
		return 0;
	}

}
