package com.parallelsymmetry.essence.testutil;

import javafx.application.Platform;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public abstract class FxTestCase extends TestCase {

	static {
		initializeFx();
	}

	private static boolean initializeFx() {
		if( isFxInitialized() ) return true;
		try {
			final CountDownLatch latch = new CountDownLatch( 1 );
			Platform.startup( latch::countDown );
			latch.await();
			return true;
		} catch( InterruptedException exception ) {
			return false;
		}
	}

	protected static boolean isFxInitialized() {
		try {
			Platform.runLater( () -> {} );
			return true;
		} catch( IllegalStateException exception ) {
			return false;
		}
	}

}
