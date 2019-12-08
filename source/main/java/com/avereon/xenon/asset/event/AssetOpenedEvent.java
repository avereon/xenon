package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

import static com.avereon.xenon.asset.AssetEvent.Type.OPENED;

public class AssetOpenedEvent extends AssetEvent {

	public AssetOpenedEvent( Object source, Asset asset ) {
		super( source, OPENED, asset );
	}

}
