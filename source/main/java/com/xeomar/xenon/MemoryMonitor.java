package com.xeomar.xenon;

import com.xeomar.util.FileUtil;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemoryMonitor extends Label {

	private static final String DIVIDER = "/";

	private static Timer timer;

	private static Set<MemoryMonitor> monitors;

	// IDEA updates every five seconds

	static {
		monitors = new CopyOnWriteArraySet<>();
		timer = new Timer( "Memory Monitor Timer", true );
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				update();
			}

		};
		timer.schedule( task, 0, 2000 );
	}

	public MemoryMonitor() {
		setMinHeight( 20 ); // 16 + 2px top and 2px bottom
		setId( "status-bar-memory-monitor" );
		monitors.add( this );
	}

	public void close() {
		monitors.remove( this );
	}

	private static void update() {
		Runtime runtime = Runtime.getRuntime();
		long maximum = runtime.maxMemory();
		long allocated = runtime.totalMemory();
		long used = allocated - runtime.freeMemory();

		float allocatedPercent = (float)allocated / (float)maximum;
		float usedPercent = (float)used / (float)maximum;

		String usedSize = FileUtil.getHumanSize( used );
		String allocatedSize = FileUtil.getHumanSize( allocated );
		String maximumSize = FileUtil.getHumanSize( maximum );

		//		long usedMegabytes = used / MEGABYTE;
		//		long allocatedMegabytes = allocated / MEGABYTE;
		//		long maximumMegabytes = maximum / MEGABYTE;

		String text;
		//		if( MemoryMonitorFactory.isShowPercent() ) {
		//			text = format.format( usedPercent * 100 ) + "% " + DIVIDER + " " + format.format( allocatedPercent * 100 ) + "% " + DIVIDER + " " + maximumSize ;
		//		} else {
		text = usedSize + " " + DIVIDER + " " + allocatedSize + " " + DIVIDER + " " + maximumSize;
		//		}

		for( MemoryMonitor monitor : monitors ) {
			Platform.runLater( () -> monitor.setText( text ) );
		}
	}

}
