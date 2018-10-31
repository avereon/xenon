package com.xeomar.xenon.task;

import com.xeomar.product.ProductEvent;

public class TaskEvent extends ProductEvent {

	public enum Type {
		TASK_SUBMITTED, TASK_START, TASK_PROGRESS, TASK_FINISH, THREAD_CREATE, THREAD_FINISH
	}

	private static final long serialVersionUID = 6199687149599225794L;

	private Task<?> task;

	private Type type;

	public TaskEvent( Object source, Task<?> task, Type type ) {
		super( source );
		this.task = task;
		this.type = type;
	}

	public Task<?> getTask() {
		return task;
	}

	public Type getType() {
		return type;
	}

}
