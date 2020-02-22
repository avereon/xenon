package com.avereon.xenon.asset;

import com.avereon.xenon.Program;
import com.avereon.xenon.scheme.BaseScheme;

public class MockScheme extends BaseScheme {

	public static final String ID = "mock";

	MockScheme( Program program ) {
		super( program );
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

}
