package com.avereon.xenon.task;

public class TaskException extends RuntimeException {

	public TaskException() {
		super();
	}

	public TaskException( String message ) {
		super( message );
	}

	public TaskException( Throwable cause ) {
		super( cause );
	}

	public TaskException( String message, Throwable cause ) {
		super( message, cause );
	}

}
