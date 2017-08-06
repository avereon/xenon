package com.xeomar.xenon.task;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TaskEventTest extends BaseTaskTest {

	@Test
	public void testSuccess() throws Exception {
		Task<Object> task = new MockTask( manager );

		TaskWatcher watcher = new TaskWatcher();
		task.addTaskListener( watcher );

		manager.submit( task );
		task.get();

		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );

		/**
		 * Because there are two threads involved in this test, the test thread
		 * needs to wait for the eventList to arrive. Task is required to ensure the
		 * done state is set correctly before eventList are sent but this allows the
		 * test thread to continue before the eventList arrive.
		 */
		watcher.waitForEvent( TaskEvent.Type.TASK_FINISH );
		assertEvent( watcher.getEvents().get( 0 ), task, TaskEvent.Type.TASK_SUBMITTED );
		assertEvent( watcher.getEvents().get( 1 ), task, TaskEvent.Type.TASK_START );
		assertEvent( watcher.getEvents().get( 2 ), task, TaskEvent.Type.TASK_PROGRESS );
		assertEvent( watcher.getEvents().get( 3 ), task, TaskEvent.Type.TASK_FINISH );
		assertThat( watcher.getEvents().size(), is( 4 ) );
	}

	@Test
	public void testFailure() throws Exception {
		Task<Object> task = new MockTask( manager, null, true );

		TaskWatcher watcher = new TaskWatcher();
		task.addTaskListener( watcher );

		manager.submit( task );
		try {
			task.get();
			Assert.fail( "Exception should be thrown." );
		} catch( Exception exception ) {
			assertThat( exception, not( is( nullValue() ) ) );
		}

		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.FAILED ) );

		/**
		 * Because there are two threads involved in this test, the test thread
		 * needs to wait for the eventList to arrive. Task is required to ensure the
		 * done state is set correctly before eventList are sent but this allows the
		 * test thread to continue before the eventList arrive.
		 */
		watcher.waitForEvent( TaskEvent.Type.TASK_FINISH );
		assertEvent( watcher.getEvents().get( 0 ), task, TaskEvent.Type.TASK_SUBMITTED );
		assertEvent( watcher.getEvents().get( 1 ), task, TaskEvent.Type.TASK_START );
		assertEvent( watcher.getEvents().get( 2 ), task, TaskEvent.Type.TASK_PROGRESS );
		assertEvent( watcher.getEvents().get( 3 ), task, TaskEvent.Type.TASK_FINISH );
		assertThat( watcher.getEvents().size(), is( 4 ) );
	}

	private void assertEvent( TaskEvent event, Task<?> task, TaskEvent.Type type ) {
		assertThat( event.getTask(), is( task ) );
		assertThat( event.getType(), is( type ) );
	}

}
