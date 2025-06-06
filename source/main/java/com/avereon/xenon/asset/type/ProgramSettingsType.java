package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramSettingsType extends AssetType {

	private static final String uriPattern = XenonScheme.ID + ":/settings";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public static final java.net.URI UPDATES = java.net.URI.create( uriPattern + "#modules-updates" );

	public ProgramSettingsType( XenonProgramProduct product ) {
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
