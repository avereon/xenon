package com.xeomar.xenon.task;

import java.util.concurrent.FutureTask;

public class TaskFuture<R> extends FutureTask<R> {

	private Task<R> task;

	private Throwable exceptionSource;

	TaskFuture( Task<R> task ) {
		super( task );
		this.task = task;
		this.exceptionSource = new TaskException();
	}

	public Task<R> getTask() {
		return task;
	}

	@Override
	public void run() {
		task.setState( Task.State.RUNNING );
		task.fireTaskEvent( TaskEvent.Type.TASK_START );
		super.run();
	}

	@Override
	protected void done() {
		task.setProgress( task.getTotal() );
		task.fireTaskEvent( TaskEvent.Type.TASK_FINISH );
		if( isCancelled() ) task.setState( Task.State.CANCELLED );
		task.setTaskManager( null );
		super.done();
	}

	@Override
	protected void set( R value ) {
		task.setState( Task.State.SUCCESS );
		super.set( value );
	}

	@Override
	protected void setException( Throwable throwable ) {
		task.setState( Task.State.FAILED );
		exceptionSource.initCause( throwable );
		super.setException( exceptionSource );
	}

}
