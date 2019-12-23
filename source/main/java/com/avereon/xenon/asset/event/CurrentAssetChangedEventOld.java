package com.avereon.xenon.asset.event;

import com.avereon.xenon.ProductEventOld;
import com.avereon.xenon.asset.Asset;

public class CurrentAssetChangedEventOld extends ProductEventOld {

	private Asset newAsset;

	private Asset oldAsset;

	public CurrentAssetChangedEventOld( Object source, Asset oldAsset, Asset newAsset ) {
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

	@Override
	public String toString() {
		return super.toString() + ": " + newAsset.getUri();
	}

}
