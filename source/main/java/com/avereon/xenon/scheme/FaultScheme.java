package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.exception.AssetException;

public class FaultScheme extends ProgramScheme {

	public static final String ID = "fault";

	public FaultScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return true;
	}

}
