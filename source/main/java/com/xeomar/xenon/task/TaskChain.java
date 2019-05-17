package com.xeomar.xenon.task;

import com.xeomar.xenon.Program;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskChain<A, R> {

	// The output of the prior task is the input to the task
	private TaskChain< ? super A, R> prior;

	// The output of the task is the input to the next task
	private Task<? extends R> task;

	// The output of the task is the input to the next task
	private TaskChain< ? extends R, ?> next;

	public TaskChain() {}

	private <Q> TaskChain( TaskChain<? extends Q, ? super A> prior ) {
		this.prior = prior;
		prior.next = this;
	}

	public <Q> TaskChain<R, Q> run( Supplier<R> supplier ) {
		task = new SupplierTask<>( supplier );
		return new TaskChain<>( this );
	}

	public <Q> TaskChain<R, ? extends Q> run( Function<? super A, ? extends R> function ) {
		task = new FunctionTask<>( function );
		return new TaskChain<>( this );
	}

	public R submit( Program program ) throws ExecutionException, InterruptedException {
		TaskChain head = this;
		while( head.prior != null ) head = head.prior;

		return (R)head.crunch( program );
	}

	private R crunch( Program program ) throws ExecutionException, InterruptedException {
		return crunch( program, null );
	}

	private R crunch( Program program, A a ) throws ExecutionException, InterruptedException {
		if( task instanceof FunctionTask ) ((FunctionTask<A, ? extends R>)task).setP( a );

		R result = program.getTaskManager().submit( task ).get();

		//if( next.task != null ) return (R)next.crunch( program, result );

		return result;
	}

	private static class SupplierTask<H> extends Task<H> {

		private Supplier<H> supplier;

		SupplierTask( Supplier<H> supplier ) {
			this.supplier = supplier;
		}

		@Override
		public H call() throws Exception {
			return supplier.get();
		}

	}

	private static class FunctionTask<P, H> extends Task<H> {

		private P p;

		private Function<? super P, ? extends H> function;

		FunctionTask( Function<? super P, ? extends H> function ) {
			this.function = function;
		}

		public void setP( P p ) {
			this.p = p;
		}

		@Override
		public H call() throws Exception {
			return function.apply( p );
		}

	}

}
