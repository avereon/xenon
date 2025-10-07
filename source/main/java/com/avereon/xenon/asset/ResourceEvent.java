package com.avereon.xenon.asset;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import lombok.Getter;

@Getter
public class ResourceEvent extends Event {

	public static final EventType<ResourceEvent> ASSET = new EventType<>( Event.ANY, "ASSET" );

	public static final EventType<ResourceEvent> ANY = ASSET;

	public static final EventType<ResourceEvent> OPENED = new EventType<>( ASSET, "OPENED" );

	public static final EventType<ResourceEvent> LOADED = new EventType<>( ASSET, "LOADED" );

	@Deprecated
	public static final EventType<ResourceEvent> READY = new EventType<>( ASSET, "READY" );

	public static final EventType<ResourceEvent> MODIFIED = new EventType<>( ASSET, "MODIFIED" );

	public static final EventType<ResourceEvent> UNMODIFIED = new EventType<>( ASSET, "UNMODIFIED" );

	// The asset is the asset in the active tool
	public static final EventType<ResourceEvent> ACTIVATED = new EventType<>( ASSET, "ACTIVATED" );

	// The asset is not the asset in the active tool
	public static final EventType<ResourceEvent> DEACTIVATED = new EventType<>( ASSET, "DEACTIVATED" );

	public static final EventType<ResourceEvent> SAVED = new EventType<>( ASSET, "SAVED" );

	public static final EventType<ResourceEvent> CLOSED = new EventType<>( ASSET, "CLOSED" );

	public static final EventType<ResourceEvent> DELETED = new EventType<>( ASSET, "DELETED" );

	private final Resource resource;

	public ResourceEvent( Object source, EventType<? extends ResourceEvent> type, Resource resource ) {
		super( source, type );
		this.resource = resource;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends ResourceEvent> getEventType() {
		return (EventType<ResourceEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		Resource resource = getResource();
		if( resource == null ) return super.toString() + ": null";
		return super.toString() + ": " + resource;
	}

}
