package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.exception.NullCodecException;
import lombok.CustomLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@CustomLog
public class HttpScheme extends ProgramScheme {

	public static final String ID = "http";

	private static final String URL = "url";

	private HttpClient client;

	/**
	 * Create an HttpScheme.
	 *
	 * @param program The program
	 */
	public HttpScheme( Xenon program ) {
		this( program, ID );
	}

	/**
	 * So the HttpsScheme can extend this class.
	 *
	 * @param program The program
	 * @param id The scheme id
	 */
	protected HttpScheme( Xenon program, String id ) {
		super( program, id );
	}

	@Override
	public boolean canLoad( Resource resource ) throws ResourceException {
		return true;
	}

	@Override
	public void load( Resource resource, Codec codec ) throws ResourceException {
		if( codec == null ) throw new NullCodecException( resource );

		URL url = getUrl( resource );
		try {
			URLConnection connection = url.openConnection();

			resource.setEncoding( connection.getContentEncoding() );
			resource.setMediaType( connection.getContentType() );

			try( InputStream stream = connection.getInputStream() ) {
				codec.load( resource, stream );
			} catch( Throwable exception ) {
				throw new ResourceException( resource, exception );
			}
		} catch( Throwable exception ) {
			throw new ResourceException( resource, exception );
		}
	}

	@Override
	public boolean exists( Resource resource ) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri( resource.getUri() ).build();
			HttpResponse<String> response = getClient().send( request, HttpResponse.BodyHandlers.ofString() );
			return response.statusCode() < 500 & response.statusCode() != 404;
		} catch( IOException | InterruptedException exception ) {
			log.atSevere().withCause( exception ).log();
		}
		return false;
	}

	private HttpClient getClient() {
		if( client == null ) client = HttpClient.newBuilder().version( HttpClient.Version.HTTP_2 ).followRedirects( HttpClient.Redirect.ALWAYS ).build();
		return client;
	}

	/**
	 * Get the file.
	 */
	private URL getUrl( Resource resource ) throws ResourceException {
		URL url = resource.getValue( URL );

		if( url == null ) {
			try {
				resource.setValue( URL, url = resource.getUri().toURL() );
			} catch( IOException exception ) {
				throw new ResourceException( resource, exception );
			}
		}

		return url;
	}

}
