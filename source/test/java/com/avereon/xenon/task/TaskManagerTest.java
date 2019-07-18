package com.avereon.xenon.task;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TaskManagerTest extends BaseTaskTest {

	private TaskManager manager;

	@Before
	@Override
	public void setup() {
		// Use a different manager instance
		manager = new TaskManager();
	}

	@Test
	public void testStartAndAwait() throws Exception {
		manager.start();
		manager.awaitStart( DEFAULT_WAIT_TIME, DEFAULT_WAIT_UNIT );
		assertThat( manager.isRunning(), is( true ) );
		manager.stop();
	}

	@Test
	public void testStopAndWait() throws Exception {
		manager.start();
		manager.awaitStart( DEFAULT_WAIT_TIME, DEFAULT_WAIT_UNIT );
		assertThat( manager.isRunning(), is( true ) );
		manager.stop();
		manager.awaitStop( DEFAULT_WAIT_TIME, DEFAULT_WAIT_UNIT );
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	public void testStartAndStop() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		manager.stop();
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	public void testRestart() throws Exception {
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
	public void testStopBeforeStart() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.stop();
		assertThat( manager.isRunning(), is( false ) );
	}

	@Test
	public void testSubmitNullRunnable() throws Exception {
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
	public void testSubmitNullCallable() throws Exception {
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
	public void testSubmitNullResult() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		MockTask task = new MockTask( manager );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( nullValue() ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testSubmitWithResult() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testFailedTask() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		MockTask task = new MockTask( manager, null, true );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		manager.submit( task );
		try {
			assertThat( task.get(), is( nullValue() ) );
			Assert.fail( "Task should throw an Exception" );
		} catch( ExecutionException exception ) {
			assertThat( exception, instanceOf( ExecutionException.class ) );
			assertThat( exception.getCause(), instanceOf( TaskException.class ) );
			assertThat( exception.getCause().getCause(), instanceOf( Exception.class ) );
			assertThat( exception.getCause().getCause().getMessage(), is( MockTask.EXCEPTION_MESSAGE ) );
		}
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.FAILED ) );
	}

	@Test
	public void testSubmitBeforeStart() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		MockTask task = new MockTask( manager );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		try {
			manager.submit( task );
			Assert.fail( "TaskManager.submit() should throw and exception if the manager is not running" );
		} catch( Exception exception ) {
			assertThat( exception, instanceOf( Exception.class ) );
		}

		assertThat( manager.isRunning(), is( false ) );
		assertThat( task.isDone(), is( false ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.WAITING ) );
	}

	@Test
	public void testUsingTaskAsFuture() throws Exception {
		assertThat( manager.isRunning(), is( false ) );

		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		manager.submit( task );
		assertThat( task.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testNestedTask() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult );
		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		manager.submit( task );
		assertThat( task.get( 100, TimeUnit.MILLISECONDS ), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testNestedTaskWithException() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		Object nestedResult = new Object();
		MockTask nestedTask = new MockTask( manager, nestedResult, true );
		assertThat( nestedTask.getState(), is( Task.State.WAITING ) );

		Object result = new Object();
		MockTask task = new MockTask( manager, result, nestedTask );
		assertThat( task.getState(), is( Task.State.WAITING ) );

		manager.submit( task );

		// Check the parent task.
		task.get( 100, TimeUnit.MILLISECONDS );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );

		// Check the nested task.
		try {
			assertThat( nestedTask.get( 100, TimeUnit.MILLISECONDS ), is( nullValue() ) );
			Assert.fail( "Task should throw an Exception" );
		} catch( ExecutionException exception ) {
			assertThat( exception, instanceOf( ExecutionException.class ) );
			assertThat( exception.getCause(), instanceOf( TaskException.class ) );
			assertThat( exception.getCause().getCause(), instanceOf( Exception.class ) );
			assertThat( exception.getCause().getCause().getMessage(), is( MockTask.EXCEPTION_MESSAGE ) );
		}
		assertThat( nestedTask.isDone(), is( true ) );
		assertThat( nestedTask.isCancelled(), is( false ) );
		assertThat( nestedTask.getState(), is( Task.State.FAILED ) );
	}

	@Test
	public void testTaskListener() throws Exception {
		manager.setMaxThreadCount( 1 );
		manager.start();
		assertThat( manager.isRunning(), is( true ) );

		TaskWatcher listener = new TaskWatcher();
		manager.addTaskListener( listener );

		Object result = new Object();
		MockTask task = new MockTask( manager, result );
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( listener.getEvents().size(), is( 0 ) );

		Future<Object> future = manager.submit( task );
		assertThat( future.get(), is( result ) );
		assertThat( task.isDone(), is( true ) );
		assertThat( task.isCancelled(), is( false ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );

		//		assertThat( listener.getEvents().get( 0 ).getType(), is( TaskEvent.Type.TASK_SUBMITTED ) );
		//		assertThat( listener.getEvents().get( 1 ).getType(), is( TaskEvent.Type.TASK_START ) );
		//		assertThat( listener.getEvents().get( 2 ).getType(), is( TaskEvent.Type.TASK_PROGRESS ) );
		//		assertThat( listener.getEvents().get( 3 ).getType(), is( TaskEvent.Type.TASK_FINISH ) );
		//		assertThat( listener.getEvents().size(), is( 4 ) );
	}

}
