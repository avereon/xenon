package com.xeomar.xenon.util;

import javafx.application.Application;
import javafx.stage.Stage;

public final class JavaFxStarter extends Application {

	private final static Object startLock = new Object();

	private static boolean started;

	public JavaFxStarter() {}

	@Override
	public void start( Stage primaryStage ) throws Exception {
		synchronized( startLock ) {
			started = true;
			startLock.notifyAll();
		}
	}

	public static void startAndWait( long timeout ) {
		synchronized( startLock ) {
			if( started ) return;

			new Thread( () -> {
				try {
					JavaFxStarter.launch();
				} catch( IllegalStateException exception ) {
					started = true;
				}
			} ).start();

			long limit = System.currentTimeMillis() + timeout;
			while( !started ) {
				try {
					startLock.wait( 20 );
				} catch( InterruptedException exception ) {
					exception.printStackTrace();
				}
				if( System.currentTimeMillis() > limit ) return;
			}
		}
	}

}
