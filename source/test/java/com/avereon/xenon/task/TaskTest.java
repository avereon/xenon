package com.avereon.xenon.task;

import com.avereon.util.ThreadUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TaskTest extends BaseTaskTest {

	/*
	 * Don't make this number too small. The smaller the number, the more likely
	 * the computer can't complete the task quickly enough to pass the test. A
	 * good time is between 10-50 milliseconds.
	 */
	private int delay = 50;

	@Test
	void testPriority() {
		Task<?> task = new MockTask( manager );

		// Check default priority.
		assertThat( task.getPriority(), is( Task.Priority.MEDIUM ) );

		// Check changing priority.
		task.setPriority( Task.Priority.LOW );
		assertThat( task.getPriority(), is( Task.Priority.LOW ) );
	}

	@Test
	void testSuccess() throws Exception {
		Task<?> task = new MockTask( manager, delay );
		ThreadUtil.pause( delay );
		assertThat( task.getState(), is( Task.State.READY ) );

		manager.submit( task );

		taskWatcher.waitForEvent( TaskEvent.START );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		taskWatcher.waitForEvent( TaskEvent.FINISH );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testFailure() throws Exception {
		Task<?> task = new MockTask( manager, 5 * delay, true );
		ThreadUtil.pause( delay );
		assertThat( task.getState(), is( Task.State.READY ) );

		manager.submit( task );

		taskWatcher.waitForEvent( TaskEvent.START );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		taskWatcher.waitForEvent( TaskEvent.FINISH );
		assertThat( task.getState(), is( Task.State.FAILED ) );
	}

}
