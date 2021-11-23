package com.avereon.xenon.task;

import com.avereon.event.EventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class TaskEventTest extends BaseTaskTest {

	@Test
	void testSuccess() throws Exception {
		Task<Object> task = new MockTask( manager );

		TaskWatcher watcher = new TaskWatcher();
		task.register( TaskEvent.ANY, watcher );

		manager.submit( task );
		task.get();

		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );

		/*
		 * Because there are two threads involved in this test, the test thread
		 * needs to wait for the eventList to arrive. Task is required to ensure the
		 * done state is set correctly before eventList are sent but this allows the
		 * test thread to continue before the eventList arrive.
		 */
		int index = 0;
		watcher.waitForEvent( TaskEvent.FINISH );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.SUBMITTED );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.START );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.PROGRESS );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.SUCCESS );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.FINISH );
		assertThat( watcher.getEvents().size() ).isEqualTo( index );
	}

	@Test
	@SuppressWarnings( { "CatchMayIgnoreException", "ResultOfMethodCallIgnored" } )
	void testFailure() throws Exception {
		Task<Object> task = new MockTask( manager, null, true );

		TaskWatcher watcher = new TaskWatcher();
		task.register( TaskManagerEvent.ANY, watcher );

		manager.submit( task );
		try {
			task.get();
			fail( "Exception should be thrown." );
		} catch( Exception exception ) {
			assertThat( exception ).isNotNull();
		}

		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.FAILED );

		/*
		 * Because there are two threads involved in this test, the test thread
		 * needs to wait for the eventList to arrive. Task is required to ensure the
		 * done state is set correctly before eventList are sent but this allows the
		 * test thread to continue before the eventList arrive.
		 */
		int index = 0;
		watcher.waitForEvent( TaskEvent.FINISH );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.SUBMITTED );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.START );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.PROGRESS );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.FAILURE );
		assertEvent( watcher.getEvents().get( index++ ), task, TaskEvent.FINISH );
		assertThat( watcher.getEvents().size() ).isEqualTo( index );
	}

	private void assertEvent( TaskManagerEvent event, Task<?> task, EventType<? extends TaskManagerEvent> type ) {
		assertThat( ((TaskEvent)event).getTask() ).isEqualTo( task );
		assertThat( event.getEventType() ).isEqualTo( type );
	}

}
