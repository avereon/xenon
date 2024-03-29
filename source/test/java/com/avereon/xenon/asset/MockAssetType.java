package com.avereon.xenon.asset;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;

public class MockAssetType extends AssetType {

	private static final String NAME = "Mock Asset";

	private static final String DESCRIPTION = "Mock Asset Type";

	private final String key;

	public MockAssetType( XenonProgramProduct product ) {
		this( product, "mock", new MockCodec() );
	}

	public MockAssetType( XenonProgramProduct product, String key, Codec defaultCodec ) {
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
	public boolean assetOpen( Xenon program, Asset asset ) {
		return true;
	}

}
