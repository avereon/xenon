package com.avereon.xenon.asset;

import com.avereon.product.Product;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A special codec that indicates the real codec should be autodetected.
 */
public class AutoDetectCodec extends Codec {

	private final Product product;

	public AutoDetectCodec( Product product ) {
		this.product = product;
	}

	@Override
	public String getKey() {
		return "codec.autodetect";
	}

	@Override
	public String getName() {
		return product.rb().text( "labels", "auto.detect" );
	}

	@Override
	public boolean canLoad() {
		return false;
	}

	@Override
	public boolean canSave() {
		return false;
	}

	@Override
	public void load( Asset asset, InputStream input ) {}

	@Override
	public void save( Asset asset, OutputStream output ) {}

}
