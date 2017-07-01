package com.parallelsymmetry.essence.task;

import com.parallelsymmetry.essence.LogUtil;
import com.parallelsymmetry.essence.settings.Settings;
import com.parallelsymmetry.essence.util.Configurable;
import com.parallelsymmetry.essence.util.Controllable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager implements ExecutorService, Configurable, Controllable<TaskManager> {

	private static final int MIN_THREAD_COUNT = 4;

	private static final int MAX_THREAD_COUNT = 32;

	private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

	private static final int DEFAULT_MIN_THREAD_COUNT = Math.max( 4, PROCESSOR_COUNT );

	private static final int DEFAULT_MAX_THREAD_COUNT = Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 );

	private static final Logger log = LogUtil.get( TaskManager.class );

	private ThreadPoolExecutor executor;

	private int maxThreadCount = DEFAULT_MAX_THREAD_COUNT;

	private int minThreadCount = DEFAULT_MIN_THREAD_COUNT;

	private Settings settings;

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
		submit( task );
	}

	@Override
	public <T> Future<T> submit( Callable<T> task ) {
		checkRunning();
		if( task instanceof Task ) submitted( (Task)task );
		return executor.submit( task );
	}

	@Override
	public <T> Future<T> submit( Runnable task, T result ) {
		checkRunning();
		if( task instanceof Task ) submitted( (Task)task );
		return executor.submit( task, result );
	}

	@Override
	public Future<?> submit( Runnable task ) {
		checkRunning();
		if( task instanceof Task ) submitted( (Task)task );
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
		checkRunning();

		for( Callable<T> task : tasks ) {
			if( task instanceof Task ) submitted( (Task)task );
		}

		return executor.invokeAll( tasks );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException {
		checkRunning();

		for( Callable<T> task : tasks ) {
			if( task instanceof Task ) submitted( (Task)task );
		}

		return executor.invokeAll( tasks, timeout, unit );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks ) throws InterruptedException, ExecutionException {
		checkRunning();

		for( Callable<T> task : tasks ) {
			if( task instanceof Task ) submitted( (Task)task );
		}

		return invokeAny( tasks );
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		checkRunning();

		for( Callable<T> task : tasks ) {
			if( task instanceof Task ) submitted( (Task)task );
		}

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
		executor = new ThreadPoolExecutor( minThreadCount, maxThreadCount, 1, TimeUnit.SECONDS, queue, new TaskThreadFactory() );
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
		return null;
	}

	@Override
	public TaskManager stop() {
		if( executor == null || executor.isShutdown() ) return this;
		executor.shutdown();
		return this;
	}

	public TaskManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		if( executor == null || executor.isShutdown() ) return this;
		executor.awaitTermination( timeout, unit );
		return this;
	}

	@Override
	public void loadSettings( Settings settings ) {
		this.settings = settings;
		this.maxThreadCount = settings.getInteger( "thread-count", maxThreadCount );
	}

	@Override
	public void saveSettings( Settings settings ) {
		if( this.settings == null ) return;
		this.settings.set( "thread-count", maxThreadCount );
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

	private void checkRunning() {
		if( executor == null ) throw new RuntimeException( "TaskManager is not running." );
	}

	private <T> void synchronousExecute( Task<T> task ) {
		try {
			task.invoke();
		} catch( Exception exception ) {
			// Exceptions should be retrieved by calling get().
		}
	}

	private <T> void synchronousExecute( Task<T> task, long timeout, TimeUnit unit ) {
		try {
			task.invoke( timeout, unit );
		} catch( Exception exception ) {
			// Exceptions should be retrieved by calling get().
		}
	}

	private <T> void synchronousExecute( Collection<? extends Task<T>> tasks ) {
		for( Task<T> task : tasks ) {
			synchronousExecute( task );
		}
	}

	private <T> void synchronousExecute( Collection<? extends Task<T>> tasks, long timeout, TimeUnit unit ) {
		for( Task<T> task : tasks ) {
			synchronousExecute( task, timeout, unit );
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
