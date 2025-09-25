package com.avereon.xenon.workpane;

import javafx.event.EventTarget;
import javafx.event.EventType;
import lombok.Getter;

import java.io.Serial;

@Getter
public class ToolEvent extends WorkpaneEvent {

	public static final EventType<ToolEvent> TOOL = new EventType<>( WorkpaneEvent.ANY, "TOOL" );

	public static final EventType<ToolEvent> ANY = TOOL;

	public static final EventType<ToolEvent> OPENING = new EventType<>( TOOL, "OPENING" );

	public static final EventType<ToolEvent> OPENED = new EventType<>( TOOL, "OPENED" );

	public static final EventType<ToolEvent> ADDED = new EventType<>( TOOL, "ADDED" );

	public static final EventType<ToolEvent> REMOVED = new EventType<>( TOOL, "REMOVED" );

	public static final EventType<ToolEvent> DISPLAYED = new EventType<>( TOOL, "DISPLAYED" );

	public static final EventType<ToolEvent> CONCEALED = new EventType<>( TOOL, "CONCEALED" );

	public static final EventType<ToolEvent> ACTIVATED = new EventType<>( TOOL, "ACTIVATED" );

	public static final EventType<ToolEvent> DEACTIVATED = new EventType<>( TOOL, "DEACTIVATED" );

	public static final EventType<ToolEvent> REORDERED = new EventType<>( TOOL, "ORDERED" );

	public static final EventType<ToolEvent> CLOSING = new EventType<>( TOOL, "CLOSING" );

	public static final EventType<ToolEvent> CLOSED = new EventType<>( TOOL, "CLOSED" );

	@Serial
	private static final long serialVersionUID = -4975302925133248236L;

	private final Tool tool;

	public ToolEvent( Object source, EventType<? extends ToolEvent> type, Workpane workpane, Tool tool ) {
		super( source, type, workpane );
		this.tool = tool;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends ToolEvent> getEventType() {
		return (EventType<? extends ToolEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		Tool tool = getTool();
		Object source = getSource();
		EventTarget target = getTarget();
		String eventInfo = getClass().getSimpleName() + ":" + getEventType() + "@" + System.identityHashCode( this );
		String toolInfo = tool == null ? "null" : tool.getClass().getSimpleName() + "@" + System.identityHashCode( tool );
		String sourceInfo = source == null ? "null" : source.getClass().getSimpleName() + "@" + System.identityHashCode( source );
		String targetInfo = target == null ? "null" : target.getClass().getSimpleName() + "@" + System.identityHashCode( target );
		return eventInfo + " tool=" + toolInfo + " source=" + sourceInfo + " target=" + targetInfo;
	}

}
