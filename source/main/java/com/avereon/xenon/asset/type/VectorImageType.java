package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

/**
 * Represents a vector image.
 */
public class VectorImageType extends AssetType {

	public VectorImageType( ProgramProduct product ) {
		super( product, "image" );

		// TODO Replace this placeholder codec with actual codecs
		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.EXTENSION, "svg" );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return "image:vector";
	}

}