package com.avereon.xenon.resource.type;

import com.avereon.product.Product;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.ResourceType;

public class ProgramWelcomeType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:welcome" );

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
