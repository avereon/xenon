package com.avereon.xenon.asset;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;

public class AnyResourceFilter implements ResourceFilter {

	@Override
	public String getDescription() {
		return Rb.text( RbKey.LABEL, "all-assets" ) + " (*.*)";
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
