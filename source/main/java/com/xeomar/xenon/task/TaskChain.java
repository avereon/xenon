package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskChain<CHAIN_RESULT> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Link first;

	public TaskChain( Program program) {
		this.program = program;
	}

	public <R> Link<R> add( Supplier<R> supplier ) {
		Link<R> link = new Link<>( new SupplierTask<>( supplier ) );
		first = link;
		return link;
	}

	public <P, R> Link<R> add( Function<P, R> function ) {
		Link<R> link = new Link<>( new FunctionTask<>( function ) );
		first = link;
		return link;
	}

	@SuppressWarnings( "unchecked" )
	public CHAIN_RESULT submit() throws ExecutionException, InterruptedException {
		return (CHAIN_RESULT)crunch();
	}

	private Object crunch() throws ExecutionException, InterruptedException {
		Link<?> link = first;

		Object parameter = null;
		while( link != null ) {
			parameter = crunch( parameter, link.task );
			link = link.getNext();
		}

		return parameter;
	}

	private <P, R> R crunch( P parameter, Task<R> task ) throws ExecutionException, InterruptedException {
		if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );
		return program.getTaskManager().submit( task ).get();
	}

	public class Link<LINK_RESULT> {

		private Task<LINK_RESULT> task;

		private Link<?> next;

		Link( SupplierTask<LINK_RESULT> supplierTask ) {
			task = supplierTask;
		}

		<P> Link( FunctionTask<P, LINK_RESULT> functionTask ) {
			task = functionTask;
		}

		public Task<LINK_RESULT> getTask() {
			return task;
		}

		public Link getNext() {
			return next;
		}

		public <R> Link<R> add( Supplier<R> supplier ) {
			Link<R> link = new Link<>( new SupplierTask<>( supplier ) );
			next = link;
			return link;
		}

		public <R> Link<R> add( Function<LINK_RESULT, R> function ) {
			Link<R> link = new Link<>( new FunctionTask<>( function ) );
			next = link;
			return link;
		}

		public CHAIN_RESULT last( Supplier<CHAIN_RESULT> supplier ) throws ExecutionException, InterruptedException {
			next = new Link<>( new SupplierTask<>( supplier ) );
			return TaskChain.this.submit();
		}

		public CHAIN_RESULT last( Function<LINK_RESULT, CHAIN_RESULT> function ) throws ExecutionException, InterruptedException {
			next = new Link<>( new FunctionTask<>( function ) );
			return TaskChain.this.submit();
		}

	}

	private static class SupplierTask<R> extends Task<R> {

		private Supplier<R> supplier;

		SupplierTask( Supplier<R> supplier ) {
			this.supplier = supplier;
		}

		@Override
		public R call() {
			return supplier.get();
		}

	}

	private static class FunctionTask<P, R> extends Task<R> {

		private P parameter;

		private Function<? super P, ? extends R> function;

		FunctionTask( Function<? super P, ? extends R> function ) {
			this.function = function;
		}

		void setParameter( P parameter ) {
			this.parameter = parameter;
		}

		@Override
		public R call() {
			return function.apply( parameter );
		}

	}

}
