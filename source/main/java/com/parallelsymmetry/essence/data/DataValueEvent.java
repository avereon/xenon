package com.parallelsymmetry.essence.data;

public abstract class DataValueEvent extends DataEvent {

	private DataNode cause;

	public DataValueEvent( Type type, Action action, DataNode sender, DataNode cause ) {
		super( type, action, sender );
		this.cause = cause;
	}

	public DataNode getCause() {
		return cause;
	}

}
