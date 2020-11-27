package com.avereon.xenon.task;

import com.avereon.xenon.Program;
import com.avereon.xenon.util.Asynchronous;

public class TaskChain<RESULT> {

	private final Link<?, ?> first;

	private final Link<?, RESULT> task;

	private TaskChain( Link<?, ?> first, Link<?, RESULT> task ) {
		this.first = first == null ? task : first;
		this.task = task;
	}

	public static <R> TaskChain<R> of( TaskChainSupplier<R> supplier ) {
		return of( new SupplierTask<>( supplier ) );
	}

	public static <P, R> TaskChain<R> of( TaskChainFunction<P, R> function ) {
		return of( new FunctionTask<>( function ) );
	}

	public static <R> TaskChain<R> of( String name, TaskChainSupplier<R> supplier ) {
		return of( new SupplierTask<>( name, supplier ) );
	}

	public static <P, R> TaskChain<R> of( String name, TaskChainFunction<P, R> function ) {
		return of( new FunctionTask<>( name, function ) );
	}

	public static <R> TaskChain<R> of( Task<R> task ) {
		return ofTask( task );
	}

	public <R> TaskChain<R> link( TaskChainSupplier<R> supplier ) {
		return link( new SupplierTask<>( supplier ) );
	}

	public <R> TaskChain<R> link( TaskChainFunction<RESULT, R> function ) {
		return link( new FunctionTask<>( function ) );
	}

	public <R> TaskChain<R> link( String name, TaskChainSupplier<R> supplier ) {
		return link( new SupplierTask<>( name, supplier ) );
	}

	public <R> TaskChain<R> link( String name, TaskChainFunction<RESULT, R> function ) {
		return link( new FunctionTask<>( name, function ) );
	}

	public <R> TaskChain<R> link( Task<R> task ) {
		return linkTask( task );
	}

	public Task<RESULT> build() {
		return task;
	}

	@Asynchronous
	public Task<RESULT> run( Program program ) {
		// Start the first task
		program.getTaskManager().submit( first );

		// and return the last task
		return task;
	}

	private static <R> TaskChain<R> ofTask( Task<R> task ) {
		return new TaskChain<>( null, new Link<>( task ) );
	}

	private <P, R> TaskChain<R> linkTask( Task<R> task ) {
		Link<P, R> link = new Link<>( task );
		this.task.setNextLink( link );
		return new TaskChain<>( first, link );
	}

	private static class Link<P, R> extends Task<R> {

		private final Task<R> wrappedTask;

		private Link<R, ?> nextLink;

		private Exception priorException;

		public Link( Task<R> task ) {
			super( task.getName() );
			this.wrappedTask = task;
			setPriority( task.getPriority() );
		}

		@Override
		public R call() throws Exception {
			// If there was a prior exception cascade the exception down the chain
			if( priorException != null ) return failure( priorException );

			try {
				R result = wrappedTask.call();
				if( !wrappedTask.isCancelled() ) proceed( result );
				return result;
			} catch( Exception throwable ) {
				return failure( throwable );
			}
		}

		@Override
		public String toString() {
			return "Chained " + wrappedTask.getName();
		}

		/**
		 * This method, along with {@link #setParameter(Link, Object)}, is the
		 * special connection between this task and the next task. The	loose coupling
		 * using the wildcard generics allows the compiler to chain the tasks together.
		 */
		@SuppressWarnings( "unchecked" )
		private Link<?, ?> setNextLink( Link<?, ?> nextLink ) {
			return this.nextLink = (Link<R, ?>)nextLink;
		}

		/**
		 * This method, along with {@link #setNextLink(Link)}, is the special
		 * connection between this task and the next task. The	loose coupling using the
		 * wildcard generics allows the compiler to chain the tasks together.
		 */
		@SuppressWarnings( "unchecked" )
		private void setParameter( Link<R, ?> wrapper, Object parameter ) {
			if( wrapper.wrappedTask instanceof FunctionTask ) ((FunctionTask<P, R>)wrapper.wrappedTask).setParameter( (P)parameter );
		}

		private void proceed( R result ) {
			if( nextLink == null ) return;
			setParameter( nextLink, result );
			submit( nextLink, result );
		}

		@SuppressWarnings( "TypeParameterHidesVisibleType" )
		private <P, R> void submit( Link<P, R> task, P parameter ) {
			if( task == null ) return;
			task.setParameter( parameter );
			getTaskManager().submit( task );
		}

		private void setParameter( P parameter ) {
			if( wrappedTask instanceof FunctionTask ) ((FunctionTask<P, R>)wrappedTask).setParameter( parameter );
		}

		private R failure( Exception priorException ) throws Exception {
			if( nextLink == null ) throw priorException;
			failure( nextLink, priorException );
			return null;
		}

		@SuppressWarnings( "TypeParameterHidesVisibleType" )
		private <P, R> void failure( Link<P, R> task, Exception exception ) {
			if( task == null ) return;
			task.setPriorException( exception );
			getTaskManager().submit( task );
		}

		private void setPriorException( Exception priorException ) {
			this.priorException = priorException;
		}

	}

	private static class SupplierTask<R> extends Task<R> {

		private final TaskChainSupplier<R> supplier;

		public SupplierTask( TaskChainSupplier<R> supplier ) {
			this( null, supplier );
		}

		public SupplierTask( String name, TaskChainSupplier<R> supplier ) {
			super( name );
			this.supplier = supplier;
		}

		@Override
		public R call() throws Exception {
			return supplier.get();
		}

	}

	private static class FunctionTask<P, R> extends Task<R> {

		private final TaskChainFunction<? super P, ? extends R> function;

		private P parameter;

		public FunctionTask( TaskChainFunction<? super P, ? extends R> function ) {
			this( null, function );
		}

		public FunctionTask( String name, TaskChainFunction<? super P, ? extends R> function ) {
			super( name );
			this.function = function;
		}

		public void setParameter( P parameter ) {
			this.parameter = parameter;
		}

		@Override
		public R call() throws Exception {
			return function.apply( parameter );
		}

	}
}
