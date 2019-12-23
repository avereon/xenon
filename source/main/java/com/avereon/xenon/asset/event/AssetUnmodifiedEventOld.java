package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

public class AssetUnmodifiedEventOld extends AssetEventOld {

	public AssetUnmodifiedEventOld( Object source, Asset asset ) {
		super( source, Type.UNMODIFIED, asset );
	}

}
