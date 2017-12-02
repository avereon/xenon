package com.xeomar.xenon;

import com.xeomar.product.ProductEvent;
import com.xeomar.product.ProductEventListener;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.event.SettingsEvent;
import org.slf4j.Logger;

public class ProgramEventWatcher implements ProductEventListener {

	private static Logger log = LogUtil.get( ProgramEventWatcher.class );

	@Override
	public void handleEvent( ProductEvent event ) {
		if( event instanceof SettingsEvent ) {
			log.trace( event.toString() );
		} else {
			log.info( event.toString() );
		}
	}

}
