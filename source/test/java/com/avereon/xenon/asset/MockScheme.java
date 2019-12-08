package com.avereon.xenon.asset;

import com.avereon.xenon.Program;
import com.avereon.xenon.scheme.BaseScheme;

public class MockScheme extends BaseScheme {

	MockScheme( Program program ) {
		super( program );
	}

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

}
