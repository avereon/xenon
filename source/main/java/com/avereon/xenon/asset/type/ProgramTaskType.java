package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramTaskType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.task";

	public static final java.net.URI URI = java.net.URI.create( "program:task" );

	public ProgramTaskType( ProgramProduct product ) {
		super( product, "task" );
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
