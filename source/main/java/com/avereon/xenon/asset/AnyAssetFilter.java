package com.avereon.xenon.asset;

import com.avereon.product.Rb;
import com.avereon.xenon.BundleKey;

public class AnyAssetFilter implements AssetFilter {

	@Override
	public String getDescription() {
		return Rb.text( BundleKey.LABEL, "all-assets" ) + " (*.*)";
	}

	@Override
	public boolean accept( Asset asset ) {
		return true;
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
