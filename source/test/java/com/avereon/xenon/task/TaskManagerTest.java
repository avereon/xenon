package com.avereon.xenon.task;

import com.avereon.util.ThreadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

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
		assertThat( manager.isRunning(), is( true ) );
		manager.stop();
	}

	@Test
	void testStopAndWait() {
		manager.start();
		assertThat( manager.isRunning(), is( true ) );
		manager.stop();
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	void testStartAndStop() {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		manager.stop();
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	void testRestart() {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		manager.stop();
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		manager.stop();
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	void testStopBeforeStart() {
		assertThat( manager.isRunning(), is( false ) );

		manager.stop();
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	void testSubmitNullRunnable() {
		assertThat( manager.isRunning(), is( false ) );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		try {
			manager.submit( Task.of( "", (Runnable)null ) );
			fail( "TaskManager.submit(null) should throw a NullPointerException" );
		} catch( NullPointerException exception ) {
			assertThat( exception, instanceOf( NullPointerException.class ) );
		}
	}

	@Test
	void testSubmitNullCallable() {
		assertThat( manager.isRunning(), is( false ) );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		try {
			manager.submit( Task.of( "", (Callable<?>)null ) );
			fail( "TaskManager.submit(null) should throw a NullPointerException" );
		} catch( NullPointerException exception ) {
			assertThat( exception, instanceOf( NullPointerException.class ) );
		}
	}

	@Test
	void testSubmitNullResult() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		MockTask task = new MockTask( manager );
		assertThat( task.getState(), is( Task.State.READY ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( nullValue() ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testSubmitWithResult() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState(), is( Task.State.READY ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testFailedTask() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		MockTask task = new MockTask( manager, null, true );
		assertThat( task.getState(), is( Task.State.READY ) );

		manager.submit( task );
		try {
			assertThat( task.get(), is( nullValue() ) );
			fail( "Task should throw an Exception" );
		} catch( ExecutionException exception ) {
			assertThat( exception, instanceOf( ExecutionException.class ) );
			assertThat( exception.getCause(), instanceOf( TaskException.class ) );
			assertThat( exception.getCause().getCause(), instanceOf( Exception.class ) );
			assertThat( exception.getCause().getCause().getMessage(), is( MockTask.EXCEPTION_MESSAGE ) );
			assertThat( exception.getCause().getCause().getCause(), is( nullValue() ) );
		}
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.FAILED ) );
	}

	@Test
	void testSubmitBeforeStart() {
		assertThat( manager.isRunning(), is( false ) );

		MockTask task = new MockTask( manager );
		assertThat( task.getState(), is( Task.State.READY ) );

		assertNull( manager.submit( task ) );

		assertThat( manager.isRunning(), is( false ) );
		assertThat( task.isDone(), is( false ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.READY ) );
	}

	@Test
	void testUsingTaskAsFuture() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState(), is( Task.State.READY ) );

		manager.submit( task );
		assertThat( task.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testNestedTask() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult );
		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertThat( task.getState(), is( Task.State.READY ) );

		manager.submit( task );
		assertThat( task.get( GET_TASK_TIMEOUT, TimeUnit.MILLISECONDS ), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testNestedTaskWithException() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult, true );
		assertThat( nestedTask.getState(), is( Task.State.READY ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertThat( task.getState(), is( Task.State.READY ) );

		manager.submit( task );

		// Check the parent task.
		task.get( GET_TASK_TIMEOUT, TimeUnit.MILLISECONDS );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );

		// Check the nested task.
		try {
			assertThat( nestedTask.get( GET_TASK_TIMEOUT, TimeUnit.MILLISECONDS ), is( nullValue() ) );
			fail( "Task should throw an Exception" );
		} catch( ExecutionException exception ) {
			assertThat( exception, instanceOf( ExecutionException.class ) );
			assertThat( exception.getCause(), instanceOf( TaskException.class ) );
			assertThat( exception.getCause().getCause(), instanceOf( Exception.class ) );
			assertThat( exception.getCause().getCause().getMessage(), is( MockTask.EXCEPTION_MESSAGE ) );
			assertThat( exception.getCause().getCause().getCause(), is( nullValue() ) );
		}
		assertThat( nestedTask.isDone(), is( true ) );
		assertThat( nestedTask.isCancelled(), is( false ) );
		assertThat( nestedTask.getState(), is( Task.State.FAILED ) );
	}

	@Test
	void testTaskListener() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState(), is( Task.State.READY ) );
		assertThat( watcher.getEvents().size(), is( 0 ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
		// Wait for the task thread to stop also - this happens after the thread idle timeout
		watcher.waitForEvent( TaskThreadEvent.FINISH, manager.getThreadIdleTimeout() + 5000 );

		int index = 0;
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskEvent.SUBMITTED ) );
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskThreadEvent.CREATE ) );
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskEvent.START ) );
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskEvent.PROGRESS ) );
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskEvent.SUCCESS ) );
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskEvent.FINISH ) );
		assertThat( watcher.getEvents().get( index++ ).getEventType(), is( TaskThreadEvent.FINISH ) );
		assertThat( watcher.getEvents().size(), is( index ) );
	}

	@Test
	void testTaskCascadeOnPriority() throws Exception {
		manager.setP1ThreadCount( 1 );
		manager.setP2ThreadCount( 1 );
		manager.setP3ThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );
		assertThat( manager.getP1ThreadCount(), is( 1 ) );
		assertThat( manager.getP2ThreadCount(), is( 1 ) );
		assertThat( manager.getP3ThreadCount(), is( 1 ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		task.setPriority( Task.Priority.LOW );
		assertThat( task.getState(), is( Task.State.READY ) );
		assertThat( watcher.getEvents().size(), is( 0 ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.getProcessedPriority(), is( Task.Priority.LOW ) );
	}

	@Test
	void testTaskCascadeOnAvailability() throws Exception {
		manager.setP1ThreadCount( 1 );
		manager.setP2ThreadCount( 1 );
		manager.setP3ThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );
		assertThat( manager.getP1ThreadCount(), is( 1 ) );
		assertThat( manager.getP2ThreadCount(), is( 1 ) );
		assertThat( manager.getP3ThreadCount(), is( 1 ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		task.setPriority( Task.Priority.HIGH );
		assertThat( task.getState(), is( Task.State.READY ) );
		assertThat( watcher.getEvents().size(), is( 0 ) );

		// Submit a high priority task that takes a "long" time
		Task<?> longTask = new MockTask( manager, 10000 ).setPriority( Task.Priority.HIGH );
		manager.submit( longTask );

		// Small pause to let the longTask get rolling
		ThreadUtil.pause( 100 );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.getProcessedPriority(), is( Task.Priority.MEDIUM ) );

		longTask.cancel( true );
	}

}
