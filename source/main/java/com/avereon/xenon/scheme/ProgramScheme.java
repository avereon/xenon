package com.avereon.xenon.scheme;

import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.AssetException;

import java.io.IOException;

public class ProgramScheme extends BaseScheme {

	public static final String ID = "program";

	public ProgramScheme( Program program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		if( codec != null ) {
			try {
				codec.load( asset, null );
			} catch( IOException exception ) {
				throw new AssetException( asset,  "Unable to load " + asset.getUri(), exception );
			}
		}
	}

	@Override
	public void save( Asset asset, Codec codec ) throws AssetException {
		if( codec != null ) {
			try {
				codec.save( asset, null );
			} catch( IOException exception ) {
				throw new AssetException( asset,  "Unable to save asset", exception );
			}
		}
	}

}
