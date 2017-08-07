package com.xeomar.xenon.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MockCodec extends Codec {

	public static final String EXTENSION = "mock";

	private String key;

	public MockCodec() {
		this( null );
	}

	public MockCodec(String key) {
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
		return "Mock Resource (*." + EXTENSION + ")";
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
	public void load( Resource resource, InputStream input ) throws IOException {}

	@Override
	public void save( Resource resource, OutputStream output ) throws IOException {}

}
