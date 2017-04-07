package com.parallelsymmetry.essence;

public class ProgramEvent {

	private Object source;

	public ProgramEvent(Object source ) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}

}
