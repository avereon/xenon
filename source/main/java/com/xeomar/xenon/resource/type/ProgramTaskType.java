package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.ResourceType;

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
