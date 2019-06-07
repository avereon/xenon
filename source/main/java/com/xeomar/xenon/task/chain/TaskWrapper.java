package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;

import java.util.function.Function;

class TaskWrapper<P, R> extends Task<R> {

	private Program program;

	private P parameter;

	private Task<R> task;

	private TaskChain<R> link;

	public TaskWrapper( Task<R> task ) {
		this.task = task;
	}

	public Program getProgram() {
		return program;
	}

	public void setProgram( Program program ) {
		this.program = program;
	}

	void setParameter( P parameter ) {
		this.parameter = parameter;
	}

	void setLink( TaskChain<R> link ) {
		this.link = link;
	}

	@Override
	public R call() throws Exception {
		if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );

		System.out.println( "TaskWrapper before call()..." );
		System.out.println( "Task is: " + task.getClass().getSimpleName());
		R result = task.call();
		System.out.println( "TaskWrapper after call()...with result: " + result );
		try {
			if( link.getNext() != null ) link.submit( getProgram(), result, link.getNext().getTask() );
		} catch( Throwable throwable ) {
			throwable.printStackTrace();
		}
		System.out.println( "TaskWrapper after submit()..." );
		return result;
	}

}
