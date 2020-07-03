package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

/**
 * Represents a bitmap image.
 */
public class BitmapImageType extends AssetType {

	public BitmapImageType( ProgramProduct product ) {
		super( product, "image" );

		// TODO Replace this placeholder codec with actual codecs
		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.EXTENSION, "png" );
		codec.addSupported( Codec.Pattern.EXTENSION, "jpg" );
		codec.addSupported( Codec.Pattern.EXTENSION, "jpeg" );
		codec.addSupported( Codec.Pattern.EXTENSION, "gif" );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return "image:bitmap";
	}

}
