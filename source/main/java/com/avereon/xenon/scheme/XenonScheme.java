package com.avereon.xenon.scheme;

import com.avereon.index.Document;
import com.avereon.product.Rb;
import com.avereon.util.IoUtil;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.exception.ResourceException;
import lombok.CustomLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@CustomLog
public class XenonScheme extends ProgramScheme {

	public static final String ID = "xenon";

	public XenonScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean canLoad( Asset asset ) throws ResourceException {
		return true;
	}

	@Override
	public void load( Asset asset, Codec codec ) throws ResourceException {
		// Most program assets don't actually load anything
		// However, the following do:

		// Help content
		URI uri = asset.getUri();
		if( uri.getScheme().equals( ID ) ) {
			if( uri.getSchemeSpecificPart().equals( "help" ) ) {
				loadHelp( asset, codec );
			}
		}
	}

	private void loadHelp( Asset asset, Codec codec ) {
		URI uri = asset.getUri();

		String content;
		try {
			Document document = getProgram().getIndexService().lookupFromCache( uri );
			if( document == null ) {
				log.atWarn().log( "Document not found: doc=%s", uri );
				String message = Rb.text( "program", "help-document-not-found" );
				content = "<html><body>" + message + "</body></html>";
			} else {
				content = IoUtil.toString( document.reader() );
			}
		} catch( Exception exception ) {
			String message = Rb.text( "program", "error-loading-help-content" );
			content = "<html><body>" + message + "</body></html>";
			log.atError( exception ).log();
		}

		try {
			codec.load( asset, new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) ) );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

}
