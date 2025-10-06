package com.avereon.xenon.asset.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.scheme.XenonScheme;

@Deprecated
public class ProgramThemesType extends ResourceType {

	private static final String uriPattern = XenonScheme.ID + ":/themes";

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
