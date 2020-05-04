package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramFaultType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.fault";

	public static final java.net.URI URI = java.net.URI.create( "program:fault" );

	public ProgramFaultType( ProgramProduct product ) {
		super( product, "fault" );
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
