package com.parallelsymmetry.essence.util;

import java.util.concurrent.TimeUnit;

public interface ControllableExtended extends Controllable {

	void startAndWait( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException;

	void stopAndWait( long timeout, TimeUnit unit ) throws ControllableException, InterruptedException;

}
