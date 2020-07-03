package com.avereon.xenon.asset.type;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.*;

public class ProgramProductType extends AssetType {

	private static final String uriPattern = "program:product";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramProductType( ProgramProduct product ) {
		super( product, "product" );

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

	@Override
	public boolean assetInit( Program program, Asset asset ) {
		asset.setModel( program.getCard() );
		return true;
	}

}
