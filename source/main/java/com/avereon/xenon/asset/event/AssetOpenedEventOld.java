package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

import static com.avereon.xenon.asset.AssetEventOld.Type.OPENED;

public class AssetOpenedEventOld extends AssetEventOld {

	public AssetOpenedEventOld( Object source, Asset asset ) {
		super( source, OPENED, asset );
	}

}
