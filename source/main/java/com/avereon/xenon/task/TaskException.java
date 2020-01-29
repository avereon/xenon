package com.avereon.xenon.task;

import java.util.Arrays;

public class TaskException extends RuntimeException {

	public TaskException() {
		super();
		setStackTrace( trimStackTrace() );
	}

	private StackTraceElement[] trimStackTrace() {
		int index = 0;
		String className = Task.class.getName();
		StackTraceElement[] elements = getStackTrace();

		while( className.equals( elements[ index ].getClassName() ) ) {
			index++;
		}

		return Arrays.copyOfRange( elements, index, elements.length );
	}

	@Override
	public String getMessage() {
		return getCause().getMessage();
	}

	@Override
	public String getLocalizedMessage() {
		return getCause().getLocalizedMessage();
	}

}
