package com.avereon.xenon.test.asset;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;

import java.io.InputStream;
import java.io.OutputStream;

public class MockCodec extends Codec {

	public static final java.net.URI URI = java.net.URI.create( "program:/mock" );

	static final String EXTENSION = "mock";

	private final String key;

	MockCodec() {
		this( null );
	}

	MockCodec( String key ) {
		this.key = key;
		addSupported( Pattern.URI, "mock:test" );
		addSupported( Pattern.URI, "program:/mock" );
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
	public void load( Asset asset, InputStream input ) {}

	@Override
	public void save( Asset asset, OutputStream output ) {}

}
