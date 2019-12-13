package com.avereon.xenon.asset;

public class CodecAssetFilter implements AssetFilter {

	private Codec codec;

	public CodecAssetFilter( Codec codec ) {
		if( codec == null ) throw new NullPointerException( "Codec cannot be null" );
		this.codec = codec;
	}

	@Override
	public String getDescription() {
		return codec.getName();
	}

	@Override
	public boolean accept( Asset asset ) {
		if( asset == null ) return false;
		return codec.isSupportedFileName( asset.getName() );
	}

}