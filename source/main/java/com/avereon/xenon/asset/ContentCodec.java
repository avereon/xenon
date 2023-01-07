package com.avereon.xenon.asset;

import com.avereon.util.IoUtil;
import com.avereon.util.TextUtil;
import lombok.CustomLog;

import java.io.*;
import java.nio.charset.StandardCharsets;

@CustomLog
public class ContentCodec extends Codec {

	@Override
	public String getKey() {
		return "content";
	}

	@Override
	public String getName() {
		return "content";
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return false;
	}

	@Override
	public void load( Asset asset, InputStream input ) {
		try {
			String content = IoUtil.toString( input, StandardCharsets.UTF_8 );
			if( TextUtil.isEmpty( content ) ) log.atWarn().log( "No content loaded!" );
			asset.setModel( content );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

	@Override
	public void save( Asset asset, OutputStream output ) {}

}
