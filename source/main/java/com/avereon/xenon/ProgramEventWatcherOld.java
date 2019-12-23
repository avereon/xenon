package com.avereon.xenon;

import com.avereon.util.LogUtil;
import com.avereon.xenon.event.ProgramSettingsEventOld;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramEventWatcherOld implements ProductEventListener {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	@Override
	public void handleEvent( ProductEventOld event ) {
		if( event instanceof ProgramSettingsEventOld ) {
			log.trace( event.toString() );
		} else {
			log.info( event.toString() );
		}
	}

}
