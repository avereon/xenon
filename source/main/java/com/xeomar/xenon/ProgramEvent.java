package com.xeomar.xenon;

import org.slf4j.Logger;

import java.util.Set;

public class ProgramEvent {

	private static Logger log = LogUtil.get( ProgramEvent.class );

	private Object source;

	public ProgramEvent(Object source ) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}

	public String toString() {
		String sourceClass = getSource().getClass().getSimpleName();
		String eventClass = getClass().getSimpleName();
		return( sourceClass + ":" + eventClass );
	}

	public void fire( Set<? extends ProgramEventListener> listeners ){
		for( ProgramEventListener listener : listeners ) {
			try {
				listener.eventOccurred( this );
			} catch( Throwable throwable ) {
				log.error( "Error dispatching event", throwable );
			}
		}
	}

}
