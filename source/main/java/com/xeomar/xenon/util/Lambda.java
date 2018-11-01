package com.xeomar.xenon.util;

import com.xeomar.xenon.task.Task;

import java.util.TimerTask;

public class Lambda {

	public static Task<?> task( String name, Runnable runnable ) {
		return new Task( name ) {

			@Override
			public Void call() {
				runnable.run();
				return null;
			}

		};
	}

	public static TimerTask timerTask( Runnable runnable ) {
		return new TimerTask() {

			@Override
			public void run() {
				runnable.run();
			}

		};
	}

}
