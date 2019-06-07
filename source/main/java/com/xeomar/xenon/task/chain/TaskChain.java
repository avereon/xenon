package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.ThrowingFunction;
import com.xeomar.xenon.task.ThrowingSupplier;
import com.xeomar.xenon.util.Asynchronous;

// FIXME Can't add task objects
// FIXME Can't chain chains
public class TaskChain<RESULT> {

	private TaskChainContext context;

	private TaskWrapper<?, RESULT> task;

	private TaskChain<?> next;

	private TaskChain( TaskChainContext context, TaskWrapper<?, RESULT> task ) {
		this.context = context;
		task.setLink( this );
		this.task = task;
	}

	// FIXME Can't name tasks
	public static <R> TaskChain<R> init( ThrowingSupplier<R> supplier ) {
		TaskChainContext context = new TaskChainContext();
		TaskChain<R> link = new TaskChain<>( context, new TaskWrapper<Void,R>( new SupplierTask<>( supplier ) ) );
		context.setFirst( link );
		return link;
	}

	// FIXME Can't name tasks
	public static <P, R> TaskChain<R> init( ThrowingFunction<P, R> function ) {
		TaskChainContext context = new TaskChainContext();
		TaskChain<R> link = new TaskChain<>( context, new TaskWrapper<P, R>( new FunctionTask<>( function ) ) );
		context.setFirst( link );
		return link;
	}

	// FIXME Can't name tasks
	public <R> TaskChain<R> link( ThrowingSupplier<R> supplier ) {
		TaskChain<R> link = new TaskChain<>( context, new TaskWrapper<Void,R>( new SupplierTask<>( supplier ) ) );
		next = link;
		return link;
	}

	// FIXME Can't name tasks
	public <R> TaskChain<R> link( ThrowingFunction<RESULT, R> function ) {
		TaskChain<R> link = new TaskChain<>( context, new TaskWrapper<RESULT,R>( new FunctionTask<>( function ) ) );
		next = link;
		return link;
	}

	@Asynchronous
	public Task<RESULT> run( Program program ) {
		// Start the first task
		submit( program, context.getFirst().getTask(), null );

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

	@SuppressWarnings( "unchecked" )
	<P> TaskWrapper<P, RESULT> getTask() {
		return (TaskWrapper<P,RESULT>)this.task;
	}

	TaskChain<?> getNext() {
		return next;
	}

}
