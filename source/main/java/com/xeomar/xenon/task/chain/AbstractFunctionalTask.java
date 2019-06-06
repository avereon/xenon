package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;

public abstract class AbstractFunctionalTask<R> extends Task<R> {

	private Program program;

	public Program getProgram() {
		return program;
	}

	public void setProgram( Program program ) {
		this.program = program;
	}

}
