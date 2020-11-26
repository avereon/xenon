package com.avereon.xenon.task;

import com.avereon.xenon.Program;

public class NewTaskChain<T> {

	private LinkTask<?, ?> first;

	private LinkTask<?, ?> prior;

	private TaskManager taskManager;

	public static <R> NewTaskChain<R> of( Task<R> task ) {
		return new NewTaskChain<R>().link( task );
	}

	public static <R> NewTaskChain<R> of( ThrowingSupplier<R> supplier ) {
		return new NewTaskChain<R>().link( supplier );
	}

	public static <T, R> NewTaskChain<T> of( ThrowingFunction<T, R> function ) {
		return new NewTaskChain<T>().link( function );
	}

	public static <R> NewTaskChain<R> of( String name, ThrowingSupplier<R> supplier ) {
		return new NewTaskChain<R>().link( name, supplier );
	}

	public static <T, R> NewTaskChain<T> of( String name, ThrowingFunction<T, R> function ) {
		return new NewTaskChain<T>().link( name, function );
	}

	public <R> NewTaskChain<T> link( Task<R> task ) {
		return link( new LinkTask<Void, R>( task ) );
	}

	public <R> NewTaskChain<T> link( ThrowingSupplier<R> supplier ) {
		return link( new LinkTask<Void, R>( new SupplierTask<>( supplier ) ) );
	}

	public <R> NewTaskChain<T> link( ThrowingFunction<T, R> function ) {
		return link( new LinkTask<T, R>( new FunctionTask<>( function ) ) );
	}

	public <R> NewTaskChain<T> link( String name, ThrowingSupplier<R> supplier ) {
		return link( new LinkTask<Void, R>( new SupplierTask<>( name, supplier ) ) );
	}

	public <R> NewTaskChain<T> link( String name, ThrowingFunction<T, R> function ) {
		return link( new LinkTask<T, R>( new FunctionTask<>( name, function ) ) );
	}

	@SuppressWarnings( "unchecked" )
	public Task<T> getFinalTask() {
		return (Task<T>)prior;
	}

	@SuppressWarnings( "unchecked" )
	public Task<T> run( Program program ) {
		this.taskManager = program.getTaskManager();
		taskManager.submit( first );
		return (Task<T>)prior;
	}

	private <P, R> NewTaskChain<T> link( LinkTask<P, R> task ) {
		if( first == null ) {
			first = task;
		} else {
			prior.setNextTask( task );
		}
		prior = task;

		return this;
	}

	private class LinkTask<U, V> extends Task<V> {

		private final Task<V> task;

		private Object parameter;

		private LinkTask<?, ?> next;

		private Exception priorException;

		public LinkTask( Task<V> task ) {
			this.task = task;
			setTotal( task.getTotal() );
			task.getEventBus().register( TaskEvent.PROGRESS, e -> {
				setProgress( task.getProgress() );
				System.out.println( "progress=" + task.getProgress() );
			} );
		}

		@Override
		public String getName() {
			return task.getName();
		}

		@Override
		public boolean cancel( boolean mayInterruptIfRunning ) {
			return task.cancel( mayInterruptIfRunning ) & super.cancel( mayInterruptIfRunning );
		}

		void setNextTask( LinkTask<?, ?> next ) {
			this.next = next;
		}

		private void setParameter( Object parameter ) {
			this.parameter = parameter;
		}

		private void setPriorException( Exception priorException ) {
			this.priorException = priorException;
		}

		private void proceed( V parameter ) {
			if( next == null ) return;
			next.setParameter( parameter );
			taskManager.submit( next );
		}

		private V failure( Exception priorException ) throws Exception {
			if( next == null ) throw priorException;
			next.setPriorException( priorException );
			taskManager.submit( next );
			return null;
		}

		@SuppressWarnings( "unchecked" )
		private void setParameter() {
			((FunctionTask<U, V>)task).setParameter( (U)parameter );
		}

		@Override
		public V call() throws Exception {
			// If there was a prior exception cascade the exception down the chain
			if( priorException != null ) return failure( priorException );

			try {
				if( task instanceof FunctionTask ) setParameter();
				V result = task.call();
				if( !isCancelled() ) proceed( result );
				return result;
			} catch( Exception throwable ) {
				return failure( throwable );
			}

		}

	}

	private static class SupplierTask<R> extends Task<R> {

		private final ThrowingSupplier<R> supplier;

		SupplierTask( ThrowingSupplier<R> supplier ) {
			this( null, supplier );
		}

		SupplierTask( String name, ThrowingSupplier<R> supplier ) {
			super( name );
			this.supplier = supplier;
		}

		@Override
		public R call() throws Exception {
			return supplier.get();
		}

	}

	private static class FunctionTask<P, R> extends Task<R> {

		private P parameter;

		private final ThrowingFunction<? super P, ? extends R> function;

		FunctionTask( ThrowingFunction<? super P, ? extends R> function ) {
			this( null, function );
		}

		FunctionTask( String name, ThrowingFunction<? super P, ? extends R> function ) {
			super( name );
			this.function = function;
		}

		void setParameter( P parameter ) {
			this.parameter = parameter;
		}

		@Override
		public R call() throws Exception {
			return function.apply( parameter );
		}

	}

}
