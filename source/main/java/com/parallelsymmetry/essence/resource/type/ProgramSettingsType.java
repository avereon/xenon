package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.ResourceType;

public class ProgramSettingsType extends ResourceType {

	public ProgramSettingsType( Product product ) {
		super( product, "settings" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

}
