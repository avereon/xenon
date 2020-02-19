package com.avereon.xenon.throwable;

import com.avereon.util.Log;

import java.lang.System.Logger;

public class ProgramUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = Log.get();

	@Override
	public void uncaughtException( Thread thread, Throwable throwable ) {
		log.log( Log.ERROR, "Uncaught exception on " + thread.getName() + " thread", throwable );
	}

}
