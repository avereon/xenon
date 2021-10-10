package com.avereon.xenon.asset;

import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class CodecAssetFilter implements AssetFilter {

	private final Codec codec;

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
		try {
			if( asset == null ) return false;
			if( asset.isFolder() ) return true;
			return codec.isSupported( asset );
		} catch( Exception exception ) {
			log.atWarn( exception ).log();
			return false;
		}
	}

	@Override
	public String toString() {
		List<String> patterns = new ArrayList<>();
		patterns.addAll( codec.getSupported( Codec.Pattern.EXTENSION ).stream().map( p -> "*." + p ).sorted().collect( Collectors.toList() ) );
		patterns.addAll( codec.getSupported( Codec.Pattern.FILENAME ).stream().sorted().collect( Collectors.toList() ) );

		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for( String pattern : patterns ) {
			if( !isFirst ) builder.append( ", " );
			builder.append( pattern );
			isFirst = false;
		}
		return getDescription() + " (" + builder + ")";
	}

}
