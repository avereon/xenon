package com.avereon.xenon.asset.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.scheme.FaultScheme;

public class ProgramFaultType extends AssetType {

	private static final String uriPattern = "program:/fault";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramFaultType( XenonProgramProduct product ) {
		super( product, "fault" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.SCHEME, FaultScheme.ID );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

}
