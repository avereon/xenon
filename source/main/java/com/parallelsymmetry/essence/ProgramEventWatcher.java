package com.parallelsymmetry.essence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramEventWatcher implements ProgramEventListener {

	private static Logger log = LoggerFactory.getLogger( ProgramEventWatcher.class );

	@Override
	public void eventOccurred( ProgramEvent event ) {
		String sourceClass = event.getSource().getClass().getSimpleName();
		String eventClass = event.getClass().getSimpleName();
		log.info( sourceClass + ":" + eventClass );
	}

}
