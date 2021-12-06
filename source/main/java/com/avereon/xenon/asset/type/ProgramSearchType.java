package com.avereon.xenon.asset.type;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.*;

public class ProgramSearchType extends AssetType {

	private static final String mediaTypePattern = BASE_MEDIA_TYPE + ".index.search";

	private static final String uriPattern = "program:/index-search";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramSearchType( ProgramProduct product ) {
		super( product, "index-search" );
		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		codec.addSupported( Codec.Pattern.MEDIATYPE, mediaTypePattern );
		setDefaultCodec( codec );
	}

	@Override
	public boolean assetOpen( Program program, Asset asset ) throws AssetException {
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
