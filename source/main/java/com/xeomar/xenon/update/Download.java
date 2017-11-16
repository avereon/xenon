package com.xeomar.xenon.update;

import java.io.*;
import java.net.URI;

public class Download extends OutputStream {

	private URI source;

	private int length;

	private String encoding;

	private File target;

	private OutputStream output;

	public Download( URI source, int length, String encoding ) {
		this( source, length, encoding, null );
	}

	public Download( URI source, int length, String encoding, File target ) {
		this.source = source;
		this.length = length;
		this.encoding = encoding;
		this.target = target;
	}

	public URI getSource() {
		return source;
	}

	public int getLength() {
		return length;
	}

	public String getEncoding() {
		return encoding;
	}

	public File getTarget() {
		return target;
	}

	@Override
	public void write( int data ) throws IOException {
		getOutputStream().write( data );
	}

	@Override
	public void write( byte[] data ) throws IOException {
		getOutputStream().write( data );
	}

	@Override
	public void write( byte[] data, int offset, int length ) throws IOException {
		getOutputStream().write( data, offset, length );
	}

	@Override
	public void flush() throws IOException {
		getOutputStream().flush();
	}

	@Override
	public void close() throws IOException {
		getOutputStream().close();
	}

	private OutputStream getOutputStream() throws IOException {
		if( target == null ) {
			target = File.createTempFile( "download", "data" );
			target.deleteOnExit();
		}
		if( output == null ) {
			output = new BufferedOutputStream( new FileOutputStream( target ) );
		}
		return output;
	}

}
