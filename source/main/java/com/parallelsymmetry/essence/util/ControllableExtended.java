package com.parallelsymmetry.essence.util;

import java.util.concurrent.TimeUnit;

public interface ControllableExtended<T> extends Controllable<T> {

	T startAndWait( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException;

	T stopAndWait( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException;

}
