package com.avereon.xenon.asset;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.scheme.BaseScheme;

public class MockScheme extends BaseScheme {

	public static final String ID = "mock";

	MockScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Resource resource ) {
		return true;
	}

	@Override
	public boolean canLoad( Resource resource ) {
		return true;
	}

	@Override
	public boolean canSave( Resource resource ) {
		return true;
	}

}
