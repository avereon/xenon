package com.xeomar.xenon.transaction;

public interface TxnEventDispatcher {

	void dispatchEvent( TxnEvent event );

}
