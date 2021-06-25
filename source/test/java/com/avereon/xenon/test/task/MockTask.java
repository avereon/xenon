package com.avereon.xenon.test.task;

import com.avereon.util.ThreadUtil;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;

final class MockTask extends Task<Object> {

	static final String EXCEPTION_MESSAGE = "Intentionally fail task.";

	private int delay;

	private boolean fail;

	private Task<?> nest;

	private TaskManager manager;

	private Object object;

	MockTask( TaskManager manager ) {
		this( manager, null );
	}

	MockTask( TaskManager manager, int delay ) {
		this( manager, null, delay );
	}

	MockTask( TaskManager manager, int delay, boolean fail ) {
		this( manager, null, delay, fail );
	}

	MockTask( TaskManager manager, Object object ) {
		this( manager, object, 0 );
	}

	MockTask( TaskManager manager, Object object, boolean fail ) {
		this( manager, object, 0, fail );
	}

	MockTask( TaskManager manager, Object object, Task<?> nest ) {
		this.manager = manager;
		this.object = object;
		this.nest = nest;
	}

	private MockTask( TaskManager manager, Object object, int delay ) {
		this( manager, object, delay, false );
	}

	private MockTask( TaskManager manager, Object object, int delay, boolean fail ) {
		this.manager = manager;
		this.object = object;
		this.delay = delay;
		this.fail = fail;
	}

	@Override
	public Object call() throws Exception {
		if( delay > 0 ) ThreadUtil.pause( delay );
		if( fail ) throw new Exception( EXCEPTION_MESSAGE );
		if( nest != null ) manager.submit( nest );
		return object;
	}

	@Override
	protected void failed() {
		// This intentionally overrides the super method to reduce the output
	}
}
