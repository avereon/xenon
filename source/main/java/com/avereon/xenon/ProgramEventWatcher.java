package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramEventWatcher implements EventHandler<Event> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public void handle( Event event ) {
		if( "PROGRESS".equals( event.getEventType().getName() ) ) {
			log.trace( String.valueOf( event ) );
		} else if( event instanceof ProgramEvent ) {
			log.info( String.valueOf( event ) );
//		} else if( event instanceof AssetEvent ) {
//			log.warn( String.valueOf( event ) );
		} else {
			log.debug( String.valueOf( event ) );
		}
	}

}
