package com.avereon.xenon.transaction;

public interface TxnEventTarget {

	void dispatch( TxnEvent event );

}
