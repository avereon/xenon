package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

public class AssetReadyEventOld extends AssetEventOld {

	public AssetReadyEventOld( Object source, Asset asset ) {
		super( source, AssetEventOld.Type.READY, asset );
	}

}
