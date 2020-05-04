package com.avereon.xenon.asset.type;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.product.ModEvent;

import java.lang.System.Logger;

public class ProgramAboutType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.about";

	public static final java.net.URI URI = java.net.URI.create( "program:about" );

	private static final Logger log = Log.get();

	public ProgramAboutType( ProgramProduct product ) {
		super( product, "about" );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	/**
	 * There are no codecs for this asset type so this method always returns null.
	 *
	 * @return null
	 */
	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	@Override
	public boolean assetInit( Program program, Asset asset ) {
		asset.setModel( getProduct().getCard() );

		program.register( ModEvent.ENABLED, e -> asset.refresh() );
		program.register( ModEvent.DISABLED, e -> asset.refresh() );

		return true;
	}

}
