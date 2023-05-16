package com.avereon.xenon.asset.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.ContentCodec;

public class ProgramHelpType extends AssetType {

	public static final String SCHEME = "program:help";

	public ProgramHelpType( XenonProgramProduct product ) {
		super( product, "help" );

		Codec codec = new ContentCodec();
		codec.addSupported( Codec.Pattern.SCHEME, SCHEME );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return SCHEME;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

}
