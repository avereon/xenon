package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.task.ThrowingFunction;

class FunctionTask<P, R> extends TaskChainTask<R> {

	private P parameter;

	private ThrowingFunction<? super P, ? extends R> function;

	private TaskChain<R> link;

	FunctionTask( ThrowingFunction<? super P, ? extends R> function ) {
		this.function = function;
	}

	void setParameter( P parameter ) {
		this.parameter = parameter;
	}

	void setLink( TaskChain<R> link ) {
		this.link = link;
	}

	@Override
	public R call() throws Exception {
		R result = function.apply( parameter );
		if( link.getNext() != null ) link.submit( getProgram(), result, link.getNext().getTask() );
		return result;
	}

}
