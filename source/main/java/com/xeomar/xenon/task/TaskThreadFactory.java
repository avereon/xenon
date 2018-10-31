package com.xeomar.xenon.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskThreadFactory implements ThreadFactory {

	private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

	private final AtomicInteger threadNumber = new AtomicInteger( 1 );

	private final TaskManager manager;

	private final ThreadGroup group;

	private final String prefix;

	public TaskThreadFactory( TaskManager manager, ThreadGroup group) {
		this.manager = manager;
		this.group = group;
		prefix = "TaskPool-" + poolNumber.getAndIncrement() + "-worker-";
	}

	@Override
	public Thread newThread( Runnable runnable ) {
		Thread thread = new TaskThread( manager, group, runnable, prefix + threadNumber.getAndIncrement(), 0 );
		thread.setPriority( Thread.NORM_PRIORITY );
		thread.setDaemon( true );
		return thread;
	}

}
