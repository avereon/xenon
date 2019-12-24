package com.avereon.xenon.workpane;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class ToolEvent extends Event {

	public static final EventType<ToolEvent> TOOL = new EventType<>(Event.ANY, "TOOL");

	public static final EventType<ToolEvent> ANY = TOOL;

	public static final EventType<ToolEvent> OPENING = new EventType<>( TOOL, "OPENING" );

	public static final EventType<ToolEvent> OPENED = new EventType<>( TOOL, "OPENED" );

	public static final EventType<ToolEvent> ADDED = new EventType<>( TOOL, "ADDED" );

	public static final EventType<ToolEvent> ACTIVATED = new EventType<>( TOOL, "ACTIVATED" );

	public static final EventType<ToolEvent> DEACTIVATED = new EventType<>( TOOL, "DEACTIVATED" );

	public static final EventType<ToolEvent> ORDERED = new EventType<>( TOOL, "ORDERED" );

	public static final EventType<ToolEvent> REMOVED = new EventType<>( TOOL, "REMOVED" );

	public static final EventType<ToolEvent> CLOSING = new EventType<>( TOOL, "CLOSING" );

	public static final EventType<ToolEvent> CLOSED = new EventType<>( TOOL, "CLOSED" );

	private static final long serialVersionUID = -4975302925133248236L;

	private Tool tool;

	public ToolEvent( Object source, EventType<ToolEvent> type, Tool tool ) {
		super( source, type );
		this.tool = tool;
	}

	public Tool getTool() {
		return tool;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<ToolEvent> getEventType() {
		return (EventType<ToolEvent>)super.getEventType();
	}

}
