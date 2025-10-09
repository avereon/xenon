package com.avereon.xenon.resource.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.ContentCodec;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramHelpType extends ResourceType {

	private static final String uriPattern = XenonScheme.ID + ":/help";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramHelpType( XenonProgramProduct product ) {
		super( product, "help" );

		Codec codec = new ContentCodec();
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
