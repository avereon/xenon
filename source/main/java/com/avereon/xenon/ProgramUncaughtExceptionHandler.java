package com.avereon.xenon;

import com.avereon.util.Log;
import com.avereon.xenon.notice.NoticeManager;
import java.lang.System.Logger;

class ProgramUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = Log.log();

	private Program program;

	ProgramUncaughtExceptionHandler( Program program ) {
		this.program = program;
	}

	@Override
	public void uncaughtException( Thread thread, Throwable throwable ) {
		String message = "Uncaught exception on " + thread.getName() + " thread";
		NoticeManager noticeManager = program.getNoticeManager();
		if( noticeManager != null ) {
			noticeManager.error( message, throwable );
		} else {
			log.log( Log.ERROR,  message, throwable );
		}
	}

}
