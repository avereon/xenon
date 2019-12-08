package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

public class AssetUnmodifiedEvent extends AssetEvent {

	public AssetUnmodifiedEvent( Object source, Asset asset ) {
		super( source, Type.UNMODIFIED, asset );
	}

}
