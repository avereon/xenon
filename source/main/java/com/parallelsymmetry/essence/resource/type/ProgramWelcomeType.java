package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.ResourceType;

public class ProgramWelcomeType extends ResourceType {

	public ProgramWelcomeType( Product product ) {
		super( product, "welcome" );
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
