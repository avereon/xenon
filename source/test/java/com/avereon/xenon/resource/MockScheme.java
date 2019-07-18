package com.avereon.xenon.resource;

import com.avereon.xenon.Program;
import com.avereon.xenon.scheme.BaseScheme;

public class MockScheme extends BaseScheme {

	public MockScheme( Program program ) {
		super( program );
	}

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public boolean exists( Resource resource ) {
		return true;
	}

}
