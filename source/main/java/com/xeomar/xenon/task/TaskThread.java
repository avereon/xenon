package com.xeomar.xenon.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskThread extends Thread {

	private Logger log  = LoggerFactory.getLogger( TaskThread.class );

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
