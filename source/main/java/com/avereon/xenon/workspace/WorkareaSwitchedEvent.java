package com.avereon.xenon.workspace;

import com.avereon.event.EventType;

public class WorkareaSwitchedEvent extends WorkspaceEvent {

	public static final EventType<WorkareaSwitchedEvent> WORKAREA_SWITCHED = new EventType<>( WorkspaceEvent.ANY, "WORKAREA_SWITCHED" );

	public static final EventType<WorkareaSwitchedEvent> ANY = WORKAREA_SWITCHED;

	public static final EventType<WorkareaSwitchedEvent> SWITCHED = new EventType<>( WORKAREA_SWITCHED, "SWITCHED" );

	private Workarea oldWorkarea;

	private Workarea newWorkarea;

	public WorkareaSwitchedEvent( Object source, EventType<? extends WorkareaSwitchedEvent> type, Workspace workspace, Workarea oldWorkarea, Workarea newWorkarea ) {
		super( source, type, workspace );
		this.oldWorkarea = oldWorkarea;
		this.newWorkarea = newWorkarea;
	}

	public Workarea getOldWorkarea() {
		return oldWorkarea;
	}

	public Workarea getNewWorkarea() {
		return newWorkarea;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends WorkareaSwitchedEvent> getEventType() {
		return (EventType<? extends WorkareaSwitchedEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + ( oldWorkarea == null ? "null" : oldWorkarea.getName() + " -> " + (newWorkarea == null ? "null" : newWorkarea.getName() ) );
	}

}
