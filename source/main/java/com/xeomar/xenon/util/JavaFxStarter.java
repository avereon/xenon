package com.xeomar.xenon.util;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.TimeoutException;

public final class JavaFxStarter extends Application {

	private final static Object startLock = new Object();

	private static boolean started;

	public JavaFxStarter() {}

	@Override
	public void start( Stage primaryStage ) {
		setStarted();
	}

	private static void setStarted() {
		synchronized( startLock ) {
			started = true;
			startLock.notifyAll();
		}
	}

	public static void startAndWait( long timeout ) {
		long limit = System.currentTimeMillis() + timeout;

		synchronized( startLock ) {
			if( started ) return;

			new Thread( () -> {
				try {
					JavaFxStarter.launch();
				} catch( IllegalStateException exception ) {
					// Platform was already started by a different class
					setStarted();
				}
			} ).start();

			while( !started ) {
				try {
					startLock.wait( timeout );
				} catch( InterruptedException exception ) {
					exception.printStackTrace();
				}
				if( !started && System.currentTimeMillis() >= limit ) {
					throw new RuntimeException( new TimeoutException( "FX platform start timeout after " + timeout + " ms" ) );
				}
			}
		}
	}

}
