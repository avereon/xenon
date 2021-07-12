package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

public class ProgramAssetType extends AssetType {

	public static final String URI = "program:asset";

	public static final String OPEN_FRAGMENT = "#open";

	public static final String SAVE_FRAGMENT = "#save";

	public static final java.net.URI OPEN_URI = java.net.URI.create( URI + OPEN_FRAGMENT );

	public static final java.net.URI SAVE_URI = java.net.URI.create( URI + SAVE_FRAGMENT );

	public ProgramAssetType( ProgramProduct product ) {
		super( product, "asset-open" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, URI );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return URI;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

}
