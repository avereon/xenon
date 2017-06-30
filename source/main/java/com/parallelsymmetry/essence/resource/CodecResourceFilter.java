package com.parallelsymmetry.essence.resource;

public class CodecResourceFilter implements ResourceFilter {

	private Codec codec;

	public CodecResourceFilter( Codec codec ) {
		if( codec == null ) throw new NullPointerException( "Codec cannot be null" );
		this.codec = codec;
	}

	@Override
	public String getDescription() {
		return codec.getName();
	}

	@Override
	public boolean accept( Resource resource ) {
		if( resource == null ) return false;
		return codec.isSupportedFileName( resource.getName() );
	}

}
