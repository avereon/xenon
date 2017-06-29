package com.parallelsymmetry.essence.task;

import java.util.Set;
import java.util.concurrent.*;

/**
 * An executable task.
 * <p>
 * WARNING! Do not create a waitFor() method to wait for the task to complete.
 * The correct way to wait for the result is to obtain the Future object when
 * calling the submit( Task ) method and then call future.get().
 *
 * @author Mark Soderquist
 * @param <V> The return type of the task.
 */

public abstract class Task<V> implements Callable<V>, Future<V> {

	public enum State {
		WAITING, RUNNING, DONE;
	}

	public enum Result {
		UNKNOWN, CANCELLED, SUCCESS, FAILED;
	}

	public enum Priority {
		LOW, MEDIUM, HIGH, UI
	}

	private Object stateLock = new Object();

	private State state = State.WAITING;

	private Result result = Result.UNKNOWN;

	private String name;

	private FutureTask<V> future;

	private TaskManager manager;

	private Set<TaskListener> listeners;

	private Priority priority;

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
		future = new TaskFuture<V>( this, new TaskExecute<V>( this ) );
		listeners = new CopyOnWriteArraySet<TaskListener>();
	}

	public abstract V execute() throws Exception;

	@Override
	public V call() throws Exception {
		return invoke();
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
		waitForState( State.DONE );
		return future.get();
	}

	@Override
	public V get( long duration, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		waitForState( State.DONE, duration, unit );
		return future.get( duration, unit );
	}

	public String getName() {
		return name == null ? getClass().getName() : name;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority( Priority priority ) {
		this.priority = priority;
	}

	public State getState() {
		return state;
	}

	public void waitForState( State state ) throws InterruptedException {
		synchronized( stateLock ) {
			while( this.state != state ) {
				stateLock.wait();
			}
		}
	}

	public void waitForState( State state, long duration, TimeUnit unit ) throws InterruptedException {
		synchronized( stateLock ) {
			while( this.state != state ) {
				stateLock.wait( unit.toMillis( duration ), (int)( unit.toNanos( duration ) % 1000000 ) );
			}
		}
	}

	public Result getResult() {
		return result;
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

	protected TaskManager getTaskManager() {
		return manager;
	}

	protected void setTaskManager( TaskManager manager ) {
		this.manager = manager;
	}

	protected void fireTaskEvent( TaskEvent.Type type ) {
		TaskEvent event = new TaskEvent( this, this, type );
		for( TaskListener listener : listeners ) {
			listener.handleEvent( event );
		}
		TaskManager manager = this.manager;
		if( manager != null ) manager.fireTaskEvent( event );
	}

	V invoke() throws InterruptedException, ExecutionException {
		setState( State.RUNNING );
		fireTaskEvent( TaskEvent.Type.TASK_START );

		future.run();
		return future.get();
	}

	V invoke( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		setState( State.RUNNING );
		fireTaskEvent( TaskEvent.Type.TASK_START );

		future.run();
		return future.get( timeout, unit );
	}

	private void setState( State state ) {
		synchronized( stateLock ) {
			this.state = state;
			stateLock.notifyAll();
		}
	}

	private static class TaskFuture<W> extends FutureTask<W> {

		private Task<?> task;

		public TaskFuture( Task<?> task, Callable<W> callable ) {
			super( callable );
			this.task = task;
		}

		// This is a workaround in Java 6 to capture the result of the task.
		@Override
		protected void done() {
			try {
				task.future.get();
				task.result = Result.SUCCESS;
			} catch( InterruptedException exception ) {
				// Intentionally ignore exception.
			} catch( CancellationException exception ) {
				task.result = Result.CANCELLED;
			} catch( ExecutionException exception ) {
				task.result = Result.FAILED;
			} finally {
				task.setState( State.DONE );
				task.setProgress( task.maximum );
				task.fireTaskEvent( TaskEvent.Type.TASK_FINISH );
				task.manager.completed( task );
				task.manager = null;
			}

			super.done();
		}

		// TODO The following methods work in Java 7 but not in Java 6.
		//		@Override
		//		protected void done() {
		//			task.setState( State.DONE );
		//			task.fireTaskEvent( TaskEvent.Type.TASK_FINISH );
		//			super.done();
		//		}
		//
		//		@Override
		//		protected void set( W value ) {
		//			task.result = Result.SUCCESS;
		//			super.set( value );
		//		}
		//
		//		@Override
		//		protected void setException( Throwable throwable ) {
		//			task.result = Result.FAILED;
		//			super.setException( throwable );
		//		}

	}

	private static class TaskExecute<W> implements Callable<W> {

		private Task<W> task;

		public TaskExecute( Task<W> task ) {
			this.task = task;
		}

		@Override
		public W call() throws Exception {
			// TODO This may not be the right place, but implement preemptive priority execution.
			return task.execute();
		}

	}

}
