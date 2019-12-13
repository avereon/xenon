package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.AssetType;

public class ProgramTaskType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:task" );

	public ProgramTaskType( Product product ) {
		super( product, "task" );
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