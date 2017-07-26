package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.ResourceType;

public class ProgramGuideType extends ResourceType {

	public static final String URI = "program:guide";

	public ProgramGuideType( Product product ) {
		super( product, "guide" );
	}

	// This resource type does not have a codec
	public Codec getDefaultCodec() {
		return null;
	}

	// This resource type does not have a codec
	public void setDefaultCodec( Codec codec ) {}

}
