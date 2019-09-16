package com.avereon.xenon;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

class ProgramUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = LogUtil.get( Program.class );

	@Override
	public void uncaughtException( Thread thread, Throwable exception ) {
		String message = "Uncaught program exception";

		log.error( message, exception );

		System.err.println( message );
		exception.printStackTrace( System.err );
	}

}
