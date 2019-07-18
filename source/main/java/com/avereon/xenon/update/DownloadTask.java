package com.avereon.xenon.update;

import com.avereon.product.Product;
import com.avereon.util.LogUtil;
import com.avereon.xenon.task.Task;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloadTask extends Task<Download> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final int DEFAULT_CONNECT_TIMEOUT = 2000;

	public static final int DEFAULT_READ_TIMEOUT = 10000;

	private static final int BUFFER_SIZE = 256 * 1024;

	private Product product;

	private URI uri;

	private Path target;

	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

	private int readTimeout = DEFAULT_READ_TIMEOUT;

	private Set<DownloadListener> listeners;

	public DownloadTask( Product product, URI uri ) {
		this( product, uri, null );
		listeners = new CopyOnWriteArraySet<>();
	}

	public DownloadTask( Product product, URI uri, Path target ) {
		super( product.getResourceBundle().getString( "prompt", "download" ) + " " + uri.toString(), Priority.LOW );
		this.uri = uri;
		this.target = target;
	}

	public URI getUri() {
		return uri;
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

		byte[] buffer = new byte[ BUFFER_SIZE ];
		Download download = new Download( uri, length, encoding, target );

		try {
			int read = 0;
			int offset = 0;
			while( (read = input.read( buffer )) > -1 ) {
				if( isCancelled() ) return null;
				download.write( buffer, 0, read );
				offset += read;
				setProgress( offset );
				fireEvent( new DownloadEvent( offset, length ) );
			}
			if( isCancelled() ) return null;
		} finally {
			download.close();
		}

		log.debug( "Resource downloaded: " + uri );
		log.trace( "        to location: " + download.getTarget() );

		return download;
	}

	public void addListener( DownloadListener listener ) {
		listeners.add( listener );
	}

	public void removeListener( DownloadListener listener ) {
		listeners.remove( listener );
	}

	private void fireEvent( DownloadEvent event ) {
		for( DownloadListener listener : new HashSet<>( listeners ) ) {
			try {
				listener.update( event );
			} catch( Throwable throwable ) {
				log.error( "Error updating download progress", throwable );
			}
		}
	}

}
