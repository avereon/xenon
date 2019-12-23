package com.avereon.xenon.task;

import com.avereon.xenon.ProductEventOld;

public class TaskEventOld extends ProductEventOld {

	public enum Type {
		TASK_SUBMITTED, TASK_START, TASK_PROGRESS, TASK_FINISH, THREAD_CREATE, THREAD_FINISH
	}

	private static final long serialVersionUID = 6199687149599225794L;

	private Task<?> task;

	private Type type;

	public TaskEventOld( Object source, Task<?> task, Type type ) {
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
