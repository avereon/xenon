package com.avereon.xenon.task.chain;

import com.avereon.xenon.task.ThrowingSupplier;

class SupplierTask<R> extends TaskChainTask<R> {

	private ThrowingSupplier<R> supplier;

	SupplierTask( ThrowingSupplier<R> supplier ) {
		this.supplier = supplier;
	}

	@Override
	public R call() throws Exception {
		//TaskChain<R> link = getLink();
		R result = supplier.get();
		//if( link.getNext() != null ) link.submit( getProgram(), result, link.getNext().getTask() );
		return result;
	}

}
