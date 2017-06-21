package com.parallelsymmetry.essence.transaction;

import java.util.EventObject;

public class TxnEvent extends EventObject {

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

}
