package com.xeomar.xenon.task;

import com.xeomar.xenon.Program;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskChain<A, R> {

	private Program program;

	private TaskChain<?, A> prior;

	private TaskChain<R, ?> next;

	private Task<R> task;

	public TaskChain( Program program ) {
		this.program = program;
	}

	private <Q> TaskChain( TaskChain<Q, A> prior ) {
		this.prior = prior;
		prior.next = this;
		this.program = prior.program;
	}

	public <Q> TaskChain<R, Q> run( Supplier<R> supplier ) {
		task = new SupplierTask<>( supplier );
		return new TaskChain<>( this );
	}

	public <Q> TaskChain<R, Q> run( Function<A, R> function ) {
		task = new FunctionTask<>( function );
		return new TaskChain<>( this );
	}

	public R submit() throws ExecutionException, InterruptedException {
		TaskChain head = this;
		while( head.prior != null ) head = head.prior;

		return (R)head.crunch();
	}

	private R crunch() throws ExecutionException, InterruptedException {
		return crunch( null );
	}

	private R crunch( A a ) throws ExecutionException, InterruptedException {
		if( task instanceof FunctionTask ) ((FunctionTask)task).setA( a );

		R result = program.getTaskManager().submit( task ).get();

		if( next.task != null ) return (R)next.crunch( result );

		return result;
	}

	private class SupplierTask<H> extends Task<H> {

		private Supplier<H> supplier;

		public SupplierTask( Supplier<H> supplier ) {
			this.supplier = supplier;
		}

		@Override
		public H call() throws Exception {
			return supplier.get();
		}

	}

	private class FunctionTask<A, H> extends Task<H> {

		private A a;

		private Function<A, H> function;

		public FunctionTask( Function<A, H> function ) {
			this.function = function;
		}

		public void setA( A a ) {
			this.a = a;
		}

		@Override
		public H call() throws Exception {
			return function.apply( a );
		}

	}

}
