package com.parallelsymmetry.essence.util;

import java.util.concurrent.TimeUnit;

@SuppressWarnings( { "unused", "UnusedReturnValue" } )
public interface Controllable<T> {

	boolean isRunning();

	T start();

	T awaitStart( long timeout, TimeUnit unit ) throws InterruptedException;

	T restart();

	T awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException;

	T stop();

	T awaitStop( long timeout, TimeUnit unit ) throws InterruptedException;

}
