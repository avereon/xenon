package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

public class AssetLoadedEventOld extends AssetEventOld {

	public AssetLoadedEventOld( Object source, Asset asset ) {
		super( source, Type.LOADED, asset );
	}

}
