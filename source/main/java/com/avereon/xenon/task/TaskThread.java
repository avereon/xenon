package com.avereon.xenon.task;

final class TaskThread extends Thread {

	private TaskManager manager;

	TaskThread( TaskManager manager, ThreadGroup group, Runnable target, String name, long stackSize ) {
		super( group, target, name, stackSize );
		this.manager = manager;
		manager.getEventBus().dispatch( new TaskThreadEvent( manager, TaskThreadEvent.CREATE, this ) );
	}

	@Override
	public void run() {
		try {
			super.run();
		} finally {
			manager.getEventBus().dispatch( new TaskThreadEvent( manager, TaskThreadEvent.FINISH, this ) );
		}
	}

}
