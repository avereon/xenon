package com.avereon.xenon.tool.guide;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class GuideEvent extends Event {

	public static final EventType<GuideEvent> GUIDE = new EventType<>( Event.ANY, "GUIDE" );

	public static final EventType<GuideEvent> ANY = GUIDE;

	public static final EventType<GuideEvent> GUIDE_CHANGING = new EventType<>( GUIDE, "GUIDE_CHANGING" );

	public static final EventType<GuideEvent> GUIDE_CHANGED = new EventType<>( GUIDE, "GUIDE_CHANGED" );

	private Guide oldGuide;

	private Guide newGuide;

	public GuideEvent( Object source, EventType<? extends Event> type ) {
		super( source, type );
	}

	public GuideEvent( Object source, EventType<? extends Event> type, Guide oldGuide, Guide newGuide ) {
		this( source, type );
		this.oldGuide = oldGuide;
		this.newGuide = newGuide;
	}

	public Guide getOldGuide() {
		return oldGuide;
	}

	public Guide getNewGuide() {
		return newGuide;
	}

}
