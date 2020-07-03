package com.avereon.xenon.scheme;

import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;

public class FaultScheme extends BaseScheme {

	public static final String ID = "fault";

	public FaultScheme( Program program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

}
