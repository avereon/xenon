package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramThemesType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.themes";

	public static final java.net.URI URI = java.net.URI.create( "program:themes" );

	public ProgramThemesType( ProgramProduct product ) {
		super( product, "themes" );
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
