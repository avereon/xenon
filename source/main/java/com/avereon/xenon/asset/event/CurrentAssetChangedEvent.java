package com.avereon.xenon.asset.event;

import com.avereon.product.ProductEvent;
import com.avereon.xenon.asset.Asset;

public class CurrentAssetChangedEvent extends ProductEvent {

	private Asset newAsset;

	private Asset oldAsset;

	public CurrentAssetChangedEvent( Object source, Asset oldAsset, Asset newAsset ) {
		super( source );
		this.newAsset = newAsset;
		this.oldAsset = oldAsset;
	}

	public Asset getNewAsset() {
		return newAsset;
	}

	public Asset getOldAsset() {
		return oldAsset;
	}

}
