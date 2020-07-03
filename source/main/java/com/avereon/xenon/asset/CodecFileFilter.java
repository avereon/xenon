package com.avereon.xenon.asset;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;

/**
 * @author Mark Soderquist
 */
public class CodecFileFilter implements DirectoryStream.Filter<Path> {

	private final Codec codec;

	public CodecFileFilter( Codec codec ) {
		this.codec = codec;
	}

	public Codec getCodec() {
		return codec;
	}

	@Override
	public boolean accept( Path path ) {
		return codec.isSupported( Codec.Pattern.FILENAME, path.getFileName().toString() );
	}

}
