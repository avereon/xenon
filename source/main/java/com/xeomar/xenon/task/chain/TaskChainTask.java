package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;

public abstract class TaskChainTask<R> extends Task<R> {

	private Program program;

	private TaskChain<R> link;

	public Program getProgram() {
		return program;
	}

	public void setProgram( Program program ) {
		this.program = program;
	}

	void setLink( TaskChain<R> link ) {
		this.link = link;
	}

	TaskChain<R> getLink() {
		return link;
	}

}
