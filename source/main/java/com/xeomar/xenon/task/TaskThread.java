package com.xeomar.xenon.task;

final class TaskThread extends Thread {

	TaskThread( ThreadGroup group, Runnable target, String name, long stackSize ) {
		super( group, target, name, stackSize );
	}

}
