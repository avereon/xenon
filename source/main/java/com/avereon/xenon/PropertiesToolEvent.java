package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import com.avereon.xenon.tool.settings.SettingsPage;

import java.util.ArrayList;
import java.util.List;

public class PropertiesToolEvent extends Event {

	public static final EventType<PropertiesToolEvent> PROPERTIES = new EventType<>( Event.ANY, "PROPERTIES" );

	public static final EventType<PropertiesToolEvent> ANY = PROPERTIES;

	public static final EventType<PropertiesToolEvent> SHOW = new EventType<>( PROPERTIES, "SHOW" );

	public static final EventType<PropertiesToolEvent> HIDE = new EventType<>( PROPERTIES, "HIDE" );

	private final List<SettingsPage> pages;

	public PropertiesToolEvent( Object source, EventType<? extends PropertiesToolEvent> type, SettingsPage... page ) {
		this( source, type, List.of( page ) );
	}

	public PropertiesToolEvent( Object source, EventType<? extends PropertiesToolEvent> type, List<SettingsPage> pages ) {
		super( source, type );
		if( type == SHOW && pages == null || pages.isEmpty() ) throw new IllegalArgumentException( "Show pages cannot be null or empty" );
		this.pages = new ArrayList<>( pages );
	}

	public List<SettingsPage> getPages() {
		return pages;
	}

}
