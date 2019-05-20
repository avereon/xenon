package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskChain<K> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	// The output of the prior task is the input to the task
	private TaskChain<K> prior;

	// The output of the task is the input to the next task
	private TaskChain<K> next;

	private Task<?> task;

	public TaskChain() {}

	private TaskChain( TaskChain<K> prior ) {
		this.prior = prior;
		prior.next = this;
	}

	public <W> TaskChain<K> add( Supplier<W> supplier ) {
		this.task = new SupplierTask<>( supplier );
		return new TaskChain<>( this );
	}

	public <U, W> TaskChain<K> add( Function<U, W> function ) {
		this.task = new FunctionTask<>( function );
		return new TaskChain<>( this );
	}

	public K submit( Program program ) throws ExecutionException, InterruptedException {
		TaskChain head = this;
		while( head.prior != null ) {
			System.err.println( "<" );
			head = head.prior;
		}
		return (K)head.crunch( program );
	}

	private <R> R crunch( Program program ) throws ExecutionException, InterruptedException {
		return crunch( program, null );
	}

	private <P, R> R crunch( Program program, P parameter ) throws ExecutionException, InterruptedException {
		log.warn( "Crunching: " + task.getClass().getName() );
		if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );
		R result = (R)program.getTaskManager().submit( task ).get();
		return next.task == null ? result : (R)next.crunch( program, result );
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

	private static class FunctionTask<G, H> extends Task<H> {

		private G parameter;

		private Function<? super G, ? extends H> function;

		FunctionTask( Function<? super G, ? extends H> function ) {
			this.function = function;
		}

		public void setParameter( G parameter ) {
			this.parameter = parameter;
		}

		@Override
		public H call() throws Exception {
			return function.apply( parameter );
		}

	}

}
