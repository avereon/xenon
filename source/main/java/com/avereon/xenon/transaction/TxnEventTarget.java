package com.avereon.xenon.transaction;

public interface TxnEventTarget {

	void handle( TxnEvent event );

}
