package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramGuideType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:guide" );

	public ProgramGuideType( Product product ) {
		super( product, "guide" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	// This asset type does not have a codec
	public Codec getDefaultCodec() {
		return null;
	}

	// This asset type does not have a codec
	public void setDefaultCodec( Codec codec ) {}

}
