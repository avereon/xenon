package com.parallelsymmetry.essence.transaction;

public class TransactionException extends RuntimeException {

	private static final long serialVersionUID = -4305434261080128225L;

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
