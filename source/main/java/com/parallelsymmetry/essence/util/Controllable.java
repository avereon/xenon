package com.parallelsymmetry.essence.util;

import java.util.concurrent.TimeUnit;

public interface Controllable<T> {

	boolean isRunning();

	T start() throws ControllableException;

	T restart( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException;

	T stop() throws ControllableException;

}
