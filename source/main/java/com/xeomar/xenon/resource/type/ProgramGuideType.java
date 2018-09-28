package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.ResourceType;

public class ProgramGuideType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:guide" );

	public ProgramGuideType( Product product ) {
		super( product, "guide" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	// This resource type does not have a codec
	public Codec getDefaultCodec() {
		return null;
	}

	// This resource type does not have a codec
	public void setDefaultCodec( Codec codec ) {}

}
