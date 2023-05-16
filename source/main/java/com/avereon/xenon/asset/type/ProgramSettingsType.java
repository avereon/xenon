package com.avereon.xenon.asset.type;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.asset.*;

public class ProgramSettingsType extends AssetType {

	private static final String uriPattern = "program:/settings";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramSettingsType( ProgramProduct product ) {
		super( product, "settings" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public boolean assetOpen( Xenon program, Asset asset ) {
		asset.setModel( program.getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		return true;
	}

}
