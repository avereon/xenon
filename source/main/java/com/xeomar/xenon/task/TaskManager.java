package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.util.Configurable;
import com.xeomar.xenon.util.Controllable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class TaskManager implements ExecutorService, Configurable, Controllable<TaskManager> {

	private static final int MIN_THREAD_COUNT = 4;

	private static final int MAX_THREAD_COUNT = 32;

	private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

	private static final int DEFAULT_MIN_THREAD_COUNT = Math.max( 4, PROCESSOR_COUNT );

	private static final int DEFAULT_MAX_THREAD_COUNT = Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 );

	private static final Logger log = LogUtil.get( TaskManager.class );

	private ThreadPoolExecutor executor;

	private ThreadGroup group;

	private int maxThreadCount = DEFAULT_MAX_THREAD_COUNT;

	private int minThreadCount = DEFAULT_MIN_THREAD_COUNT;

	private Settings settings;

	private BlockingQueue<Runnable> queue;

	private Set<TaskListener> listeners;

	public TaskManager() {
		queue = new LinkedBlockingQueue<Runnable>();
		group = new ThreadGroup( getClass().getName() );
		listeners = new CopyOnWriteArraySet<TaskListener>();
	}

	@Override
	public void execute( Runnable task ) {
		submit( task );
	}

	@Override
	public <T> Future<T> submit( Callable<T> task ) {
		checkRunning();
		return executor.submit( task );
	}

	@Override
	public Future<?> submit( Runnable task ) {
		checkRunning();
		return executor.submit( task );
	}

	@Override
	public <T> Future<T> submit( Runnable task, T result ) {
		checkRunning();
		return executor.submit( task, result );
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
		checkRunning();

//		for( Callable<T> task : tasks ) {
//			if( task instanceof Task ) submitted( (Task)task );
//		}

		return executor.invokeAll( tasks );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException {
		checkRunning();

//		for( Callable<T> task : tasks ) {
//			if( task instanceof Task ) submitted( (Task)task );
//		}

		return executor.invokeAll( tasks, timeout, unit );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks ) throws InterruptedException, ExecutionException {
		checkRunning();

//		for( Callable<T> task : tasks ) {
//			if( task instanceof Task ) submitted( (Task)task );
//		}

		return executor.invokeAny( tasks );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		checkRunning();

//		for( Callable<T> task : tasks ) {
//			if( task instanceof Task ) submitted( (Task)task );
//		}

		return executor.invokeAny( tasks, timeout, unit );
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

	public int getThreadCount() {
		return maxThreadCount;
	}

	public void setThreadCount( int count ) {
		if( count > MAX_THREAD_COUNT ) count = MAX_THREAD_COUNT;
		minThreadCount = Math.max( MIN_THREAD_COUNT, count / 2 );
		maxThreadCount = Math.min( MAX_THREAD_COUNT, Math.max( minThreadCount, count ) );

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
		log.trace( "Task manager thread counts: " + minThreadCount + " min " + maxThreadCount + " max" );
		executor = new TaskManagerExecutor( minThreadCount, maxThreadCount, 1, TimeUnit.SECONDS, queue, new TaskThreadFactory( group ) );
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
				// TODO Make task manager restart timeout a setting?
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
		this.maxThreadCount = settings.getInteger( "thread-count", maxThreadCount );
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

	void fireTaskEvent( TaskEvent event ) {
		for( TaskListener listener : listeners ) {
			listener.handleEvent( event );
		}
	}

	private void checkRunning() {
		if( executor == null ) throw new RuntimeException( "TaskManager is not running." );
	}

	private class TaskManagerExecutor extends ThreadPoolExecutor {

		public TaskManagerExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory ) {
			super( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory );
		}

		@SuppressWarnings( "unchecked" )
		protected <T> RunnableFuture<T> newTaskFor( Callable<T> callable ) {
			RunnableFuture<T> future;
			if( callable instanceof Task ) {
				Task task = (Task)callable;
				task.setTaskManager( TaskManager.this );
				future = task.createFuture( callable );
			} else {
				future = new FutureTask<T>( callable );
			}
			return future;
		}

	}

}
