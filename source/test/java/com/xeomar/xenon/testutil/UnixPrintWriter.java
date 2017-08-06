package com.xeomar.xenon.testutil;

import java.io.*;

/**
 * Created by SoderquistMV on 4/7/2017.
 */
public class UnixPrintWriter extends PrintWriter {

	public UnixPrintWriter( Writer writer ) {
		super( writer );
	}

	public UnixPrintWriter( Writer writer, boolean autoFlush ) {
		super( writer, autoFlush );
	}

	public UnixPrintWriter( OutputStream writer ) {
		super( writer );
	}

	public UnixPrintWriter( OutputStream writer, boolean autoFlush ) {
		super( writer, autoFlush );
	}

	public UnixPrintWriter( String fileName ) throws FileNotFoundException {
		super( fileName );
	}

	public UnixPrintWriter( String fileName, String charset ) throws FileNotFoundException, UnsupportedEncodingException {
		super( fileName, charset );
	}

	public UnixPrintWriter( File file ) throws FileNotFoundException {
		super( file );
	}

	public UnixPrintWriter( File file, String charset ) throws FileNotFoundException, UnsupportedEncodingException {
		super( file, charset );
	}

	@Override
	public void println() {
		write( '\n' );
	}
}
