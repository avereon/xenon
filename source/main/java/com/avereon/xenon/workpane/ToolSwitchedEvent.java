package com.avereon.xenon.workpane;

import com.avereon.event.EventType;
import com.avereon.util.JavaUtil;
import com.avereon.xenon.workspace.WorkspaceEvent;

public class ToolSwitchedEvent extends WorkspaceEvent {

	public static final EventType<ToolSwitchedEvent> SWITCHED = new EventType<>( WorkspaceEvent.ANY, "SWITCHED" );

	private Tool oldTool;

	private Tool newTool;

	public ToolSwitchedEvent( Object source, EventType<? extends ToolSwitchedEvent> type, Tool oldTool, Tool newTool ) {
		super( source, type, null );
		this.oldTool = oldTool;
		this.newTool = newTool;
	}

	public Tool getOldTool() {
		return oldTool;
	}

	public Tool getNewTool() {
		return newTool;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends ToolSwitchedEvent> getEventType() {
		return (EventType<? extends ToolSwitchedEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + JavaUtil.getClassName( oldTool ) + " -> " + JavaUtil.getClassName( newTool );
	}

}
