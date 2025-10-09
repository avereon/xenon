package com.avereon.xenon.resource.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.PlaceholderCodec;
import com.avereon.xenon.scheme.FaultScheme;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramFaultType extends ResourceType {

	private static final String uriPattern = XenonScheme.ID + ":/fault";

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
