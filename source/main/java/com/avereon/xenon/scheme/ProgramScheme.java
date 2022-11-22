package com.avereon.xenon.scheme;

import com.avereon.index.Document;
import com.avereon.util.IoUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.Codec;
import lombok.CustomLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@CustomLog
public class ProgramScheme extends ProductScheme {

	public static final String ID = "program";

	public ProgramScheme( Program program ) {
		super( program, ID );
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return true;
	}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		// Most assets don't actually load anything

		// However, the following do

		// Help content
		URI uri = URI.create( asset.getUri().getSchemeSpecificPart() );
		if( uri.getScheme() != null ) {
			switch( uri.getScheme() ) {
				case "help" -> loadHelp( asset, codec );
			}
		}
	}

	private void loadHelp( Asset asset, Codec codec ) {
		URI uri = asset.getUri();
		//log.atConfig().log( "help uri=%s", uri );

		String content;
		try {
			Document document = getProgram().getIndexService().lookup( uri );
			if( document == null ) {
				log.atWarn().log( "Document not found: doc=%s", uri );
				content = "<html><body></body></html>";
			} else {
				content = IoUtil.toString( document.reader() );
			}
		} catch( Exception exception ) {
			content = "<html><body></body></html>";
		}

		try {
			codec.load( asset, new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) ) );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

}
