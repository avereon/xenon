package com.avereon.xenon.util;

import javafx.application.Platform;

import java.util.Timer;

public class TimerUtil {

	private static Timer timer = new Timer( true );

	public static void fxTask( Runnable runnable, long delay ) {
		timer.schedule( Lambda.timerTask( () -> Platform.runLater( runnable ) ), delay );
	}

}
