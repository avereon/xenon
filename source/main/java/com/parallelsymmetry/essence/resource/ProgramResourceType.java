package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.product.Product;

public class ProgramResourceType extends ResourceType {

	public ProgramResourceType( Product product, String resourceBundleKey ) {
		super( product, resourceBundleKey );
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		// Depending on the URI path make the proper data model
		String path = resource.getUri().getPath();

		switch( path  ) {
			case "about" : {
				break;
			}
			case "settings" : {
				break;
			}
		}

		return true;
	}
}
