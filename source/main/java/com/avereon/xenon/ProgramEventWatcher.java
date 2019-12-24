package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramEventWatcher extends EventHub<Event> implements EventHandler<Event> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public ProgramEventWatcher() {
		register( Event.ANY, e -> {
			if( e instanceof SettingsEvent ) {
				log.error( String.valueOf( e ) );
			} else {
				log.warn( String.valueOf( e ) );
			}
		} );
	}

}
