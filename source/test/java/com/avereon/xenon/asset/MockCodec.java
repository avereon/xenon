package com.avereon.xenon.asset;

import java.io.InputStream;
import java.io.OutputStream;

public class MockCodec extends Codec {

	static final String EXTENSION = "mock";

	private String key;

	MockCodec() {
		this( null );
	}

	MockCodec( String key ) {
		this.key = key;
		addSupportedMediaType( "application/mock" );
		addSupportedExtension( EXTENSION );
		addSupportedFirstLine( "?mock" );
	}

	@Override
	public String getKey() {
		return key == null ? "com.parallelsymmetry.codec.mock" : "com.parallelsymmetry.codec.mock." + key;
	}

	@Override
	public String getName() {
		return "Mock Asset (*." + EXTENSION + ")";
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
