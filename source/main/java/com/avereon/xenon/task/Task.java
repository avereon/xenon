package com.avereon.xenon.task;

import com.avereon.util.LogUtil;
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
		READY,
		SCHEDULED,
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

	private State state = State.READY;

	private String name;

	private Priority priority;

	private Throwable exceptionSource;

	private Set<TaskListener> listeners;

	private TaskManager manager;

	private long total = 1;

	private long progress;

	public Task() {
		this( null );
	}

	public Task( String name ) {
		this( name, Priority.MEDIUM );
	}

	public Task( String name, Priority priority ) {
		this( new TaskCallable<>(), name, priority );
	}

	private Task( TaskCallable<R> taskCallable, String name, Priority priority ) {
		super( taskCallable );
		this.name = name;
		this.priority = priority;
		exceptionSource = new TaskException();
		listeners = new CopyOnWriteArraySet<>();
		taskCallable.setCallable( this );
	}

	@Override
	public void run() {
		setState( Task.State.RUNNING );
		fireTaskEvent( TaskEvent.Type.TASK_START );
		super.run();
	}

	public String getName() {
		return name == null ? getClass().getName() : name;
	}

	protected Task<R> setName( String name ) {
		this.name = name;
		return this;
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

	public long getProgress() {
		return progress;
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
				} catch( InterruptedException | ExecutionException exception ) {
					exception.printStackTrace();
				}
				return null;
			}

		};
	}

	@Override
	protected void done() {
		setProgress( getTotal() );
		fireTaskEvent( TaskEvent.Type.TASK_FINISH );
		if( isCancelled() ) setState( Task.State.CANCELLED );
		setTaskManager( null );
		super.done();
	}

	@Override
	protected void set( R value ) {
		setState( Task.State.SUCCESS );
		super.set( value );
	}

	@Override
	protected void setException( Throwable throwable ) {
		setState( Task.State.FAILED );
		exceptionSource.initCause( throwable );
		super.setException( exceptionSource );
	}

	protected void setTotal( long max ) {
		this.total = max;
	}

	protected void setProgress( long progress ) {
		this.progress = progress;
		fireTaskEvent( TaskEvent.Type.TASK_PROGRESS );
	}

	protected void scheduled() {}

	protected void running() {}

	protected void success() {}

	protected void cancelled() {}

	protected void failed() {}

	void setTaskManager( TaskManager manager ) {
		this.manager = manager;
		if( manager != null ) fireTaskEvent( TaskEvent.Type.TASK_SUBMITTED );
	}

	void setState( State state ) {
		synchronized( stateLock ) {
			this.state = state;
			stateLock.notifyAll();
		}

		switch( state ) {
			case SCHEDULED: {
				scheduled();
				break;
			}
			case RUNNING: {
				running();
				break;
			}
			case SUCCESS: {
				success();
				break;
			}
			case CANCELLED: {
				cancelled();
				break;
			}
			case FAILED: {
				failed();
				break;
			}
		}
	}

	private TaskManager getTaskManager() {
		return manager;
	}

	private void fireTaskEvent( TaskEvent.Type type ) {
		TaskEvent event = new TaskEvent( this, this, type );
		if( getTaskManager() != null ) event.fire( getTaskManager().getTaskListeners() );
		event.fire( listeners );
	}

	private static class TaskCallable<T> implements Callable<T> {

		private Callable<T> callable;

		TaskCallable() {}

		void setCallable( Callable<T> callable ) {
			this.callable = callable;
		}

		@Override
		public T call() throws Exception {
			return callable.call();
		}

	}

}
