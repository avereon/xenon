package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

public class ProgramAssetOpenType extends AssetType {

	private static final String uriPattern = "program:asset:open";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramAssetOpenType( ProgramProduct product ) {
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
