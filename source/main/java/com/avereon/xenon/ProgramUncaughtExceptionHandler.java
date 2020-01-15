package com.avereon.xenon;

import com.avereon.util.LogUtil;
import com.avereon.xenon.notice.NoticeManager;
import org.slf4j.Logger;

class ProgramUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = LogUtil.get( Program.class );

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
			log.error( message, throwable );
		}
	}

}
