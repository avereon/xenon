package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			log.error( "Error running task", throwable );
			throw throwable;
		}
	}

}
