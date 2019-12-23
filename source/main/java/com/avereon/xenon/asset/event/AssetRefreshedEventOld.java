package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

public class AssetRefreshedEventOld extends AssetEventOld {

	public AssetRefreshedEventOld( Object source, Asset asset ) {
		super( source, Type.REFRESHED, asset );
	}

}
