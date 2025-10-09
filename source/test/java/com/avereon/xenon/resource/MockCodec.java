package com.avereon.xenon.resource;

import com.avereon.xenon.scheme.XenonScheme;

import java.io.InputStream;
import java.io.OutputStream;

public class MockCodec extends Codec {

	public static final java.net.URI URI = java.net.URI.create( XenonScheme.ID + ":/mock" );

	static final String EXTENSION = "mock";

	private final String key;

	public MockCodec() {
		this( null );
	}

	public MockCodec( String key ) {
		this.key = key;
		addSupported( Pattern.URI, "mock:test" );
		addSupported( Pattern.URI, XenonScheme.ID + ":/mock" );
		addSupported( Pattern.SCHEME, "mock" );
		addSupported( Pattern.MEDIATYPE, "application/mock" );
		addSupported( Pattern.EXTENSION, EXTENSION );
		addSupported( Pattern.FILENAME, "^.*\\." + EXTENSION + "$" );
		addSupported( Pattern.FIRSTLINE, "?mock" );
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
	public void load( Resource resource, InputStream input ) {}

	@Override
	public void save( Resource resource, OutputStream output ) {}

}
