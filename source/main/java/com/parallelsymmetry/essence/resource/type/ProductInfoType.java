package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceException;
import com.parallelsymmetry.essence.resource.ResourceType;

public class ProductInfoType extends ResourceType {

	public ProductInfoType( Product product ) {
		super( product, "product-info" );
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		// FIXME Should this really go in the load method of the codec?
		resource.setModel( getProduct().getMetadata() );
		return true;
	}

}
