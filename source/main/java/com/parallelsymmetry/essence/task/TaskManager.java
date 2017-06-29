package com.parallelsymmetry.essence.task;

import com.parallelsymmetry.essence.Settings;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

// NEXT Implement TaskManager as an ExecutorService, Configurable and Controllable
public class TaskManager implements ExecutorService {

	private static final int MIN_THREAD_COUNT = 4;

	private static final int MAX_THREAD_COUNT = 32;

	private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

	private static final int DEFAULT_MIN_THREAD_COUNT = Math.max( 4, PROCESSOR_COUNT );

	private static final int DEFAULT_MAX_THREAD_COUNT = Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 );

	private ExecutorService executor;

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
		executor.execute( task );
	}

	@Override
	public <T> Future<T> submit( Callable<T> task ) {
		return executor.submit(task);
	}

	@Override
	public <T> Future<T> submit( Runnable task, T result ) {
		return executor.submit(task,result);
	}

	@Override
	public Future<?> submit( Runnable task ) {
		return executor.submit( task );
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

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
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

}
