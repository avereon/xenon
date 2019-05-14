package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.concurrent.*;

/**
 * An executable task.
 * <p>
 * WARNING! Do not create a waitFor() method to wait for the task to complete.
 * The correct way to wait for the result is to obtain the Future object when
 * calling the submit( Task ) method and then call future.get().
 *
 * @param <R> The return type of the task.
 * @author Mark Soderquist
 */

public abstract class Task<R> extends FutureTask<R> implements Callable<R> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public enum State {
		WAITING,
		RUNNING,
		CANCELLED,
		SUCCESS,
		FAILED
	}

	public enum Priority {
		LOW,
		MEDIUM,
		HIGH
	}

	private final Object stateLock = new Object();

	private State state = State.WAITING;

	private Priority priority;

	private String name;

	private TaskFuture<R> future;

	private TaskManager manager;

	private Set<TaskListener> listeners;

	private long total = 1;

	private long progress;

	public Task() {
		this( null );
	}

	public Task( String name ) {
		this(  name, Priority.MEDIUM );
	}

	public Task( String name, Priority priority ) {
		this( new Passthrough<>(), name, priority );
	}

	private Task( Passthrough<R> passthrough, String name, Priority priority ) {
		super( passthrough );
		this.name = name;
		this.priority = priority;
		passthrough.setCallable( this );
		listeners = new CopyOnWriteArraySet<>();
	}

//	@Override
//	public boolean isDone() {
//		return future != null && future.isDone();
//	}
//
//	@Override
//	public boolean isCancelled() {
//		return future != null && future.isCancelled();
//	}
//
//	@Override
//	public boolean cancel( boolean mayInterruptIfRunning ) {
//		return future != null && future.cancel( mayInterruptIfRunning );
//	}
//
//	@Override
//	public R get() throws InterruptedException, ExecutionException {
//		return future.get();
//	}
//
//	@Override
//	public R get( long duration, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
//		return future.get( duration, unit );
//	}

	public String getName() {
		return name == null ? getClass().getName() : name;
	}

	public State getState() {
		return state;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority( Priority priority ) {
		this.priority = priority;
	}

	public double getPercent() {
		return Math.min( 1.0, (double)progress / (double)total );
	}

	public long getTotal() {
		return total;
	}

	public void setTotal( long max ) {
		this.total = max;
	}

	public long getProgress() {
		return progress;
	}

	public void setProgress( long progress ) {
		this.progress = progress;
		fireTaskEvent( TaskEvent.Type.TASK_PROGRESS );
	}

	public void addTaskListener( TaskListener listener ) {
		listeners.add( listener );
	}

	public void removeTaskListener( TaskListener listener ) {
		listeners.remove( listener );
	}

	public static <N> Task<N> of( String name, Callable<N> callable ) {
		if( callable == null ) throw new NullPointerException( "Callable cannot be null" );
		return new Task<>( name ) {

			@Override
			public N call() throws Exception {
				return callable.call();
			}

		};
	}

	public static Task<?> of( String name, Runnable runnable ) {
		if( runnable == null ) throw new NullPointerException( "Runnable cannot be null" );

		return new Task( name ) {

			@Override
			public Void call() {
				runnable.run();
				return null;
			}

		};
	}

	public static <N> Task<N> of( FutureTask<N> futureTask ) {
		if( futureTask == null ) throw new NullPointerException( "FutureTask cannot be null" );

		return new Task<>( "" ) {

			@Override
			public N call() {
				futureTask.run();
				try {
					return futureTask.get();
				} catch( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
				return null;
			}

		};
	}

	private TaskManager getTaskManager() {
		return manager;
	}

	void setTaskManager( TaskManager manager ) {
		this.manager = manager;
		if( manager != null ) fireTaskEvent( TaskEvent.Type.TASK_SUBMITTED );
	}

	FutureTask<R> createFuture() {
		return this.future = new TaskFuture<>( this );
	}

	void fireTaskEvent( TaskEvent.Type type ) {
		TaskEvent event = new TaskEvent( this, this, type );
		event.fire( getTaskManager().getTaskListeners() );
		event.fire( listeners );
	}

	void setState( State state ) {
		synchronized( stateLock ) {
			this.state = state;
			stateLock.notifyAll();
		}
	}

	private static class Passthrough<T> implements Callable<T> {

		private Callable<T> callable;

		public Passthrough() {}

		public void setCallable( Callable<T> callable ) {
			this.callable = callable;
		}

		@Override
		public T call() throws Exception {
			return callable.call();
		}

	}

}
