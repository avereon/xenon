package com.avereon.xenon;

import com.avereon.util.Log;

import java.lang.System.Logger;

class ProgramUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = Log.log();

	@Override
	public void uncaughtException( Thread thread, Throwable throwable ) {
		log.log( Log.ERROR, "Uncaught exception on " + thread.getName() + " thread", throwable );
	}

}
