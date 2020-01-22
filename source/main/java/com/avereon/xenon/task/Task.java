package com.avereon.xenon.task;

import com.avereon.util.LogUtil;
import com.avereon.xenon.util.ProgramEventBus;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

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

	public static final long INDETERMINATE_PROGRESS = -1;

	private final Object stateLock = new Object();

	private State state = State.READY;

	private String name;

	private Priority priority;

	private Throwable exceptionSource;

	private Throwable throwable;

	private ProgramEventBus eventBus;

	private TaskManager manager;

	private long total = INDETERMINATE_PROGRESS;

	private long progress;

	private Priority processedPriority;

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
		exceptionSource = new TaskSourceWrapper();
		eventBus = new ProgramEventBus();
		taskCallable.setCallable( this );
	}

	@Override
	public void run() {
		setState( Task.State.RUNNING );
		eventBus.dispatch( new TaskEvent( this, TaskEvent.START, this ) );
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

	public Task<R> setPriority( Priority priority ) {
		this.priority = priority;
		return this;
	}

	public double getPercent() {
		if( total == INDETERMINATE_PROGRESS ) return INDETERMINATE_PROGRESS;
		return Math.max( 0.0, Math.min( 1.0, (double)progress / (double)total ) );
	}

	public Priority getProcessedPriority() {
		return processedPriority;
	}

	void setProcessedPriority( Priority processedPriority ) {
		this.processedPriority = processedPriority;
	}

	public long getTotal() {
		return total;
	}

	public long getProgress() {
		return progress;
	}

	public ProgramEventBus getEventBus() {
		return eventBus;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + getName();
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

		return new Task<Void>( name ) {

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
			public N call() throws Exception {
				futureTask.run();
				return futureTask.get();
			}

		};
	}

	@Override
	protected void done() {
		setProgress( getTotal() );
		if( isCancelled() ) setState( Task.State.CANCELLED );

		switch( getState() ) {
			case SUCCESS: {
				eventBus.dispatch( new TaskEvent( this, TaskEvent.SUCCESS, this ) );
				break;
			}
			case CANCELLED: {
				eventBus.dispatch( new TaskEvent( this, TaskEvent.CANCEL, this ) );
				break;
			}
			case FAILED: {
				eventBus.dispatch( new TaskEvent( this, TaskEvent.FAILURE, this ) );
				break;
			}
		}
		eventBus.dispatch( new TaskEvent( this, TaskEvent.FINISH, this ) );

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
		exceptionSource.initCause( throwable );
		this.throwable = exceptionSource;

		// Internal state must be set before calling setState()
		setState( Task.State.FAILED );

		// Internal setState() must be called before super.setException()
		// because super.setException() notifies threads waiting on get()
		super.setException( exceptionSource );
	}

	protected Throwable getException() {
		return throwable;
	}

	protected void setTotal( long total ) {
		this.total = total;
	}

	protected void setProgress( long progress ) {
		this.progress = progress;
		eventBus.dispatch( new TaskEvent( this, TaskEvent.PROGRESS, this ) );
	}

	protected void scheduled() {}

	protected void running() {}

	protected void success() {}

	protected void cancelled() {}

	protected void failed() {
		log.error( "Task failed", getException() );
	}

	void setTaskManager( TaskManager manager ) {
		this.manager = manager;
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
