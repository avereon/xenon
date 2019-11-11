package com.avereon.xenon.task;

import com.avereon.settings.Settings;
import com.avereon.util.Configurable;
import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;

public class TaskManager implements Configurable, Controllable<TaskManager> {

	private static final int LOW_THREAD_COUNT = 4;

	private static final int HIGH_THREAD_COUNT = 32;

	private static final int THREAD_IDLE_SECONDS = 2;

	private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

	private static final int DEFAULT_MIN_THREAD_COUNT = Math.max( 4, PROCESSOR_COUNT / 4 );

	private static final int DEFAULT_MAX_THREAD_COUNT = Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 );

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ThreadPoolExecutor executorP1;

	private ThreadPoolExecutor executorP2;

	private ThreadPoolExecutor executorP3;

	private ThreadGroup group;

	private int maxThreadCount;

	private Settings settings;

	private Map<Task,Task> taskMap;

	private Queue<Task> taskQueue;

	private Set<TaskListener> listeners;

	public TaskManager() {
		taskMap = new ConcurrentHashMap<>(  );
		taskQueue = new ConcurrentLinkedQueue<>();
		group = new ThreadGroup( getClass().getName() );
		listeners = new CopyOnWriteArraySet<>();
		setMaxThreadCount( DEFAULT_MAX_THREAD_COUNT );
	}

	public static void taskThreadCheck() {
		if( !TaskManager.isTaskThread() ) {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();

			String callingClass = stack[ 2 ].getClassName();
			String callingMethod = stack[ 2 ].getMethodName();
			String callingFile = stack[ 2 ].getFileName();
			int callingLine = stack[ 2 ].getLineNumber();

			String caller = callingClass + "." + callingMethod + "(" + callingFile + ":" + callingLine + ")";

			throw new RuntimeException( caller + " not called on task thread" );
		}
	}

	public <T> Task<T> submit( Task<T> task ) {
		Task<T> existing = getExisting( task );
		if( existing != null ) return existing;

		task.setState( Task.State.SCHEDULED );

		Task<T> result;
		switch( task.getPriority() ) {
			case HIGH: {
				result = (Task<T>)checkRunning( executorP1 ).submit( (Callable<T>)task );
				break;
			}
			case LOW: {
				result = (Task<T>)checkRunning( executorP3 ).submit( (Callable<T>)task );
				break;
			}
			default: {
				result = (Task<T>)checkRunning( executorP2 ).submit( (Callable<T>)task );
				break;
			}
		}

		return result;
	}

	@SuppressWarnings( "unchecked" )
	private <T> Task<T> getExisting( Task<T> task ) {
		return (Task<T>)taskMap.get( task );
	}

	public long getTaskCount() {
		return getTasks().size();
	}

	public List<Task> getTasks() {
		return new ArrayList<>( taskQueue );
	}

	public int getCurrentThreadCount() {
		int count = 0;
		count += executorP1 == null ? 0 : executorP1.getPoolSize();
		count += executorP2 == null ? 0 : executorP2.getPoolSize();
		count += executorP3 == null ? 0 : executorP3.getPoolSize();
		return count;
	}

	public int getMaxThreadCount() {
		int count = 0;
		count += executorP1 == null ? 0 : executorP1.getCorePoolSize();
		count += executorP2== null ? 0 : executorP2.getCorePoolSize();
		count += executorP3 == null ? 0 : executorP3.getCorePoolSize();
		return count;
	}

	public void setMaxThreadCount( int count ) {
		maxThreadCount = Math.min( Math.max( LOW_THREAD_COUNT, count ), HIGH_THREAD_COUNT );
		if( settings != null ) settings.set( "thread-count", maxThreadCount );
		if( executorP1 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( 1 ) );
		if( executorP2 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( 2 ) );
		if( executorP3 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( 3 ) );
	}

	@Override
	public boolean isRunning() {
		return executorP1 != null && !executorP1.isTerminated() && executorP2 != null && !executorP2.isTerminated() && executorP3 != null && !executorP3.isTerminated();
	}

	@Override
	public TaskManager start() {
		if( isRunning() ) return this;
		executorP1 = new TaskManagerExecutor(
			getPriorityThreadCount( 1 ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.NORM_PRIORITY )
		);
		executorP2 = new TaskManagerExecutor(
			getPriorityThreadCount( 2 ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY + 1 )
		);
		executorP3 = new TaskManagerExecutor(
			getPriorityThreadCount( 3 ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY )
		);
		return this;
	}

