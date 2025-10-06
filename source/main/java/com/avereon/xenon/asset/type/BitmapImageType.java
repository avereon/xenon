package com.avereon.xenon.asset.type;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;

/**
 * Represents a bitmap image.
 */
public class BitmapImageType extends ResourceType {

	private static final String assetTypeKey = "image:bitmap";

	private static final String gifMediaTypePattern = "image/gif";

	private static final String jpgMediaTypePattern = "image/jpg";

	private static final String jpegMediaTypePattern = "image/jpeg";

	private static final String pngMediaTypePattern = "image/png";

	public BitmapImageType( XenonProgramProduct product ) {
		super( product, "image" );

		// TODO Replace this placeholder codec with actual codecs
		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.EXTENSION, "gif" );
		codec.addSupported( Codec.Pattern.EXTENSION, "jpg" );
		codec.addSupported( Codec.Pattern.EXTENSION, "jpeg" );
		codec.addSupported( Codec.Pattern.EXTENSION, "png" );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return assetTypeKey;
	}

}
