package com.avereon.xenon.asset.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramResourceNewType extends ResourceType {

	private static final String uriPattern = XenonScheme.ID + ":/new";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramResourceNewType( XenonProgramProduct product ) {
		super( product, "asset-new" );

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