//	@Override
//	public TaskManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
//		return this;
//	}
//
//	@Override
//	public TaskManager restart() {
//		new Thread( () -> {
//			stop();
//			try {
//				// TODO Make task manager requestRestart timeout a setting?
//				awaitStop( 10, TimeUnit.SECONDS );
//				start();
//			} catch( InterruptedException exception ) {
//				exception.printStackTrace();
//			}
//		} ).start();
//
//		return this;
//	}
//
//	@Override
//	public TaskManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
//		return awaitStart( timeout, unit );
//	}

	@Override
	public TaskManager stop() {
		executorP3 = shutdown( executorP3 );
		executorP2 = shutdown( executorP2 );
		executorP1 = shutdown( executorP1 );
		try {
			if( executorP3 != null ) executorP3.awaitTermination( Program.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			if( executorP2 != null ) executorP2.awaitTermination( Program.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			if( executorP1 != null ) executorP1.awaitTermination( Program.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception
		}
		return this;
	}

	private ThreadPoolExecutor shutdown( ThreadPoolExecutor executor ) {
		if( executor == null ) return null;
		executor.shutdown();
		return null;
	}

//	@Override
//	public TaskManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
//		if( executorP3 != null ) executorP3.awaitTermination( timeout, unit );
//		if( executorP2 != null ) executorP2.awaitTermination( timeout, unit );
//		if( executorP1 != null ) executorP1.awaitTermination( timeout, unit );
//		return this;
//	}

	@Override
	public void setSettings( Settings settings ) {
		this.settings = settings;
		this.maxThreadCount = settings.get( "thread-count", Integer.class, maxThreadCount );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	public void addTaskListener( TaskListener listener ) {
		listeners.add( listener );
	}

	public void removeTaskListener( TaskListener listener ) {
		listeners.remove( listener );
	}

	Set<TaskListener> getTaskListeners() {
		return listeners;
	}

	void taskThreadEvent( TaskThread thread, TaskEvent.Type type ) {
		new TaskEvent( this, null, type ).fire( listeners );
	}

	private static boolean isTaskThread() {
		return Thread.currentThread() instanceof TaskThread;
	}

	private int getPriorityThreadCount( int priority ) {
		switch( priority ) {
			case 1:
			case 3: {
				return maxThreadCount / 4;
			}
			default:
				return maxThreadCount / 2;
		}
	}

	private ExecutorService checkRunning( ExecutorService executor ) {
		if( executor == null ) throw new RuntimeException( "TaskManager is not running." );
		return executor;
	}

	private class TaskManagerExecutor extends ThreadPoolExecutor {

		TaskManagerExecutor( int poolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory ) {
			super( poolSize, poolSize, keepAliveTime, unit, workQueue, threadFactory );
			allowCoreThreadTimeOut( true );
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor( Callable<T> callable ) {
			if( !(callable instanceof Task) ) callable = new TaskWrapper<>( callable );

			Task<T> task = (Task<T>)callable;
			task.setTaskManager( TaskManager.this );
			taskMap.put( task, task );
			taskQueue.add( task );
			return task;
		}

		@Override
		protected void afterExecute( Runnable runnable, Throwable throwable ) {
			if( runnable instanceof Task ) {
				Task task = (Task)runnable;
				taskQueue.remove( task );
				taskMap.remove( task );
			}
		}

	}

}
