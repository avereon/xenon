package com.avereon.xenon.asset;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;

public class MockAssetType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:mock" );

	private String key;

	private static final String NAME = "Mock Asset";

	private static final String DESCRIPTION = "Mock Asset Type";

	public MockAssetType( ProgramProduct product ) {
		this( product, "mock", new MockCodec() );
	}

	public MockAssetType( ProgramProduct product, String key, Codec defaultCodec ) {
		super( product, key );
		this.key = key;
		setDefaultCodec( defaultCodec );
	}

	@Override
	public String getKey() {
		return key == null ? super.getKey() : key;
	}

	@Override
	public String getName() {
		return NAME + " (" + getKey() + ")";
	}

	@Override
	public String getDescription() {
		return DESCRIPTION + " (" + getKey() + ")";
	}

	@Override
	public boolean assetInit( Program program, Asset asset ) {
		return true;
	}

}
