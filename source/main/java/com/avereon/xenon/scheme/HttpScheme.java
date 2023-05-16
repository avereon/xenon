package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.NullCodecException;
import lombok.CustomLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@CustomLog
public class HttpScheme extends BaseScheme {

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
	public boolean canLoad( Asset asset ) throws AssetException {
		return true;
	}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		if( codec == null ) throw new NullCodecException( asset );

		URL url = getUrl( asset );
		try {
			URLConnection connection = url.openConnection();

			asset.setEncoding( connection.getContentEncoding() );
			asset.setMediaType( connection.getContentType() );

			try( InputStream stream = connection.getInputStream() ) {
				codec.load( asset, stream );
			} catch( Throwable exception ) {
				throw new AssetException( asset, exception );
			}
		} catch( Throwable exception ) {
			throw new AssetException( asset, exception );
		}
	}

	@Override
	public boolean exists( Asset asset ) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri( asset.getUri() ).build();
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
	private URL getUrl( Asset asset ) throws AssetException {
		URL url = asset.getValue( URL );

		if( url == null ) {
			try {
				asset.setValue( URL, url = asset.getUri().toURL() );
			} catch( IOException exception ) {
				throw new AssetException( asset, exception );
			}
		}

		return url;
	}

}
