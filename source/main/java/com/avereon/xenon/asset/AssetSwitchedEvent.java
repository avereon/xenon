package com.avereon.xenon.asset;

import com.avereon.event.EventType;

public class AssetSwitchedEvent extends AssetEvent {

	public static final EventType<AssetSwitchedEvent> ASSET_SWITCHED = new EventType<>( AssetEvent.ANY, "CURRENT_ASSET" );

	public static final EventType<AssetSwitchedEvent> ANY = ASSET_SWITCHED;

	public static final EventType<AssetSwitchedEvent> SWITCHED = new EventType<>( ASSET_SWITCHED, "SWITCHED" );

	private Asset oldAsset;

	private Asset newAsset;

	public AssetSwitchedEvent( Object source, EventType<? extends AssetSwitchedEvent> type, Asset oldAsset, Asset newAsset ) {
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
	public EventType<? extends AssetSwitchedEvent> getEventType() {
		return (EventType<AssetSwitchedEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		return super.toString() + ": " + (oldAsset == null ? "null" : oldAsset.getUri()) + " -> " + (newAsset == null ? "null" : newAsset.getUri());
	}

}
