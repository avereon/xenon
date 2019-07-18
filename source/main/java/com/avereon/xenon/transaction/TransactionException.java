package com.avereon.xenon.transaction;

@SuppressWarnings( { "unused", "WeakerAccess" } )
public class TransactionException extends RuntimeException {

	public TransactionException() {}

	public TransactionException( String message ) {
		super( message );
	}

	public TransactionException( Throwable cause ) {
		super( cause );
	}

	public TransactionException( String message, Throwable cause ) {
		super( message, cause );
	}

}
