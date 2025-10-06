package com.avereon.xenon.asset;

import com.avereon.event.EventType;

public class ResourceSwitchedEvent extends AssetEvent {

	public static final EventType<ResourceSwitchedEvent> ASSET_SWITCHED = new EventType<>( AssetEvent.ANY, "CURRENT_ASSET" );

	public static final EventType<ResourceSwitchedEvent> ANY = ASSET_SWITCHED;

	public static final EventType<ResourceSwitchedEvent> SWITCHED = new EventType<>( ASSET_SWITCHED, "SWITCHED" );

	private Asset oldAsset;

	private Asset newAsset;

	public ResourceSwitchedEvent( Object source, EventType<? extends ResourceSwitchedEvent> type, Asset oldAsset, Asset newAsset ) {
		super( source, type, newAsset );
		this.oldAsset = oldAsset;
		this.newAsset = newAsset;
	}

	public Asset getOldAsset() {
		return oldAsset;
	}

	public Asset getNewAsset() {
		return newAsset;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends ResourceSwitchedEvent> getEventType() {
		return (EventType<ResourceSwitchedEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + (oldAsset == null ? "null" : oldAsset.getUri()) + " -> " + (newAsset == null ? "null" : newAsset.getUri());
	}

}
