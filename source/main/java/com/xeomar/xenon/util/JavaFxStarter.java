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

	public synchronized static void startAndWait( long timeout ) {
		if( started ) return;
		new Thread( JavaFxStarter::launch ).start();
		long limit = System.currentTimeMillis() + timeout;
		synchronized( startLock ) {
			while( !started ) {
				try {
					startLock.wait( 100 );
				} catch( InterruptedException exception ) {
					exception.printStackTrace();
				}
				if( System.currentTimeMillis() > limit ) return;
			}
		}
	}

}
