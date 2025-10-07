package com.avereon.xenon.asset;

import com.avereon.event.EventType;

public class ResourceSwitchedEvent extends ResourceEvent {

	public static final EventType<ResourceSwitchedEvent> ASSET_SWITCHED = new EventType<>( ResourceEvent.ANY, "CURRENT_ASSET" );

	public static final EventType<ResourceSwitchedEvent> ANY = ASSET_SWITCHED;

	public static final EventType<ResourceSwitchedEvent> SWITCHED = new EventType<>( ASSET_SWITCHED, "SWITCHED" );

	private Resource oldResource;

	private Resource newResource;

	public ResourceSwitchedEvent( Object source, EventType<? extends ResourceSwitchedEvent> type, Resource oldResource, Resource newResource ) {
		super( source, type, newResource );
		this.oldResource = oldResource;
		this.newResource = newResource;
	}

	public Resource getOldAsset() {
		return oldResource;
	}

	public Resource getNewAsset() {
		return newResource;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends ResourceSwitchedEvent> getEventType() {
		return (EventType<ResourceSwitchedEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + (oldResource == null ? "null" : oldResource.getUri()) + " -> " + (newResource == null ? "null" : newResource.getUri());
	}

}
