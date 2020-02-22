package com.avereon.xenon.product;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import com.avereon.product.ProductCard;
import com.avereon.xenon.ProgramEvent;

public class ModEvent extends ProductEvent {

	public static final EventType<ProgramEvent> MOD = new EventType<>( Event.ANY, "MOD" );

	public static final EventType<ProgramEvent> ANY = MOD;

	public static final EventType<ProgramEvent> INSTALLED = new EventType<>( MOD, "INSTALLED" );

	public static final EventType<ProgramEvent> REGISTERED = new EventType<>( MOD, "REGISTERED" );

	public static final EventType<ProgramEvent> ENABLED = new EventType<>( MOD, "ENABLED" );

	public static final EventType<ProgramEvent> STARTED = new EventType<>( MOD, "STARTED" );

	public static final EventType<ProgramEvent> STOPPED = new EventType<>( MOD, "STOPPED" );

	public static final EventType<ProgramEvent> DISABLED = new EventType<>( MOD, "DISABLED" );

	public static final EventType<ProgramEvent> UNREGISTERED = new EventType<>( MOD, "UNREGISTERED" );

	public static final EventType<ProgramEvent> REMOVED = new EventType<>( MOD, "REMOVED" );

	public ModEvent( Object source, EventType<? extends ProgramEvent> type, ProductCard card ) {
		super( source, type, card );
	}

}
