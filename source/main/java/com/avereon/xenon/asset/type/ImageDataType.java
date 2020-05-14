package com.avereon.xenon.asset.type;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.AssetType;

/**
 * Represents a bitmap image.
 */
public class ImageDataType extends AssetType {

	// FIXME Media type is defined in the codec
	private static final String MEDIA_TYPE = "image";

	public ImageDataType( ProgramProduct product ) {
		super( product, "image" );
	}

}
