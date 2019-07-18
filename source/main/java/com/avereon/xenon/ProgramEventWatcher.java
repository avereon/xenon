package com.avereon.xenon;

import com.avereon.product.ProductEvent;
import com.avereon.product.ProductEventListener;
import com.avereon.util.LogUtil;
import com.avereon.xenon.event.ProgramSettingsEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramEventWatcher implements ProductEventListener {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	@Override
	public void handleEvent( ProductEvent event ) {
		if( event instanceof ProgramSettingsEvent ) {
			log.debug( event.toString() );
		} else {
			log.info( event.toString() );
		}
	}

}
