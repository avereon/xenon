package com.avereon.xenon.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskThreadFactory implements ThreadFactory {

	private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

	private final AtomicInteger threadNumber = new AtomicInteger( 1 );

	private final TaskManager manager;

	private final ThreadGroup group;

	private final String prefix;

	private int priority;

	public TaskThreadFactory( TaskManager manager, ThreadGroup group, int priority ) {
		this.manager = manager;
		this.group = group;
		this.priority = priority;
		prefix = "TaskPool-" + poolNumber.getAndIncrement() + "-worker-";
	}

	@Override
	public Thread newThread( Runnable runnable ) {
		Thread thread = new TaskThread( manager, group, runnable, prefix + threadNumber.getAndIncrement(), 0 );
		thread.setPriority( priority );
		thread.setDaemon( true );
		return thread;
	}

}
