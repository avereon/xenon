package com.avereon.xenon.util;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import com.avereon.util.JavaUtil;

public class FxEventWrapper extends Event {

	public static final EventType<FxEventWrapper> FX_WRAPPER = new EventType<>( Event.ANY, "FX_WRAPPER" );

	private javafx.event.Event fxEvent;

	public FxEventWrapper( javafx.event.Event fxEvent ) {
		super( fxEvent.getSource(), Event.ANY );
		this.fxEvent = fxEvent;
	}

	public javafx.event.Event getFxEvent() {
		return fxEvent;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends FxEventWrapper> getEventType() {
		return (EventType<? extends FxEventWrapper>)super.getEventType();
	}

	@Override
	public String toString() {
		return JavaUtil.getClassName( this ) + ": " + fxEvent;
	}

}
