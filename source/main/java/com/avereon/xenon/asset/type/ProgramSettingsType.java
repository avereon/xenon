package com.avereon.xenon.asset.type;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class ProgramSettingsType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.settings";

	public static final java.net.URI URI = java.net.URI.create( "program:settings" );

	public ProgramSettingsType( ProgramProduct product ) {
		super( product, "settings" );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	/**
	 * @see #isUserType()
	 */
	@Override
	public boolean isUserType() {
		return false;
	}

	/**
	 * @see #getDefaultCodec()
	 */
	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	/**
	 * @see #assetInit(Program, Asset)
	 */
	@Override
	public boolean assetInit( Program program, Asset asset ) throws AssetException {
		asset.setModel( program.getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		return true;
	}

}
