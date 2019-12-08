package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

import static com.avereon.xenon.asset.AssetEvent.Type.CLOSED;

public class AssetClosedEvent extends AssetEvent {

	public AssetClosedEvent( Object source, Asset asset ) {
		super( source, CLOSED, asset );
	}

}
