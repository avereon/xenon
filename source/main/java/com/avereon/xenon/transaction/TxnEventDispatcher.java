package com.avereon.xenon.transaction;

public interface TxnEventDispatcher< T extends TxnEvent> {

	void dispatchEvent( T event );

}
