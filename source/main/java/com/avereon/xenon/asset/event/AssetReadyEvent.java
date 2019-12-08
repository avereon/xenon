package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

public class AssetReadyEvent extends AssetEvent {

	public AssetReadyEvent( Object source, Asset asset ) {
		super( source, AssetEvent.Type.READY, asset );
	}

}
