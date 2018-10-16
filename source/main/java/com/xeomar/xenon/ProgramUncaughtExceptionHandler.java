package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

class ProgramUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = LogUtil.get( Program.class );

	@Override
	public void uncaughtException( Thread thread, Throwable exception ) {
		log.error( "Uncaught exception", exception );
	}

}
