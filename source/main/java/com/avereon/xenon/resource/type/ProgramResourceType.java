package com.avereon.xenon.resource.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.PlaceholderCodec;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramResourceType extends ResourceType {

	private static final String uriPattern = XenonScheme.ID + ":/asset";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public static final String MODE_OPEN = "?mode=open";

	public static final String MODE_SAVE = "?mode=save";
	
	public static final java.net.URI OPEN_URI = java.net.URI.create( URI + MODE_OPEN );

	public static final java.net.URI SAVE_URI = java.net.URI.create( URI + MODE_SAVE );

	public ProgramResourceType( XenonProgramProduct product ) {
		super( product, "asset-open" );

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
