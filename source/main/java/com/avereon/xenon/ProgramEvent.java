package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class ProgramEvent extends Event {

	public static final EventType<ProgramEvent> PROGRAM = new EventType<>( Event.ANY, "PROGRAM" );

	public static final EventType<ProgramEvent> ANY = PROGRAM;

	public static final EventType<ProgramEvent> STARTING = new EventType<>( PROGRAM, "STARTING" );

	public static final EventType<ProgramEvent> STARTED = new EventType<>( PROGRAM, "STARTED" );

	public static final EventType<ProgramEvent> STOPPING = new EventType<>( PROGRAM, "STOPPING" );

	public static final EventType<ProgramEvent> STOPPED = new EventType<>( PROGRAM, "STOPPED" );

	public ProgramEvent( Object source, EventType<? extends ProgramEvent> type ) {
		super( source, type );
	}

}
