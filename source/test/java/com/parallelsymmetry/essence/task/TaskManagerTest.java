package com.parallelsymmetry.essence.task;

import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TaskManagerTest extends TestCase {

	private TaskManager manager;

	@Override
	public void setUp() {
		manager = new TaskManager();
	}

	public void testStartAndWait() throws Exception {
		manager.startAndWait();
		assertTrue( manager.isRunning() );
		manager.stopAndWait();
	}

	public void testStopAndWait() throws Exception {
		manager.startAndWait();
		assertTrue( manager.isRunning() );
		manager.stopAndWait();
		assertFalse( manager.isRunning() );
	}

	public void testStartAndStop() throws Exception {
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		manager.stopAndWait();
		assertFalse( manager.isRunning() );
	}

	public void testRestart() throws Exception {
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		manager.stopAndWait();
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		manager.stopAndWait();
		assertFalse( manager.isRunning() );
	}

	public void testStopBeforeStart() throws Exception {
		assertFalse( manager.isRunning() );

		manager.stopAndWait();
		assertFalse( manager.isRunning() );
	}

	public void testSubmitWithNull() throws Exception {
		assertFalse( manager.isRunning() );
		manager.startAndWait();
		assertTrue( manager.isRunning() );

		try {
			manager.submit( null );
			fail( "TaskManager.submit(null) should throw a NullPointerException." );
		} catch( NullPointerException exception ) {
			// A NullPointerException should be thrown.
		}
	}

	public void testSubmitNullResult() throws Exception {
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		MockTask task = new MockTask( manager );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		Future<Object> future = manager.submit( task );
		assertNull( future.get() );
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.SUCCESS, task.getResult() );
		assertFalse( task.isCancelled() );
	}

	public void testSubmitWithResult() throws Exception {
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		Future<Object> future = manager.submit( task );
		assertEquals( result, future.get() );
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.SUCCESS, task.getResult() );
		assertFalse( task.isCancelled() );
	}

	public void testFailedTask() throws Exception {
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		MockTask task = new MockTask( manager, null, true );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		manager.submit( task );
		try {
			assertNull( task.get() );
			fail();
		} catch( ExecutionException exception ) {
			assertEquals( MockTask.EXCEPTION_MESSAGE, exception.getCause().getMessage() );
		}
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.FAILED, task.getResult() );
		assertFalse( task.isCancelled() );
	}

	public void testSubmitBeforeStart() throws Exception {
		assertFalse( manager.isRunning() );

		MockTask task = new MockTask( manager );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		try {
			manager.submit( task );
			fail( "TaskManager.submit() should throw and exception if the manager is not running." );
		} catch( Exception exception ) {
			// Intentionally ignore exception.
		}

		assertFalse( manager.isRunning() );
		assertFalse( task.isDone() );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );
		assertFalse( task.isCancelled() );
	}

	public void testUsingTaskAsFuture() throws Exception {
		assertFalse( manager.isRunning() );

		manager.startAndWait();
		assertTrue( manager.isRunning() );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		manager.submit( task );
		assertEquals( result, task.get() );
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.SUCCESS, task.getResult() );
		assertFalse( task.isCancelled() );
	}

	public void testNestedTask() throws Exception {
		manager.setThreadCount( 1 );
		manager.startAndWait();
		assertTrue( manager.isRunning() );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult );
		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		manager.submit( task );
		assertEquals( result, task.get( 100, TimeUnit.MILLISECONDS ) );
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.SUCCESS, task.getResult() );
		assertFalse( task.isCancelled() );
	}

	public void testNestedTaskWithException() throws Exception {
		manager.setThreadCount( 1 );
		manager.startAndWait();
		assertTrue( manager.isRunning() );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult, true );
		assertEquals( Task.State.WAITING, nestedTask.getState() );
		assertEquals( Task.Result.UNKNOWN, nestedTask.getResult() );

		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );

		manager.submit( task );

		// Check the parent task.
		task.get();
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.SUCCESS, task.getResult() );
		assertFalse( task.isCancelled() );

		// Check the nested task.
		try {
			assertNull( nestedTask.get() );
			fail();
		} catch( ExecutionException exception ) {
			assertEquals( MockTask.EXCEPTION_MESSAGE, exception.getCause().getMessage() );
		}
		assertTrue( nestedTask.isDone() );
		assertEquals( Task.State.DONE, nestedTask.getState() );
		assertEquals( Task.Result.FAILED, nestedTask.getResult() );
		assertFalse( nestedTask.isCancelled() );
	}

	public void testTaskListener() throws Exception {
		manager.setThreadCount( 1 );
		manager.startAndWait();
		assertTrue( manager.isRunning() );

		MockTaskListener listener = new MockTaskListener();
		manager.addTaskListener( listener );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertEquals( Task.State.WAITING, task.getState() );
		assertEquals( Task.Result.UNKNOWN, task.getResult() );
		assertEquals( 0, listener.events.size() );

		Future<Object> future = manager.submit( task );
		assertEquals( result, future.get() );
		assertTrue( task.isDone() );
		assertEquals( Task.State.DONE, task.getState() );
		assertEquals( Task.Result.SUCCESS, task.getResult() );
		assertFalse( task.isCancelled() );

		int index = 0;
		assertEquals( 5, listener.events.size() );
		assertEquals( TaskEvent.Type.TASK_SUBMITTED, listener.events.get( index++ ).getType() );
		assertEquals( TaskEvent.Type.TASK_START, listener.events.get( index++ ).getType() );
		assertEquals( TaskEvent.Type.TASK_PROGRESS, listener.events.get( index++ ).getType() );
		assertEquals( TaskEvent.Type.TASK_FINISH, listener.events.get( index++ ).getType() );
		assertEquals( TaskEvent.Type.TASK_COMPLETED, listener.events.get( index++ ).getType() );
	}

	private class MockTaskListener implements TaskListener {

		public List<TaskEvent> events = new CopyOnWriteArrayList<TaskEvent>();

		@Override
		public void handleEvent( TaskEvent event ) {
			events.add( event );
		}

	}

}
