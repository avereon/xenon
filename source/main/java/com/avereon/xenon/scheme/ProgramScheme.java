package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.exception.AssetException;

import java.io.IOException;

public abstract class ProgramScheme extends BaseScheme {

	public ProgramScheme( Xenon program, String id ) {
		super( program, id );
	}

	@Override
	public boolean exists( Asset asset ) throws AssetException {
		return true;
	}


	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		if( codec != null ) {
			try {
				codec.load( asset, null );
			} catch( IOException exception ) {
				throw new AssetException( asset, "Unable to load " + asset.getUri(), exception );
			}
		}
	}

	@Override
	public void save( Asset asset, Codec codec ) throws AssetException {
		if( codec != null ) {
			try {
				codec.save( asset, null );
			} catch( IOException exception ) {
				throw new AssetException( asset, "Unable to save asset", exception );
			}
		}
	}

}
