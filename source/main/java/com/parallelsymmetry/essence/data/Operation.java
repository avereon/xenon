package com.parallelsymmetry.essence.data;

public abstract class Operation {

	protected Object source;

	public Operation( Object source ) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}

	protected abstract OperationResult process();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + System.identityHashCode( this ) + "]";
	}

}
