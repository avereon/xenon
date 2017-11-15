package com.xeomar.xenon;

import com.xeomar.util.Controllable;

import java.util.concurrent.TimeUnit;

public class UpdateManager implements Controllable<UpdateManager> {

	private Program program;

	public UpdateManager( Program program ) {
		this.program = program;
	}

	public void checkForUpdates() {
		// NEXT Work on implementing update manager
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public UpdateManager start() {
		return this;
	}

	@Override
	public UpdateManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public UpdateManager restart() {
		return this;
	}

	@Override
	public UpdateManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public UpdateManager stop() {
		return this;
	}

	@Override
	public UpdateManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}
}
