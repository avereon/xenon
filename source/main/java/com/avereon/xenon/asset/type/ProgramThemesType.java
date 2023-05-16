package com.avereon.xenon.asset.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

@Deprecated
public class ProgramThemesType extends AssetType {

	private static final String uriPattern = "program:/themes";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramThemesType( XenonProgramProduct product ) {
		super( product, "themes" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
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
