package com.xeomar.xenon.task;

final class TaskThread extends Thread {

	private TaskManager manager;

	TaskThread( TaskManager manager, ThreadGroup group, Runnable target, String name, long stackSize ) {
		super( group, target, name, stackSize );
		this.manager = manager;
		manager.taskThreadEvent( this, TaskEvent.Type.THREAD_CREATE );
	}

	@Override
	public void run() {
		try {
			super.run();
		} finally {
			manager.taskThreadEvent( this, TaskEvent.Type.THREAD_FINISH );
		}
	}

}
