package com.avereon.xenon.util;

import com.avereon.zarra.javafx.Fx;

import java.util.Timer;

public class TimerUtil {

	private static final Timer timer = new Timer( true );

	public static void fxTask( Runnable runnable, long delay ) {
		timer.schedule( Lambda.timerTask( () -> Fx.run( runnable ) ), delay );
	}

}
