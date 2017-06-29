package com.parallelsymmetry.essence.util;

import java.util.concurrent.TimeUnit;

public interface Controllable {

	boolean isRunning();

	void start() throws ControllableException;

	void restart(long timeout, TimeUnit unit) throws ControllableException, InterruptedException;

	void stop() throws ControllableException;

}
