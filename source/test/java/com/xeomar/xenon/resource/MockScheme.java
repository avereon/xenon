package com.xeomar.xenon.resource;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.scheme.BaseScheme;

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
