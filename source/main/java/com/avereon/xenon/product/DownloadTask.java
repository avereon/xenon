package com.avereon.xenon.product;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.util.SizeUnitBase2;
import com.avereon.util.ThreadUtil;
import com.avereon.xenon.task.Task;
import lombok.CustomLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@CustomLog
public class DownloadTask extends Task<Download> {

	public static final int DEFAULT_CONNECT_TIMEOUT = 2000;

	public static final int DEFAULT_READ_TIMEOUT = 10000;

	private static final int BUFFER_SIZE = 256 * (int)SizeUnitBase2.KiB.getSize();

	private static final boolean FORCE_SLOW_DOWNLOAD = false;

	private final URI uri;

	private final Path target;

	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

	private int readTimeout = DEFAULT_READ_TIMEOUT;

	public DownloadTask( Product product, URI uri ) {
		this( product, uri, null );
	}

	public DownloadTask( Product product, URI uri, Path target ) {
		super( Rb.text( "prompt", "download" ) + " " + uri.toString(), Priority.LOW );
		this.uri = uri;
		this.target = target;
	}

	public URI getUri() {
		return uri;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout( int connectTimeout ) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout( int readTimeout ) {
		this.readTimeout = readTimeout;
	}

	@Override
	public Download call() throws IOException {
		return download();
	}

	@Override
	public Download get() throws InterruptedException, ExecutionException {
		return super.get();
	}

	@Override
	public Download get( long duration, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		return super.get( duration, unit );
	}

	private Download download() throws IOException {
		URLConnection connection = uri.toURL().openConnection();
		if( connection instanceof HttpURLConnection ) ((HttpURLConnection)connection).setInstanceFollowRedirects( true );
		connection.setConnectTimeout( connectTimeout );
		connection.setReadTimeout( readTimeout );
		connection.setUseCaches( false );
		connection.connect();

		int length = connection.getContentLength();
		String encoding = connection.getContentEncoding();
		InputStream input = connection.getInputStream();

		if( connection instanceof HttpURLConnection ) {
			HttpURLConnection httpConnection =((HttpURLConnection)connection);
			int status = httpConnection.getResponseCode();
			if( status >= 300 ) throw new IOException( status + " " + httpConnection.getResponseMessage() );
		}

		setTotal( length );

		byte[] buffer = new byte[ FORCE_SLOW_DOWNLOAD ? 1024 : BUFFER_SIZE ];
		Download download = new Download( uri, length, encoding, target );

		try( download ) {
			int read;
			int offset = 0;
			while( (read = input.read( buffer )) > -1 ) {
				if( isCancelled() ) return null;
				download.write( buffer, 0, read );
				setProgress( offset += read );
				if( FORCE_SLOW_DOWNLOAD ) ThreadUtil.pause( 100 );
			}
			if( isCancelled() ) return null;
		}

		log.atFine().log(  "Resource downloaded: %s", uri );
		log.atFiner().log( "        to location: %s", download.getTarget() );

		return download;
	}

	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		if( object == null || getClass() != object.getClass() ) return false;
		DownloadTask that = (DownloadTask)object;
		return uri.equals( that.uri );
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

}
