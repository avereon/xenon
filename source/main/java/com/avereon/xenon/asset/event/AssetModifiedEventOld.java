package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

public class AssetModifiedEventOld extends AssetEventOld {

	public AssetModifiedEventOld( Object source, Asset asset ) {
		super( source, Type.MODIFIED, asset );
	}

}
