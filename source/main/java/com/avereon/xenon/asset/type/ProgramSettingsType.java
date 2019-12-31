package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetType;

public class ProgramSettingsType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:settings" );

	public ProgramSettingsType( Product product ) {
		super( product, "settings" );
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
