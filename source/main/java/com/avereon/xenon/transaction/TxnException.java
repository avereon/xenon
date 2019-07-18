package com.avereon.xenon.transaction;

@SuppressWarnings( { "unused", "WeakerAccess" } )
public class TxnException extends Exception {

	public TxnException() {
		super();
	}

	public TxnException( String message ) {
		super( message );
	}

	public TxnException( String message, Throwable cause ) {
		super( message, cause );
	}

	public TxnException( Throwable cause ) {
		super( cause );
	}

}
