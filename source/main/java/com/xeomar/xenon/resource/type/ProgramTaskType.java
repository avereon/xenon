package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.ResourceType;

import java.net.URI;

public class ProgramTaskType extends ResourceType {

	public static final URI uri = URI.create( "program:task" );

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
