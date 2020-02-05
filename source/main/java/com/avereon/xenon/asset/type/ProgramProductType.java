package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.AssetException;

public class ProgramProductType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.product";

	public static final java.net.URI URI = java.net.URI.create( "program:product" );

	public ProgramProductType( Product product ) {
		super( product, "product" );
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
	public boolean assetInit( Program program, Asset asset ) throws AssetException {
		asset.setModel( program.getCard() );
		return true;
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

}
