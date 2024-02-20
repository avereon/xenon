package com.avereon.xenon.task;

/**
 * The TaskException is a convenience class to help manage exceptions in tasks.
 * When a checked exception is thrown during the execution of a task, it is
 * recommended to wrap it in a TaskException and rethrow it.
 */
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
