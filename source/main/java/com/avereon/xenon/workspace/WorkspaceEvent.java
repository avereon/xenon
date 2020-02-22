package com.avereon.xenon.workspace;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class WorkspaceEvent extends Event {

	public static final EventType<WorkspaceEvent> WORKSPACE = new EventType<>( Event.ANY, "WORKSPACE" );

	public static final EventType<WorkspaceEvent> ANY = WORKSPACE;

	private Workspace workspace;

	public WorkspaceEvent( Object source, EventType<? extends WorkspaceEvent> type, Workspace workspace ) {
		super( source, type );
		this.workspace = workspace;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends WorkspaceEvent> getEventType() {
		return (EventType<? extends WorkspaceEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + System.identityHashCode( this );
	}

}
