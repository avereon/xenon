package com.avereon.xenon.resource;

import com.avereon.product.Product;
import com.avereon.product.Rb;

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
		return Rb.text( "labels", "auto.detect" );
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
	public void load( Resource resource, InputStream input ) {}

	@Override
	public void save( Resource resource, OutputStream output ) {}

}
