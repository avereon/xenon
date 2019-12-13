package com.avereon.xenon.asset;

import com.avereon.product.Product;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class AutoDetectCodec extends Codec {

	private Product product;

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
	public void load( Asset asset, InputStream input ) throws IOException {}

	@Override
	public void save( Asset asset, OutputStream output ) throws IOException {}

	@Override
	public Set<String> getSupportedFileNames() {
		return null;
	}

	@Override
	public Set<String> getSupportedFirstLines() {
		return null;
	}

	@Override
	public Set<String> getSupportedMediaTypes() {
		return null;
	}

}