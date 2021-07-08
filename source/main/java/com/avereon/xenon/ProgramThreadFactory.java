package com.avereon.xenon;

import lombok.CustomLog;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@CustomLog
public class ProgramThreadFactory implements ThreadFactory {

	private static final AtomicInteger count = new AtomicInteger();

	@Override
	public Thread newThread( Runnable runnable ) {
		Thread thread = new Thread( runnable, "program-thread-" + count.getAndIncrement() );
		thread.setUncaughtExceptionHandler( new ExceptionWatcher() );
		return thread;
	}

	private static class ExceptionWatcher implements Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException( Thread thread, Throwable throwable ) {
			log.atSevere().withCause( throwable ).log( "Error on thread %s", thread.getName() );
		}
	}

}
