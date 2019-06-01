package com.xeomar.xenon.task;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.util.Asynchronous;
import com.xeomar.xenon.util.Synchronous;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;

public class TaskChain<CHAIN_RESULT> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Link<?> first;

	private Link<CHAIN_RESULT> last;

	public TaskChain( Program program ) {
		this.program = program;
	}

	public <R> Link<R> add( ThrowingSupplier<R> supplier ) {
		Link<R> link = new Link<>( new SupplierTask<>( supplier ) );
		first = link;
		return link;
	}

	public <P, R> Link<R> add( ThrowingFunction<P, R> function ) {
		Link<R> link = new Link<>( new FunctionTask<>( function ) );
		first = link;
		return link;
	}

	private <P, R> void submit( P parameter, Task<R> task ) {
		if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );
		program.getTaskManager().submit( task );
	}

	private synchronized CHAIN_RESULT get() throws ExecutionException, InterruptedException {
		while( last == null ) {
			wait(1000);
		}
		return last.task.get();
	}

	@SuppressWarnings( "unchecked" )
	private synchronized void setLast( Link<?> last ) {
		this.last = (Link<CHAIN_RESULT>)last;
		this.notifyAll();
	}

	public class Link<LINK_RESULT> {

		private Task<LINK_RESULT> task;

		private Link<?> next;

		Link( SupplierTask<LINK_RESULT> supplierTask ) {
			supplierTask.link = this;
			task = supplierTask;
		}

		<P> Link( FunctionTask<P, LINK_RESULT> functionTask ) {
			functionTask.link = this;
			task = functionTask;
		}

		public Task<LINK_RESULT> getTask() {
			return task;
		}

		public Link getNext() {
			return next;
		}

		public <R> Link<R> add( ThrowingSupplier<R> supplier ) {
			Link<R> link = new Link<>( new SupplierTask<>( supplier ) );
			next = link;
			return link;
		}

		public <R> Link<R> add( ThrowingFunction<LINK_RESULT, R> function ) {
			Link<R> link = new Link<>( new FunctionTask<>( function ) );
			next = link;
			return link;
		}

		@Asynchronous
		public void run() {
			TaskChain.this.submit( null, first.task );
		}

		@Synchronous
		public CHAIN_RESULT get() throws ExecutionException, InterruptedException {
			run();
			return TaskChain.this.get();
		}

	}

	private class SupplierTask<R> extends Task<R> {

		private ThrowingSupplier<R> supplier;

		private Link<R> link;

		SupplierTask( ThrowingSupplier<R> supplier ) {
			this.supplier = supplier;
		}

		@Override
		public R call() throws Exception {
			R result = supplier.get();
			if( link.next != null ) TaskChain.this.submit( result, link.next.task );
			if( link.next == null ) TaskChain.this.setLast( link );
			return result;
		}

	}

	private class FunctionTask<P, R> extends Task<R> {

		private P parameter;

		private ThrowingFunction<? super P, ? extends R> function;

		private Link<?> link;

		FunctionTask( ThrowingFunction<? super P, ? extends R> function ) {
			this.function = function;
		}

		void setParameter( P parameter ) {
			this.parameter = parameter;
		}

		@Override
		public R call() throws Exception {
			R result = function.apply( parameter );
			if( link.next != null ) TaskChain.this.submit( result, link.next.task );
			if( link.next == null ) TaskChain.this.setLast( link );
			return result;
		}

	}

}
