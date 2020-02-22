package com.avereon.xenon.task.chain;

import com.avereon.xenon.task.ThrowingFunction;

class FunctionTask<P, R> extends TaskChainTask<R> {

	private P parameter;

	private ThrowingFunction<? super P, ? extends R> function;

	FunctionTask( ThrowingFunction<? super P, ? extends R> function ) {
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
