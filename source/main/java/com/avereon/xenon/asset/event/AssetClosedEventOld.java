package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

import static com.avereon.xenon.asset.AssetEventOld.Type.CLOSED;

public class AssetClosedEventOld extends AssetEventOld {

	public AssetClosedEventOld( Object source, Asset asset ) {
		super( source, CLOSED, asset );
	}

}
