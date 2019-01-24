package com.xeomar.xenon.resource;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;

/**
 * @author Mark Soderquist
 */
public class CodecFileFilter implements DirectoryStream.Filter<Path> {

	private Codec codec;

	public CodecFileFilter( Codec codec ) {
		this.codec = codec;
	}

	public Codec getCodec() {
		return codec;
	}

	@Override
	public boolean accept( Path path ) {
		return codec.isSupportedFileName( path.getFileName().toString() );
	}

}
