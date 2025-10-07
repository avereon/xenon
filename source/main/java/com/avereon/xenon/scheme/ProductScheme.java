package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.asset.Codec;

import java.io.IOException;

public abstract class ProductScheme extends BaseScheme {

	public ProductScheme( Xenon program, String id ) {
		super( program, id );
	}

	@Override
	public boolean exists( Resource resource ) {
		return true;
	}

	@Override
	public void load( Resource resource, Codec codec ) throws ResourceException {
		if( codec != null ) {
			try {
				codec.load( resource, null );
			} catch( IOException exception ) {
				throw new ResourceException( resource, "Unable to load " + resource.getUri(), exception );
			}
		}
	}

	@Override
	public void save( Resource resource, Codec codec ) throws ResourceException {
		if( codec != null ) {
			try {
				codec.save( resource, null );
			} catch( IOException exception ) {
				throw new ResourceException( resource, "Unable to save asset", exception );
			}
		}
	}

}
