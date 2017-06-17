package com.parallelsymmetry.essence.transaction;

public interface TxnEventDispatcher {

	void dispatchEvent( TxnEvent event );

}
