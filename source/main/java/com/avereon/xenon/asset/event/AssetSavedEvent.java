package com.avereon.xenon.asset.event;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;

public class AssetSavedEvent extends AssetEvent {

	public AssetSavedEvent( Object source, Asset asset ) {
		super( source, Type.SAVED, asset );
	}

}
