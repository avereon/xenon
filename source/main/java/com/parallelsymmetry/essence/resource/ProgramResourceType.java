package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.product.Product;

public class ProgramResourceType extends ResourceType {

	public ProgramResourceType( Product product, String resourceBundleKey ) {
		super( product, resourceBundleKey );
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

}
