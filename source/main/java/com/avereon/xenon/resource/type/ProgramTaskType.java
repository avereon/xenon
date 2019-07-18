package com.avereon.xenon.resource.type;

import com.avereon.product.Product;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.ResourceType;

public class ProgramTaskType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:task" );

	public ProgramTaskType( Product product ) {
		super( product, "task" );
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
