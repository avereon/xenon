package com.parallelsymmetry.essence.transaction;

import java.util.EventObject;

public class TxnEvent extends EventObject {

	private TxnEventDispatcher dispatcher;

	public TxnEvent( TxnEventDispatcher dispatcher ) {
		this( dispatcher, dispatcher );
	}

	public TxnEvent( TxnEventDispatcher dispatcher, Object source ) {
		super( source );
		this.dispatcher = dispatcher;
	}

	public TxnEventDispatcher getDispatcher() {
		return dispatcher;
	}

}
