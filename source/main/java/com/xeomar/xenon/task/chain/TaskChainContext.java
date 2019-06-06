package com.xeomar.xenon.task.chain;

class TaskChainContext {

	private TaskChain<?> first;

	TaskChainContext() {}

	TaskChain<?> getFirst() {
		return first;
	}

	void setFirst( TaskChain<?> first ) {
		this.first = first;
	}

}
