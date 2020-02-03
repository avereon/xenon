package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.util.Log;

import java.lang.System.Logger;

public class ProgramEventWatcher implements EventHandler<Event> {

	private static final Logger log = Log.log();

	public void handle( Event event ) {
		if( "PROGRESS".equals( event.getEventType().getName() ) ) {
			log.log( Log.TRACE,  String.valueOf( event ) );
		} else if( event instanceof ProgramEvent ) {
			log.log( Log.INFO,  String.valueOf( event ) );
//		} else if( event instanceof AssetEvent ) {
//			log.log( Log.WARN,  String.valueOf( event ) );
		} else {
			log.log( Log.DEBUG,  String.valueOf( event ) );
		}
	}

}
