package com.parallelsymmetry.essence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ProgramEvent {

	private static Logger log = LoggerFactory.getLogger( ProgramEvent.class );

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

	public void dispatch( Set<? extends ProgramEventListener> listeners ){
		for( ProgramEventListener listener : listeners ) {
			try {
				listener.eventOccurred( this );
			} catch( Throwable throwable ) {
				log.error( "Error dispatching event", throwable );
			}
		}
	}

}
