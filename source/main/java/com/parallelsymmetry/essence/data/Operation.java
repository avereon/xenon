package com.parallelsymmetry.essence.data;

public abstract class Operation {

	protected DataNode source;

	public Operation( DataNode source ) {
		this.source = source;
	}

	public DataNode getData() {
		return source;
	}

	protected abstract OperationResult process();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + System.identityHashCode( this ) + "]";
	}

}
