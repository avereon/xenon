package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.scheme.BaseScheme;

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
