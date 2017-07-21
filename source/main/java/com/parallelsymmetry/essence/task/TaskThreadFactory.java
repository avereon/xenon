package com.parallelsymmetry.essence.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskThreadFactory implements ThreadFactory {

	private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

	private final AtomicInteger threadNumber = new AtomicInteger( 1 );

	private final ThreadGroup group;

	private final String prefix;

	public TaskThreadFactory( ThreadGroup group) {
		this.group = group;
		prefix = "TaskPool-" + poolNumber.getAndIncrement() + "-Thread-";
	}

	@Override
	public Thread newThread( Runnable runnable ) {
		Thread thread = new TaskThread( group, runnable, prefix + threadNumber.getAndIncrement(), 0 );
		if( thread.getPriority() != Thread.NORM_PRIORITY ) thread.setPriority( Thread.NORM_PRIORITY );
		if( !thread.isDaemon() ) thread.setDaemon( true );
		return thread;
	}

}
