package com.parallelsymmetry.essence.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MockCodec extends Codec {

	public static final String EXTENSION = "mock";

	private static Set<String> supportedFileNames;

	public MockCodec() {
		addSupportedExtension( EXTENSION );
	}

	@Override
	public String getKey() {
		return "com.parallelsymmetry.codec.mock";
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
