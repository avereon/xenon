package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.ResourceType;

public class ProgramSettingsType extends ResourceType {

	public ProgramSettingsType( Product product, String resourceBundleKey ) {
		super( product, resourceBundleKey );
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

}
