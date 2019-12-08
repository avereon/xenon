package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

public class AssetLoadedEvent extends AssetEvent {

	public AssetLoadedEvent( Object source, Asset asset ) {
		super( source, Type.LOADED, asset );
	}

}
