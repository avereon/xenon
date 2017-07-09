package com.parallelsymmetry.essence.task;

import com.parallelsymmetry.essence.util.ThreadUtil;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskTest extends BaseTaskTest {

	/*
	 * Don't make this number too small. The smaller the number, the more likely
	 * the computer can't complete the task quickly enough to pass the test. A
	 * good time is between 10-50 milliseconds.
	 */
	private int delay = 50;

	@Test
	public void testPriority() throws Exception {
		Task<?> task = new MockTask( manager );

		// Check default priority.
		assertThat( task.getPriority(), is( Task.Priority.MEDIUM ) );

		// Check changing priority.
		task.setPriority( Task.Priority.LOW );
		assertThat( task.getPriority(), is( Task.Priority.LOW ) );
	}

	@Test
	public void testSuccess() throws Exception {
		Task<?> task = new MockTask( manager, 4 * delay );
		ThreadUtil.pause( delay );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		manager.submit( task );

		taskWatcher.waitForEvent( TaskEvent.Type.TASK_START );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		ThreadUtil.pause( delay );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		ThreadUtil.pause( 2 * delay );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		ThreadUtil.pause( 2 * delay );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testFailure() throws Exception {
		Task<?> task = new MockTask( manager, 4 * delay, true );
		ThreadUtil.pause( delay );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		manager.submit( task );
		taskWatcher.waitForEvent( TaskEvent.Type.TASK_START );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		ThreadUtil.pause( delay );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		ThreadUtil.pause( 2 * delay );
		assertThat( task.getState(), is( Task.State.RUNNING ) );
		ThreadUtil.pause( 2 * delay );
		assertThat( task.getState(), is( Task.State.FAILED ) );
	}

}
