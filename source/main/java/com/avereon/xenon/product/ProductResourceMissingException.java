package com.avereon.xenon.product;

import java.io.IOException;

public class ProductResourceMissingException extends IOException {

	private ProductResource resource;

	public ProductResourceMissingException( String message, ProductResource resource ) {
		super( message );
		this.resource = resource;
	}

	public ProductResource getResource() {
		return resource;
	}

	public void setResource( ProductResource resource ) {
		this.resource = resource;
	}

}
