package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.SettingsEvent;
import org.slf4j.Logger;

public class ProgramEventWatcher implements ProgramEventListener {

	private static Logger log = LogUtil.get( ProgramEventWatcher.class );

	@Override
	public void eventOccurred( ProgramEvent event ) {
		if( event instanceof SettingsEvent ) {
			log.trace( event.toString() );
		} else {
			log.info( event.toString() );
		}
	}

}
