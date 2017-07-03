package com.parallelsymmetry.essence.node;

public class CircularReferenceException extends RuntimeException {

	private static final long serialVersionUID = 8169280314381710751L;

	public CircularReferenceException() {
		super();
	}

	public CircularReferenceException( String message ) {
		super( message );
	}

	public CircularReferenceException( Throwable cause ) {
		super( cause );
	}

	public CircularReferenceException( String message, Throwable cause ) {
		super( message, cause );
	}

}
