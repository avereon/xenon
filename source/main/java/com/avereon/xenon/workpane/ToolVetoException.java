package com.avereon.xenon.workpane;

public class ToolVetoException extends Exception {

	public ToolVetoException() {
		super();
	}

	public ToolVetoException( String message ) {
		super( message );
	}

	public ToolVetoException( Throwable cause ) {
		super( cause );
	}

	public ToolVetoException( String message, Throwable cause ) {
		super( message, cause );
	}

}
