package com.xeomar.xenon.util;

import java.util.TimerTask;

public class LambdaTask {

	public static TimerTask build( Runnable runnable ) {
		return new TimerTask() {

			@Override
			public void run() {
				runnable.run();
			}
		};
	}

}
