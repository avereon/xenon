package com.avereon.xenon.scheme;

import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import lombok.extern.flogger.Flogger;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Flogger
public class HttpScheme extends BaseScheme {

	public static final String ID = "http";

	private HttpClient client;

	/**
	 * Create an HttpScheme.
	 *
	 * @param program The program
	 */
	public HttpScheme( Program program ) {
		this( program, ID );
	}

	/**
	 * So the HttpsScheme can extend this class.
	 *
	 * @param program The program
	 * @param id The scheme id
	 */
	protected HttpScheme( Program program, String id ) {
		super( program, id );
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

}
