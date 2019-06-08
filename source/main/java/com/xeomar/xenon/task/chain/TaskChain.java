package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.ThrowingFunction;
import com.xeomar.xenon.task.ThrowingSupplier;
import com.xeomar.xenon.util.Asynchronous;

public class TaskChain<RESULT> {

	private TaskChain<?> first;

	private TaskWrapper<?, RESULT> task;

	private TaskChain<?> next;

	private TaskChain( TaskChain<?> first, TaskWrapper<?, RESULT> task ) {
		this.first = first == null ? this : first;
		task.setLink( this );
		this.task = task;
	}

	public static <R> TaskChain<R> init( Task<R> task ) {
		return init( new TaskWrapper<Void, R>( task ) );
	}

	public static <R> TaskChain<R> init( ThrowingSupplier<R> supplier ) {
		return init( new TaskWrapper<Void, R>( new SupplierTask<>( supplier ) ) );
	}

	public static <P, R> TaskChain<R> init( ThrowingFunction<P, R> function ) {
		return init( new TaskWrapper<P, R>( new FunctionTask<>( function ) ) );
	}

	public static <R> TaskChain<R> init( String name, ThrowingSupplier<R> supplier ) {
		return init( new TaskWrapper<Void, R>( name, new SupplierTask<>( supplier ) ) );
	}

	public static <P, R> TaskChain<R> init( String name, ThrowingFunction<P, R> function ) {
		return init( new TaskWrapper<P, R>( name, new FunctionTask<>( function ) ) );
	}

	public <R> TaskChain<R> link( Task<R> task ) {
		return link( new TaskWrapper<Void, R>( task ) );
	}

	public <R> TaskChain<R> link( ThrowingSupplier<R> supplier ) {
		return link( new TaskWrapper<Void, R>( new SupplierTask<>( supplier ) ) );
	}

	public <R> TaskChain<R> link( ThrowingFunction<RESULT, R> function ) {
		return link( new TaskWrapper<RESULT, R>( new FunctionTask<>( function ) ) );
	}

	public <R> TaskChain<R> link( String name, ThrowingSupplier<R> supplier ) {
		return link( new TaskWrapper<Void, R>( name, new SupplierTask<>( supplier ) ) );
	}

	public <R> TaskChain<R> link( String name, ThrowingFunction<RESULT, R> function ) {
		return link( new TaskWrapper<RESULT, R>( name, new FunctionTask<>( function ) ) );
	}

	@Asynchronous
	public Task<RESULT> run( Program program ) {
		// Start the first task
		submit( program, first.getTask(), null );

		// and return this task
		return task;
	}

	<P, R> void submit( Program program, TaskWrapper<P, R> task, P parameter ) {
		if( task == null ) return;
		task.setProgram( program );
		task.setParameter( parameter );
		program.getTaskManager().submit( task );
	}

	<P, R> void failure( Program program, TaskWrapper<P, R> task, Exception exception ) {
		if( task == null ) return;
		task.setProgram( program );
		task.setPriorException( exception );
		program.getTaskManager().submit( task );
	}

	TaskChain<?> getNext() {
		return next;
	}

	@SuppressWarnings( "unchecked" )
	<P> TaskWrapper<P, RESULT> getTask() {
		return (TaskWrapper<P, RESULT>)this.task;
	}

	private static <P, R> TaskChain<R> init( TaskWrapper<P, R> task ) {
		return new TaskChain<>( null, task );
	}

	private <P, R> TaskChain<R> link( TaskWrapper<P, R> task ) {
		TaskChain<R> link = new TaskChain<>( first, task );
		next = link;
		return link;
	}

}
