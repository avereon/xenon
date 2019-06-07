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

	private TaskChain( TaskChainContext context, SupplierTask<RESULT> supplierTask ) {
		this.context = context;
		//supplierTask.setLink( this );
		//task = supplierTask;
		task = new TaskWrapper<Void, RESULT>( supplierTask );
	}

	private <P> TaskChain( TaskChainContext context, FunctionTask<P, RESULT> functionTask ) {
		this.context = context;
		//functionTask.setLink( this );
		//task = functionTask;
		task = new TaskWrapper<P, RESULT>( functionTask );
	}

	// FIXME Can't name tasks
	public static <R> TaskChain<R> init( ThrowingSupplier<R> supplier ) {
		TaskChainContext context = new TaskChainContext();
		TaskChain<R> link = new TaskChain<>( context, new SupplierTask<>( supplier ) );
		context.setFirst( link );
		return link;
	}

	// FIXME Can't name tasks
	public static <P, R> TaskChain<R> init( ThrowingFunction<P, R> function ) {
		TaskChainContext context = new TaskChainContext();
		TaskChain<R> link = new TaskChain<>( context, new FunctionTask<>( function ) );
		context.setFirst( link );
		return link;
	}

	// FIXME Can't name tasks
	public <R> TaskChain<R> link( ThrowingSupplier<R> supplier ) {
		TaskChain<R> link = new TaskChain<>( context, new SupplierTask<>( supplier ) );
		next = link;
		return link;
	}

	// FIXME Can't name tasks
	public <R> TaskChain<R> link( ThrowingFunction<RESULT, R> function ) {
		TaskChain<R> link = new TaskChain<>( context, new FunctionTask<>( function ) );
		next = link;
		return link;
	}

	@Asynchronous
	public Task<RESULT> run( Program program ) {
		// Start the first task
		submit( program, null, context.getFirst().getTask() );

		// and return this task
		return task;
	}

	<P, R> void submit( Program program, P parameter, TaskWrapper<P, R> task ) {
		//if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );
		System.out.println( "TaskChain.submit() before submit()..." );
		task.setProgram( program );
		task.setParameter( parameter );
		program.getTaskManager().submit( task );
	}

	<P> TaskWrapper<P, RESULT> getTask() {
		return (TaskWrapper<P,RESULT>)task;
	}

	TaskChain<?> getNext() {
		return next;
	}

}
