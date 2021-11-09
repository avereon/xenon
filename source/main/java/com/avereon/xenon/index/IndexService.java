package com.avereon.xenon.index;

import com.avereon.skill.Controllable;
import com.avereon.xenon.Program;
import lombok.CustomLog;

@CustomLog
public class IndexService implements Controllable<IndexService> {

	private final Program program;

	public IndexService( Program program ) {
		this.program = program;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public IndexService start() {
		return this;
	}

	@Override
	public IndexService stop() {
		return this;
	}

}
