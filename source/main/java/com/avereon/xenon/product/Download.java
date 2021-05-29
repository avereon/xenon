package com.avereon.xenon.product;

import com.avereon.util.FileUtil;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;

/**
 * This class represents content that has been downloaded, either to a specific
 * file or to a temporary file. If to a temporary file, the file is marked to
 * be removed on JVM exit.
 */
public class Download extends OutputStream {

	private final URI source;

	private final int length;

	private final String encoding;

	private Path target;

	private OutputStream output;

	public Download( URI source, int length, String encoding ) {
		this( source, length, encoding, null );
	}

	public Download( URI source, int length, String encoding, Path target ) {
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

	public Path getTarget() {
		return target;
	}

	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream( target.toFile() );
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
		if( output == null ) {
			if( target == null ) {
				target = FileUtil.createTempFile( "download", "data" );
				target.toFile().deleteOnExit();
			}
			output = new BufferedOutputStream( new FileOutputStream( target.toFile() ) );
		}
		return output;
	}

}
