package com.avereon.xenon.task;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskExecutor implements ExecutorService {

	private TaskManager manager;

	public TaskExecutor( TaskManager manager ) {
		this.manager = manager;
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		return !manager.isRunning();
	}

	@Override
	public boolean isTerminated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit( Callable<T> task ) {
		return manager.submit( (Task<T>)task );
	}

	@Override
	public <T> Future<T> submit( Runnable task, T result ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Future<?> submit( Runnable task ) {
		return submit( (Callable<?>)Task.of( task ) );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) {
		return tasks.stream().map( t -> manager.submit( (Task<T>)t ) ).collect( Collectors.toList() );
	}

	@Override
	public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void execute( Runnable command ) {
		throw new UnsupportedOperationException();
	}

}
