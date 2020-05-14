package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramAssetNewType extends AssetType {

	// FIXME Media type is defined in the codec
	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.asset.new";

	public static final java.net.URI URI = java.net.URI.create( "program:asset:new" );

	public ProgramAssetNewType( ProgramProduct product ) {
		super( product, "asset-new" );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
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
