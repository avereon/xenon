package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.ThrowingFunction;
import com.xeomar.xenon.task.ThrowingSupplier;
import com.xeomar.xenon.util.Asynchronous;

public class TaskChain<RESULT> {

	private TaskChainContext context;

	private AbstractFunctionalTask<RESULT> task;

	private TaskChain<?> next;

	private TaskChain( TaskChainContext context, SupplierTask<RESULT> supplierTask ) {
		this.context = context;
		supplierTask.setLink( this );
		task = supplierTask;
	}

	private <P> TaskChain( TaskChainContext context, FunctionTask<P, RESULT> functionTask ) {
		this.context = context;
		functionTask.setLink( this );
		task = functionTask;
	}

	public static <R> TaskChain<R> init( ThrowingSupplier<R> supplier ) {
		TaskChainContext context = new TaskChainContext();
		TaskChain<R> link = new TaskChain<>( context, new SupplierTask<>( context, supplier ) );
		context.init( link );

		return link;
	}

	public static <P, R> TaskChain<R> init( ThrowingFunction<P, R> function ) {
		TaskChainContext context = new TaskChainContext();
		TaskChain<R> link = new TaskChain<>( context, new FunctionTask<>( context, function ) );
		context.init( link );
		return link;
	}

	public <R> TaskChain<R> link( ThrowingSupplier<R> supplier ) {
		TaskChain<R> link = new TaskChain<>( context, new SupplierTask<>( context, supplier ) );
		next = link;
		return link;
	}

	public <R> TaskChain<R> link( ThrowingFunction<RESULT, R> function ) {
		TaskChain<R> link = new TaskChain<>( context, new FunctionTask<>( context, function ) );
		next = link;
		return link;
	}

	@Asynchronous
	public Task<RESULT> run( Program program ) {
		// Start the first task
		context.submit( program, null, context.getFirstLink().getTask() );

		// and return this task
		return task;
	}

	AbstractFunctionalTask<RESULT> getTask() {
		return task;
	}

	TaskChain<?> getNext() {
		return next;
	}

}
