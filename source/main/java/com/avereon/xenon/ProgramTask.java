package com.avereon.xenon;

import com.avereon.xenon.task.Task;

public abstract class ProgramTask<V> extends Task<V> {

	private Xenon program;

	public ProgramTask( Xenon program ) {
		this( program, null );
	}

	public ProgramTask( Xenon program, String name ) {
		super( name );
		this.program = program;
	}

	protected Xenon getProgram() {
		return program;
	}

}
