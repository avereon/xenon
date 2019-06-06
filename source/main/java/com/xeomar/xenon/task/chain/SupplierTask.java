package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.task.ThrowingSupplier;

class SupplierTask<R> extends TaskChainTask<R> {

	private ThrowingSupplier<R> supplier;

	private TaskChain<R> link;

	SupplierTask( ThrowingSupplier<R> supplier ) {
		this.supplier = supplier;
	}

	void setLink( TaskChain<R> link ) {
		this.link = link;
	}

	@Override
	public R call() throws Exception {
		R result = supplier.get();
		if( link.getNext() != null ) link.submit( getProgram(), result, link.getNext().getTask() );
		return result;
	}

}
