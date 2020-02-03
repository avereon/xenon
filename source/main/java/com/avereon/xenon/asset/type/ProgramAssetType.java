package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramAssetType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:asset" );

	public ProgramAssetType( Product product ) {
		super( product, "asset" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

}
