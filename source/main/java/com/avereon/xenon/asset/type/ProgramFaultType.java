package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramFaultType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:fault" );

	public ProgramFaultType( Product product ) {
		super( product, "fault" );
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
