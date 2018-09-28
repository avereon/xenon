package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.ResourceType;

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
