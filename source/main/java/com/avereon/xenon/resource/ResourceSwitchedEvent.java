package com.avereon.xenon.resource;

import com.avereon.event.EventType;
import com.avereon.xenon.asset.AssetEvent;
import com.avereon.xenon.asset.AssetSwitchedEvent;

/**
 * ResourceSwitchedEvent is the new name for the former AssetSwitchedEvent.
 */
public class ResourceSwitchedEvent extends AssetSwitchedEvent {

	@SuppressWarnings("rawtypes")
	public static final EventType<ResourceSwitchedEvent> RESOURCE_SWITCHED = new EventType<>( (EventType) AssetEvent.ANY, "CURRENT_RESOURCE" );

	public static final EventType<ResourceSwitchedEvent> ANY = RESOURCE_SWITCHED;

	public static final EventType<ResourceSwitchedEvent> SWITCHED = new EventType<>( RESOURCE_SWITCHED, "SWITCHED" );

	public ResourceSwitchedEvent( Object source, EventType<? extends ResourceSwitchedEvent> type, Resource oldResource, Resource newResource ) {
		super( source, (EventType) type, oldResource, newResource );
	}
}
