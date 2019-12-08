package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.AssetException;

public class ProgramAboutType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:about" );

	public ProgramAboutType( Product product ) {
		super( product, "about" );
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
	public boolean assetDefault( Program program, Asset asset ) throws AssetException {
		asset.setModel( getProduct().getCard() );
		return true;
	}

}
