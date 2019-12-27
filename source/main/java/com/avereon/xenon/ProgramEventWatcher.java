package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.util.LogUtil;
import com.avereon.xenon.asset.AssetEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramEventWatcher extends EventHub<Event> implements EventHandler<Event> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public ProgramEventWatcher() {
		register( Event.ANY, e -> {
			if( e instanceof AssetEvent ) {
				log.warn( String.valueOf( e ) );
			} else {
				log.debug( String.valueOf( e ) );
			}
		} );
	}

}
