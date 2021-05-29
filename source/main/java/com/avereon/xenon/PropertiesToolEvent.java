package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import com.avereon.xenon.tool.settings.SettingsPage;

public class PropertiesToolEvent extends Event {

	public static final EventType<PropertiesToolEvent> PROPERTIES = new EventType<>( Event.ANY, "PROPERTIES" );

	public static final EventType<PropertiesToolEvent> ANY = PROPERTIES;

	public static final EventType<PropertiesToolEvent> SHOW = new EventType<>( PROPERTIES, "SHOW" );

	public static final EventType<PropertiesToolEvent> HIDE = new EventType<>( PROPERTIES, "HIDE" );

	private final SettingsPage page;

	public PropertiesToolEvent( Object source, EventType<? extends PropertiesToolEvent> type, SettingsPage page ) {
		super( source, type );
		if( type == SHOW && page == null ) throw new IllegalArgumentException( "Show page cannot be null" );
		this.page = page;
	}

	public SettingsPage getPage() {
		return page;
	}

}
