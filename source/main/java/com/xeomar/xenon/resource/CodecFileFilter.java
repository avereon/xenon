package com.xeomar.xenon.resource;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Mark Soderquist
 */
public class CodecFileFilter extends FileFilter {

	private Codec codec;

	public CodecFileFilter( Codec codec ) {
		this.codec = codec;
	}

	public Codec getCodec() {
		return codec;
	}

	@Override
	public String getDescription() {
		return codec.getName();
	}

	@Override
	public boolean accept( File file ) {
		return codec.isSupportedFileName( file.getName() );
	}

}
