package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import com.xeomar.util.TestUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public final class TaskThread extends Thread {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public TaskThread( ThreadGroup group, Runnable target, String name, long stackSize ) {
		super( group, target, name, stackSize );
	}

	@Override
	public void run() {
		try {
			super.run();
		} catch( Throwable throwable ) {
			if( !TestUtil.isTest() ) log.error( "Error running task", throwable );
		}
	}

}
