package com.avereon.xenon.asset.type;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramSearchType extends ResourceType {

	private static final String mediaTypePattern = BASE_MEDIA_TYPE + ".index.search";

	private static final String uriPattern = XenonScheme.ID + ":/index-search";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramSearchType( XenonProgramProduct product ) {
		super( product, "index-search" );
		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		codec.addSupported( Codec.Pattern.MEDIATYPE, mediaTypePattern );
		setDefaultCodec( codec );
	}

	@Override
	public boolean assetOpen( Xenon program, Resource resource ) throws ResourceException {
		return true;
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
