package com.avereon.xenon.asset;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import lombok.Getter;

@Getter
public class AssetEvent extends Event {

	public static final EventType<AssetEvent> ASSET = new EventType<>( Event.ANY, "ASSET" );

	public static final EventType<AssetEvent> ANY = ASSET;

	public static final EventType<AssetEvent> OPENED = new EventType<>( ASSET, "OPENED" );

	public static final EventType<AssetEvent> LOADED = new EventType<>( ASSET, "LOADED" );

	@Deprecated
	public static final EventType<AssetEvent> READY = new EventType<>( ASSET, "READY" );

	public static final EventType<AssetEvent> MODIFIED = new EventType<>( ASSET, "MODIFIED" );

	public static final EventType<AssetEvent> UNMODIFIED = new EventType<>( ASSET, "UNMODIFIED" );

	// The asset is the asset in the active tool
	public static final EventType<AssetEvent> ACTIVATED = new EventType<>( ASSET, "ACTIVATED" );

	// The asset is not the asset in the active tool
	public static final EventType<AssetEvent> DEACTIVATED = new EventType<>( ASSET, "DEACTIVATED" );

	public static final EventType<AssetEvent> SAVED = new EventType<>( ASSET, "SAVED" );

	public static final EventType<AssetEvent> CLOSED = new EventType<>( ASSET, "CLOSED" );

	public static final EventType<AssetEvent> DELETED = new EventType<>( ASSET, "DELETED" );

	private final Asset asset;

	public AssetEvent( Object source, EventType<? extends AssetEvent> type, Asset asset ) {
		super( source, type );
		this.asset = asset;
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
		return super.toString() + ": " + asset;
	}

}
