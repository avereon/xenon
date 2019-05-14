package com.xeomar.xenon.task;

import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;
import com.xeomar.util.Controllable;
import com.xeomar.util.LogUtil;
import com.xeomar.util.TestUtil;
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

	private ThreadPoolExecutor executor;

	private ThreadGroup group;

	private int maxThreadCount;

	private Settings settings;

	private BlockingQueue<Runnable> queue;

	private Queue<Task> tasks;

	private Set<TaskListener> listeners;

	public TaskManager() {
		tasks = new ConcurrentLinkedQueue<>();
		queue = new LinkedBlockingQueue<>();
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
		return (Task<T>)checkRunning().submit( (Callable<T>)task );
	}

	public long getTaskCount() {
		return getTasks().size();
	}

	public List<Task> getTasks() {
		return new ArrayList<>( tasks );
	}

	public int getCurrentThreadCount() {
		return executor.getPoolSize();
	}

	public int getMaxThreadCount() {
		return executor.getCorePoolSize();
	}

	public void setMaxThreadCount( int count ) {
		maxThreadCount = Math.min( Math.max( LOW_THREAD_COUNT, count ), HIGH_THREAD_COUNT );

		if( settings != null ) settings.set( "thread-count", maxThreadCount );

		if( isRunning() ) {
			try {
				restart();
			} catch( Exception exception ) {
				log.error( "Error restarting task manager with new thread count", exception );
			}
		}
	}

	@Override
	public boolean isRunning() {
		return executor != null && !executor.isTerminated();
	}

	@Override
	public TaskManager start() {
		if( isRunning() ) return this;
		executor = new TaskManagerExecutor( maxThreadCount, THREAD_IDLE_SECONDS, TimeUnit.SECONDS, queue, new TaskThreadFactory( this, group ) );
		return this;
	}

	@Override
	public TaskManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public TaskManager restart() {
		new Thread( () -> {
			stop();
			try {
				// TODO Make task manager requestRestart timeout a setting?
				awaitStop( 10, TimeUnit.SECONDS );
				start();
			} catch( InterruptedException exception ) {
				exception.printStackTrace();
			}
		} ).start();

		return this;
	}

	@Override
	public TaskManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return awaitStart( timeout, unit );
	}

	@Override
	public TaskManager stop() {
		if( executor == null || executor.isShutdown() ) return this;
		executor.shutdown();
		return this;
	}

	@Override
	public TaskManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		if( executor == null || executor.isShutdown() ) return this;
		executor.awaitTermination( timeout, unit );
		return this;
	}

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

	private ExecutorService checkRunning() {
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
			tasks.add( task );
			return task;
		}

		@Override
		protected void afterExecute( Runnable runnable, Throwable throwable ) {
			if( runnable instanceof Task ) {
				Task task = (Task)runnable;

				tasks.remove( task );

				try {
					task.get();
				} catch( Throwable getThrowable ) {
					if( !TestUtil.isTest() ) log.error( "Exception executing task", getThrowable );
				}
			}
		}

	}

}
