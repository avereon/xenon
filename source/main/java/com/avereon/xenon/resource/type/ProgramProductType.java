package com.avereon.xenon.resource.type;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceException;
import com.avereon.xenon.resource.ResourceType;

public class ProgramProductType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:product" );

	public ProgramProductType( Product product ) {
		super( product, "product" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		resource.setModel( program.getCard() );
		return true;
	}

	/**
	 * There are no codecs for this resource type so this method always returns null.
	 *
	 * @return null
	 */
	@Override
	public Codec getDefaultCodec() {
		return null;
	}

}
