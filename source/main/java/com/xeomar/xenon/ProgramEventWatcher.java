package com.xeomar.xenon;

import com.xeomar.product.ProductEvent;
import com.xeomar.product.ProductEventListener;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.event.ProgramSettingsEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramEventWatcher implements ProductEventListener {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	@Override
	public void handleEvent( ProductEvent event ) {
		if( event instanceof ProgramSettingsEvent ) {
			log.trace( event.toString() );
		} else {
			log.info( event.toString() );
		}
	}

}
