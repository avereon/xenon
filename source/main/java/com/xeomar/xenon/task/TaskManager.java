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

public class TaskManager implements ExecutorService, Configurable, Controllable<TaskManager> {

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

	@Override
	public void execute( Runnable task ) {
		submit( task );
	}

	@Override
	public <T> Future<T> submit( Callable<T> task ) {
		return checkRunning().submit( task );
	}

	@Override
	public Future<?> submit( Runnable task ) {
		return checkRunning().submit( task );
	}

	@Override
	public <T> Future<T> submit( Runnable task, T result ) {
		return checkRunning().submit( task, result );
	}

	/**
	 * Synchronously submit a task.
	 *
	 * @param task The task to execute
	 * @return The Future for the task
	 * @throws InterruptedException if the executing task is interrupted
	 */
	public <T> Future<T> invoke( Callable<T> task ) throws InterruptedException {
		List<Callable<T>> tasks = new ArrayList<>();
		tasks.add( task );
		List<Future<T>> futures = invokeAll( tasks );
		return futures.get( 0 );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException {
		return checkRunning().invokeAll( tasks );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException {
		return checkRunning().invokeAll( tasks, timeout, unit );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks ) throws InterruptedException, ExecutionException {
		return checkRunning().invokeAny( tasks );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		return checkRunning().invokeAny( tasks, timeout, unit );
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return executor.shutdownNow();
	}

	@Override
	public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException {
		return executor.awaitTermination( timeout, unit );
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
	public boolean isShutdown() {
		return executor == null || executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor == null || executor.isTerminated();
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
			if( !(callable instanceof Task ) ) callable = new TaskWrapper<>( callable );
			tasks.add( (Task<T>)callable );
			return ((Task<T>)callable).createFuture( TaskManager.this );
		}

		@Override
		protected void afterExecute( Runnable task, Throwable throwable ) {
			if( task instanceof Task.TaskFuture ) tasks.remove( ((Task.TaskFuture)task).getTask() );

			try {
				((FutureTask)task).get();
			} catch( Throwable getThrowable ) {
				if( !TestUtil.isTest() ) log.error( "Exception executing task", getThrowable );
			}
		}

	}

}
