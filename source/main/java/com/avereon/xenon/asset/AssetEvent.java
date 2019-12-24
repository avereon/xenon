package com.avereon.xenon.asset;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class AssetEvent extends Event {

	public static final EventType<AssetEvent> ASSET = new EventType<>( Event.ANY, "ASSET" );

	public static final EventType<AssetEvent> ANY = ASSET;

	public static final EventType<AssetEvent> OPENED = new EventType<>( ASSET, "OPENED" );

	public static final EventType<AssetEvent> LOADED = new EventType<>( ASSET, "LOADED" );

	public static final EventType<AssetEvent> READY = new EventType<>( ASSET, "READY" );

	public static final EventType<AssetEvent> REFRESHED = new EventType<>( ASSET, "REFRESHED" );

	public static final EventType<AssetEvent> MODIFIED = new EventType<>( ASSET, "MODIFIED" );

	public static final EventType<AssetEvent> UNMODIFIED = new EventType<>( ASSET, "UNMODIFIED" );

	public static final EventType<AssetEvent> SAVED = new EventType<>( ASSET, "SAVED" );

	public static final EventType<AssetEvent> CLOSED = new EventType<>( ASSET, "CLOSED" );

	private Asset asset;

	public AssetEvent( Object source, EventType<? extends AssetEvent> type, Asset asset ) {
		super( source, type );
		this.asset = asset;
	}

	public Asset getAsset() {
		return asset;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<? extends AssetEvent> getEventType() {
		return (EventType<AssetEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		Asset asset = getAsset();
		if( asset == null ) return super.toString() + ": null";
		return super.toString() + ": " + asset.toString();
	}

}
