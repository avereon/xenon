package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

public class AssetModifiedEvent extends AssetEvent {

	public AssetModifiedEvent( Object source, Asset asset ) {
		super( source, Type.MODIFIED, asset );
	}

}
