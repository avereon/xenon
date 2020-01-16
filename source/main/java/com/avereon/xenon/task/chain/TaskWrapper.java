package com.avereon.xenon.task.chain;

import com.avereon.xenon.Program;
import com.avereon.xenon.task.Task;

class TaskWrapper<P, R> extends Task<R> {

	private Program program;

	private P parameter;

	private Task<R> task;

	private TaskChain<R> link;

	private Exception priorException;

	public TaskWrapper( Task<R> task ) {
		this( task.getName(), task );
	}

	public TaskWrapper( String name, Task<R> task ) {
		super( name );
		this.task = task;
		setPriority( task.getPriority() );
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

	public void setPriorException( Exception priorException ) {
		this.priorException = priorException;
	}

	void setLink( TaskChain<R> link ) {
		this.link = link;
	}

	@Override
	public R call() throws Exception {
		// If there was a prior exception cascade the exception down the chain
		if( priorException != null ) {
			if( link.getNext() == null ) {
				throw priorException;
			} else {
				link.failure( getProgram(), link.getNext().getTask(), priorException );
				return null;
			}
		}

		try {
			if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );
			R result = task.call();
			if( link.getNext() != null ) link.submit( getProgram(), link.getNext().getTask(), result );
			return result;
		} catch( Exception throwable ) {
			if( link.getNext() != null ) link.failure( getProgram(), link.getNext().getTask(), throwable );
			return null;
		}
	}

}
