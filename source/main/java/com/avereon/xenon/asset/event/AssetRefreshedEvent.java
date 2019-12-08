package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

public class AssetRefreshedEvent extends AssetEvent {

	public AssetRefreshedEvent( Object source, Asset asset ) {
		super( source, Type.REFRESHED, asset );
	}

}
