package com.parallelsymmetry.essence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by SoderquistMV on 4/7/2017.
 */
public class ProgramEventWatcher implements ProgramEventListener {

	private static Logger log = LoggerFactory.getLogger( ProgramEventWatcher.class );

	@Override
	public void eventOccurred( ProgramEvent event ) {
		log.info( event.toString() );
	}

}
