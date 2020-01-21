package com.avereon.xenon.task;

import java.util.concurrent.Callable;

public class TaskWrapper<T> extends Task<T> {

	private Callable<T> callable;

	public TaskWrapper( Callable<T> callable ) {
		this.callable = callable;
	}

	@Override
	public T call() throws Exception {
		return callable.call();
	}

	@Override
	public String toString() {
		return "Wrapped " + callable.toString();
	}

}
