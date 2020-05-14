package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.scheme.FaultScheme;

public class ProgramFaultType extends AssetType {

	public ProgramFaultType( ProgramProduct product ) {
		super( product, "fault" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.SCHEME, FaultScheme.ID );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return "program:fault";
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
