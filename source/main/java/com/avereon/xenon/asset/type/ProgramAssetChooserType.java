package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

public class ProgramAssetChooserType extends AssetType {

	private static final String uriPattern = "program:asset";

	private static final String openUriPattern = uriPattern + "#open";

	private static final String saveUriPattern = uriPattern + "#save";

	public static final java.net.URI OPEN_URI = java.net.URI.create( openUriPattern );

	public static final java.net.URI SAVE_URI = java.net.URI.create( saveUriPattern );

	public ProgramAssetChooserType( ProgramProduct product ) {
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
