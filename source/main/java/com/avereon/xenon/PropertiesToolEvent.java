package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class PropertiesToolEvent extends Event {

	public static final EventType<PropertiesToolEvent> PROPERTIES = new EventType<>( Event.ANY, "PROPERTIES" );

	public static final EventType<PropertiesToolEvent> ANY = PROPERTIES;

	public static final EventType<PropertiesToolEvent> SHOW = new EventType<>( PROPERTIES, "SHOW" );

	public static final EventType<PropertiesToolEvent> HIDE = new EventType<>( PROPERTIES, "HIDE" );

	public PropertiesToolEvent( Object source, EventType<? extends PropertiesToolEvent> type ) {
		super( source, type );
		// TODO Probably need a Settings object and a SettingsPage object
	}

}
