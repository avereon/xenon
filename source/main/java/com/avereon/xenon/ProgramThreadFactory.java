package com.avereon.xenon;

import com.avereon.util.Log;
import java.lang.System.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgramThreadFactory implements ThreadFactory {

	private static final Logger log = Log.log();

	private static AtomicInteger count = new AtomicInteger();

	@Override
	public Thread newThread( Runnable runnable ) {
		Thread thread = new Thread( runnable, "program-thread-" + count.getAndIncrement() );
		thread.setUncaughtExceptionHandler( new ExceptionWatcher() );
		return thread;
	}

	private static class ExceptionWatcher implements Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException( Thread thread, Throwable throwable ) {
			log.log( Log.ERROR, "Error on thread " + thread.getName(), throwable );
		}
	}

}
