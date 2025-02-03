package com.avereon.xenon;

public class UiException extends RuntimeException {

	public UiException() {
		super();
	}

	public UiException( String message ) {
		super( message );
	}

	public UiException( String message, Throwable cause ) {
		super( message, cause );
	}

	public UiException( Throwable cause ) {
		super( cause );
	}

}
