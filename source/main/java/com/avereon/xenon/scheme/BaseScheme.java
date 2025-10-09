package com.avereon.xenon.scheme;

import com.avereon.util.LimitedInputStream;
import com.avereon.util.TextUtil;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.resource.Scheme;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseScheme implements Scheme {

	// Linux defines this limit in BINPRM_BUF_SIZE
	private static final int FIRST_LINE_LIMIT = 128;

	@Getter
	protected final Xenon program;

	private final String id;

	public BaseScheme( Xenon program, String id ) {
		this.program = program;
		this.id = id;
	}

	@Override
	public String getName() {
		return id;
	}

	protected String readFirstLine( InputStream input, String encoding ) throws IOException {
		if( input == null ) return TextUtil.EMPTY;

		// TODO Maybe implement a FirstLineInputStream and move this logic there
		LimitedInputStream boundedInput = new LimitedInputStream( input, FIRST_LINE_LIMIT );
		ByteArrayOutputStream output = new ByteArrayOutputStream( FIRST_LINE_LIMIT );
		byte[] buffer = new byte[ FIRST_LINE_LIMIT ];

		int read;
		while( (read = boundedInput.read( buffer )) > -1 ) {
			// Search for line termination
			boolean eol = false;
			for( int index = 0; index < read; index++ ) {
				int data = buffer[ index ];
				if( data == 10 || data == 13 ) {
					read = index;
					eol = true;
					break;
				}
			}

			// Write the buffer
			output.write( buffer, 0, read );

			// If a line break was encountered stop
			if( eol ) break;
		}

		if( encoding == null ) encoding = StandardCharsets.UTF_8.name();
		return TextUtil.cleanEmpty( output.toString( encoding ) );
	}

}
