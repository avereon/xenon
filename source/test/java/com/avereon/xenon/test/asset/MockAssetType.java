package com.avereon.xenon.test.asset;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;

public class MockAssetType extends AssetType {

	private static final String NAME = "Mock Asset";

	private static final String DESCRIPTION = "Mock Asset Type";

	private final String key;

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
	public boolean assetOpen( Program program, Asset asset ) {
		return true;
	}

}
