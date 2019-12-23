package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEventOld;

public class AssetSavedEventOld extends AssetEventOld {

	public AssetSavedEventOld( Object source, Asset asset ) {
		super( source, Type.SAVED, asset );
	}

}
