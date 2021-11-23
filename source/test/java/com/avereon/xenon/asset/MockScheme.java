package com.avereon.xenon.asset;

import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.scheme.BaseScheme;

public class MockScheme extends BaseScheme {

	public static final String ID = "mock";

	MockScheme( Program program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

}
