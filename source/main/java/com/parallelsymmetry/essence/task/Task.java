package com.parallelsymmetry.essence.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

/**
 * An executable task.
 * <p>
 * WARNING! Do not create a waitFor() method to wait for the task to complete.
 * The correct way to wait for the result is to obtain the Future object when
 * calling the submit( Task ) method and then call future.get().
 *
 * @param <V> The return type of the task.
 * @author Mark Soderquist
 */

public abstract class Task<V> implements Callable<V>, Future<V> {

	private static final Logger log = LoggerFactory.getLogger( Task.class );

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

	private Priority priority = Priority.MEDIUM;

	private String name;

	private TaskFuture<V> future;

	private TaskManager manager;

	private Set<TaskListener> listeners;

	private long minimum = 0;

	private long maximum = 1;

	private long progress;

	public Task() {
		this( null );
	}

	public Task( String name ) {
		this( name, Priority.MEDIUM );
	}

	public Task( String name, Priority priority ) {
		this.name = name;
		this.priority = priority;
		listeners = new CopyOnWriteArraySet<TaskListener>();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean cancel( boolean mayInterruptIfRunning ) {
		return future.cancel( mayInterruptIfRunning );
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public V get( long duration, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get( duration, unit );
	}

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

	public long getMinimum() {
		return minimum;
	}

	public void setMinimum( long min ) {
		this.minimum = min;
	}

	public long getMaximum() {
		return maximum;
	}

	public void setMaximum( long max ) {
		this.maximum = max;
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

	TaskManager getTaskManager() {
		return manager;
	}

	void setTaskManager( TaskManager manager ) {
		this.manager = manager;
		if( manager != null ) fireTaskEvent( TaskEvent.Type.TASK_SUBMITTED );
	}

	FutureTask<V> createFuture( Callable<V> callable ) {
		return this.future = new TaskFuture<V>( this );
	}

	private void fireTaskEvent( TaskEvent.Type type ) {
		TaskEvent event = new TaskEvent( this, this, type );
		for( TaskListener listener : listeners ) {
			listener.handleEvent( event );
		}
		getTaskManager().fireTaskEvent( event );
	}

	private void setState( State state ) {
		synchronized( stateLock ) {
			this.state = state;
			stateLock.notifyAll();
		}
	}

	private static class TaskFuture<W> extends FutureTask<W> {

		private Task<?> task;

		private TaskFuture( Task<W> task ) {
			super( task );
			this.task = task;
		}

		@Override
		public void run() {
			task.setState( State.RUNNING );
			task.fireTaskEvent( TaskEvent.Type.TASK_START );
			super.run();
		}

		@Override
		protected void done() {
			task.setProgress( task.getMaximum() );
			task.fireTaskEvent( TaskEvent.Type.TASK_FINISH );
			if( isCancelled() ) task.setState( State.CANCELLED );
			task.setTaskManager( null );
			super.done();
		}

		@Override
		protected void set( W value ) {
			task.setState( State.SUCCESS );
			super.set( value );
		}

		@Override
		protected void setException( Throwable throwable ) {
			task.setState( State.FAILED );
			super.setException( throwable );
			log.error( "Error running task", throwable );
		}

	}

}
