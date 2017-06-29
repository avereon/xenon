package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.task.Task;

public abstract class ProgramTask<V> extends Task<V> {

	private Program program;

	public ProgramTask( Program program ) {
		this( program, null );
	}

	public ProgramTask( Program program, String name ) {
		super(  name );
		this.program = program;
	}

	protected Program getProgram() {
		return program;
	}

}
