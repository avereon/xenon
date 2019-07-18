package com.avereon.xenon.transaction;

public interface TxnEventDispatcher {

	void dispatchEvent( TxnEvent event );

}
