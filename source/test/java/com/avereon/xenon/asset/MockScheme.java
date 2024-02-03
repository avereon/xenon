package com.avereon.xenon.asset;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.scheme.BaseScheme;

public class MockScheme extends BaseScheme {

	public static final String ID = "mock";

	MockScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return true;
	}

	@Override
	public boolean canLoad( Asset asset ) {
		return true;
	}

	@Override
	public boolean canSave( Asset asset ) {
		return true;
	}

}
