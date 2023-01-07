package com.avereon.xenon.task;

import com.avereon.util.ThreadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class TaskManagerTest extends BaseTaskTest {

	private static final int GET_TASK_TIMEOUT = 500;

	private TaskManager manager;

	private TaskWatcher watcher;

	@BeforeEach
	@Override
	public void setup() {
		watcher = new TaskWatcher();
		manager = new TaskManager();
		manager.getEventBus().register( TaskManagerEvent.ANY, watcher );
	}

	@Test
	void testStartAndAwait() {
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );
		manager.stop();
	}

	@Test
	void testStopAndWait() {
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );
		manager.stop();
		assertThat( manager.isRunning() ).isEqualTo( false );
	}

	@Test
	void testStartAndStop() {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		manager.stop();
		assertThat( manager.isRunning() ).isEqualTo( false );
	}

	@Test
	void testRestart() {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		manager.stop();
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		manager.stop();
		assertThat( manager.isRunning() ).isEqualTo( false );
	}

	@Test
	void testStopBeforeStart() {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.stop();
		assertThat( manager.isRunning() ).isEqualTo( false );
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testSubmitNullRunnable() {
		assertThat( manager.isRunning() ).isEqualTo( false );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		try {
			manager.submit( Task.of( "", (Runnable)null ) );
			fail( "TaskManager.submit(null) should throw a NullPointerException" );
		} catch( NullPointerException exception ) {
			assertThat( exception ).isInstanceOf( NullPointerException.class );
		}
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testSubmitNullCallable() {
		assertThat( manager.isRunning() ).isEqualTo( false );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		try {
			manager.submit( Task.of( "", (Callable<?>)null ) );
			fail( "TaskManager.submit(null) should throw a NullPointerException" );
		} catch( NullPointerException exception ) {
			assertThat( exception ).isInstanceOf( NullPointerException.class );
		}
	}

	@Test
	void testSubmitNullResult() throws Exception {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		MockTask task = new MockTask( manager );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		Future<Object> future = manager.submit( task );
		assertThat( future.get() ).isNull();
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testSubmitWithResult() throws Exception {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		Future<Object> future = manager.submit( task );
		assertThat( future.get() ).isEqualTo( result );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testFailedTask() throws Exception {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		MockTask task = new MockTask( manager, null, true );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		manager.submit( task );
		try {
			assertThat( task.get() ).isNull();
			fail( "Task should throw an Exception" );
		} catch( ExecutionException exception ) {
			assertThat( exception ).isInstanceOf( ExecutionException.class );
			assertThat( exception.getCause() ).isInstanceOf( TaskException.class );
			assertThat( exception.getCause().getCause() ).isInstanceOf( Exception.class );
			assertThat( exception.getCause().getCause().getMessage() ).isEqualTo( MockTask.EXCEPTION_MESSAGE );
			assertThat( exception.getCause().getCause().getCause() ).isNull();
		}
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.FAILED );
	}

	@Test
	void testSubmitBeforeStart() {
		assertThat( manager.isRunning() ).isEqualTo( false );

		MockTask task = new MockTask( manager );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		assertThat( manager.submit( task ) ).isNull();

		assertThat( manager.isRunning() ).isEqualTo( false );
		assertThat( task.isDone() ).isEqualTo( false );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );
	}

	@Test
	void testUsingTaskAsFuture() throws Exception {
		assertThat( manager.isRunning() ).isEqualTo( false );

		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		manager.submit( task );
		assertThat( task.get() ).isEqualTo( result );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testNestedTask() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult );
		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		manager.submit( task );
		assertThat( task.get( GET_TASK_TIMEOUT, TimeUnit.MILLISECONDS ) ).isEqualTo( result );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testNestedTaskWithException() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult, true );
		assertThat( nestedTask.getState() ).isEqualTo( Task.State.READY );

		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );

		manager.submit( task );

		// Check the parent task.
		task.get( GET_TASK_TIMEOUT, TimeUnit.MILLISECONDS );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );

		// Check the nested task.
		try {
			assertThat( nestedTask.get( GET_TASK_TIMEOUT, TimeUnit.MILLISECONDS ) ).isNull();
			fail( "Task should throw an Exception" );
		} catch( ExecutionException exception ) {
			assertThat( exception ).isInstanceOf( ExecutionException.class );
			assertThat( exception.getCause() ).isInstanceOf( TaskException.class );
			assertThat( exception.getCause().getCause() ).isInstanceOf( Exception.class );
			assertThat( exception.getCause().getCause().getMessage() ).isEqualTo( MockTask.EXCEPTION_MESSAGE );
			assertThat( exception.getCause().getCause().getCause() ).isNull();
		}
		assertThat( nestedTask.isDone() ).isEqualTo( true );
		assertThat( nestedTask.isCancelled() ).isEqualTo( false );
		assertThat( nestedTask.getState() ).isEqualTo( Task.State.FAILED );
	}

	@Test
	void testTaskListener() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		Future<Object> future = manager.submit( task );
		assertThat( future.get() ).isEqualTo( result );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.isCancelled() ).isEqualTo( false );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
		// Wait for the task thread to stop also - this happens after the thread idle timeout
		watcher.waitForEvent( TaskThreadEvent.FINISH, manager.getThreadIdleTimeout() + 5000 );

		int index = 0;
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskEvent.SUBMITTED );
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskThreadEvent.CREATE );
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskEvent.START );
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskEvent.PROGRESS );
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskEvent.SUCCESS );
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskEvent.FINISH );
		assertThat( watcher.getEvents().get( index++ ).getEventType() ).isEqualTo( TaskThreadEvent.FINISH );
		assertThat( watcher.getEvents().size() ).isEqualTo( index );
	}

	@Test
	void testTaskCascadeOnPriority() throws Exception {
		manager.setP1ThreadCount( 1 );
		manager.setP2ThreadCount( 1 );
		manager.setP3ThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );
		assertThat( manager.getP1ThreadCount() ).isEqualTo( 1 );
		assertThat( manager.getP2ThreadCount() ).isEqualTo( 1 );
		assertThat( manager.getP3ThreadCount() ).isEqualTo( 1 );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		task.setPriority( Task.Priority.LOW );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		Future<Object> future = manager.submit( task );
		assertThat( future.get() ).isEqualTo( result );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.getProcessedPriority() ).isEqualTo( Task.Priority.LOW );
	}

	@Test
	void testTaskCascadeOnAvailability() throws Exception {
		manager.setP1ThreadCount( 1 );
		manager.setP2ThreadCount( 1 );
		manager.setP3ThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning() ).isEqualTo( true );
		assertThat( manager.getP1ThreadCount() ).isEqualTo( 1 );
		assertThat( manager.getP2ThreadCount() ).isEqualTo( 1 );
		assertThat( manager.getP3ThreadCount() ).isEqualTo( 1 );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		task.setPriority( Task.Priority.HIGH );
		assertThat( task.getState() ).isEqualTo( Task.State.READY );
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		// Submit a high priority task that takes a "long" time
		Task<?> longTask = new MockTask( manager, 10000 ).setPriority( Task.Priority.HIGH );
		manager.submit( longTask );

		// Small pause to let the longTask get rolling
		ThreadUtil.pause( 100 );

		Future<Object> future = manager.submit( task );
		assertThat( future.get() ).isEqualTo( result );
		assertThat( task.isDone() ).isEqualTo( true );
		assertThat( task.getProcessedPriority() ).isEqualTo( Task.Priority.MEDIUM );

		longTask.cancel( true );
	}

}
