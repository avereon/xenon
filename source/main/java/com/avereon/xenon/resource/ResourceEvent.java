package com.avereon.xenon.resource;

import com.avereon.event.EventType;
import com.avereon.xenon.asset.AssetEvent;

/**
 * ResourceEvent is the new name for the former AssetEvent.
 * This class extends the existing AssetEvent to expose the Resource-based API.
 */
public class ResourceEvent extends AssetEvent {

	public static final EventType<ResourceEvent> ANY = new EventType<>( AssetEvent.ANY, "RESOURCE" );

	public ResourceEvent( Object source, EventType<? extends ResourceEvent> type, Resource resource ) {
		// Delegate to the AssetEvent constructor with the same source/type/asset
		super( source, (EventType) type, resource );
	}

	@SuppressWarnings("unchecked")
	@Override
	public EventType<? extends ResourceEvent> getEventType() {
		return (EventType<ResourceEvent>) super.getEventType();
	}
}
