package com.parallelsymmetry.essence.task;

import com.parallelsymmetry.essence.util.Configurable;
import com.parallelsymmetry.essence.util.ControllableException;
import com.parallelsymmetry.essence.util.ControllableExtended;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager implements ExecutorService, ControllableExtended, Configurable {

	private static final int MIN_THREAD_COUNT = 4;

	private static final int MAX_THREAD_COUNT = 32;

	private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

	private static final int DEFAULT_MIN_THREAD_COUNT = Math.max( 4, PROCESSOR_COUNT );

	private static final int DEFAULT_MAX_THREAD_COUNT = Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 );

	private static final Logger log = LoggerFactory.getLogger( TaskManager.class );

	private ThreadPoolExecutor executor;

	private int maxThreadCount = DEFAULT_MAX_THREAD_COUNT;

	private int minThreadCount = DEFAULT_MIN_THREAD_COUNT;

	private Configuration settings;

	private List<Task<?>> tasks;

	private BlockingQueue<Runnable> queue;

	private Set<TaskListener> listeners;

	public TaskManager() {
		tasks = new CopyOnWriteArrayList<Task<?>>();
		queue = new LinkedBlockingQueue<Runnable>();
		listeners = new CopyOnWriteArraySet<TaskListener>();
	}

	@Override
	public void execute( Runnable task ) {
		executor.execute( task );
	}

	@Override
	public <T> Future<T> submit( Callable<T> task ) {
		return executor.submit( task );
	}

	@Override
	public <T> Future<T> submit( Runnable task, T result ) {
		return executor.submit( task, result );
	}

	@Override
	public Future<?> submit( Runnable task ) {
		return executor.submit( task );
	}

	/**
	 * Synchronously submit a task.
	 *
	 * @param <T>
	 * @param task
	 * @return
	 * @throws InterruptedException
	 */
	public <T> Future<T> invoke( Task<T> task ) throws InterruptedException {
		List<Task<T>> tasks = new ArrayList<>();
		tasks.add( task );
		List<Future<T>> futures = invokeAll( tasks );
		return futures.get( 0 );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException {
		return executor.invokeAll( tasks );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException {
		return executor.invokeAll( tasks, timeout, unit );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks ) throws InterruptedException, ExecutionException {
		return invokeAny( tasks );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
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
		return executor == null ? 0 : executor.getPoolSize();
	}

	public void setThreadCount( int count ) {
		if( count > MAX_THREAD_COUNT ) count = MAX_THREAD_COUNT;
		minThreadCount = Math.max( MIN_THREAD_COUNT, count / 2 );
		maxThreadCount = Math.min( MAX_THREAD_COUNT, Math.max( minThreadCount, count ) );

		saveSettings( settings );

		if( isRunning() ) {
			try {
				restart( 10, TimeUnit.SECONDS );
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
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Override
	public void start() throws ControllableException {
		if( isRunning() ) return;
		log.trace( "Task manager thread counts: " + minThreadCount + " min " + maxThreadCount + " max" );
		executor = new ThreadPoolExecutor( minThreadCount, maxThreadCount, 1, TimeUnit.SECONDS, queue, new TaskThreadFactory() );
	}

	@Override
	public void startAndWait( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException {
		start();
	}

	@Override
	public void restart( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException {
		// Don't use start() and stop() because they are asynchronous.
		stopAndWait( timeout / 2, unit );
		startAndWait( timeout / 2, unit );
	}

	@Override
	public void stop() throws ControllableException {
		if( executor == null || executor.isShutdown() ) return;
		executor.shutdown();
		executor = null;
	}

	@Override
	public void stopAndWait( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException {
		if( executor == null || executor.isShutdown() ) return;
		executor.shutdown();
		executor.awaitTermination( timeout, unit );
		executor = null;
	}

	@Override
	public void loadSettings( Configuration settings ) {
		this.settings = settings;
		this.maxThreadCount = settings.getInt( "thread-count", maxThreadCount );
	}

	@Override
	public void saveSettings( Configuration settings ) {
		if( this.settings == null ) return;
		this.settings.setProperty( "thread-count", maxThreadCount );
	}

	public void addTaskListener( TaskListener listener ) {
		listeners.add( listener );
	}

	public void removeTaskListener( TaskListener listener ) {
		listeners.remove( listener );
	}

	void submitted( Task<?> task ) {
		if( task == null ) throw new NullPointerException();
		tasks.add( task );
		task.setTaskManager( this );
		fireTaskEvent( new TaskEvent( this, task, TaskEvent.Type.TASK_SUBMITTED ) );
	}

	void completed( Task<?> task ) {
		if( task == null ) throw new NullPointerException();
		fireTaskEvent( new TaskEvent( this, task, TaskEvent.Type.TASK_COMPLETED ) );
		task.setTaskManager( null );
		tasks.remove( task );
	}

	void fireTaskEvent( TaskEvent event ) {
		for( TaskListener listener : listeners ) {
			listener.handleEvent( event );
		}
	}

	private static final class TaskThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

		private final AtomicInteger threadNumber = new AtomicInteger( 1 );

		private final ThreadGroup group;

		private final String prefix;

		public TaskThreadFactory() {
			SecurityManager securityManager = System.getSecurityManager();
			group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
			prefix = "TaskPool-" + poolNumber.getAndIncrement() + "-Thread-";
		}

		@Override
		public Thread newThread( Runnable runnable ) {
			Thread thread = new Thread( group, runnable, prefix + threadNumber.getAndIncrement(), 0 );
			if( thread.getPriority() != Thread.NORM_PRIORITY ) thread.setPriority( Thread.NORM_PRIORITY );
			if( !thread.isDaemon() ) thread.setDaemon( true );
			return thread;
		}

	}

}
