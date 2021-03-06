package com.avereon.xenon.test.task;

import com.avereon.util.ThreadUtil;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskEvent;
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
	void testPercent() {
		Task<?> task = new MockTask( manager );
		assertThat( task.getPercent(), is( (double)Task.INDETERMINATE ) );

		task.setTotal( 0 );
		assertThat( task.getPercent(), is( Double.NaN ) );

		task.setTotal( 10 );
		assertThat( task.getPercent(), is( 0.0 ) );

		task.setProgress( -1 );
		assertThat( task.getPercent(), is( 0.0 ) );

		int total = 10;
		for( int count = 0; count <= total; count++ ) {
			task.setProgress( count );
			assertThat( task.getPercent(), is( (double)count / (double)total ) );
		}

		task.setProgress( 11 );
		assertThat( task.getPercent(), is( 1.0 ) );
	}

	@Test
	void testIndeterminatePercent() {
		Task<?> task = new MockTask( manager );
		assertThat( task.getPercent(), is( (double)Task.INDETERMINATE ) );

		task.setProgress( 0 );
		assertThat( task.getPercent(), is( (double)Task.INDETERMINATE ) );

		task.setProgress( 1 );
		assertThat( task.getPercent(), is( (double)Task.INDETERMINATE ) );
	}

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
