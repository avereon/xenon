package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.SettingsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramEventWatcher implements ProgramEventListener {

	private static Logger log = LoggerFactory.getLogger( ProgramEventWatcher.class );

	@Override
	public void eventOccurred( ProgramEvent event ) {
		if( event instanceof SettingsEvent ) {
			log.debug( event.toString() );
		} else {
			log.info( event.toString() );
		}
	}

}
