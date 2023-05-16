package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;

public class NewScheme extends BaseScheme {

	public static final String ID = "new";

	public NewScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return true;
	}

	@Override
	public boolean canSave( Asset asset ) throws AssetException {
		return true;
	}

}
