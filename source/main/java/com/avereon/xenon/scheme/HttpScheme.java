package com.avereon.xenon.scheme;

import com.avereon.xenon.Program;

public class HttpScheme extends BaseScheme {

	public static final String ID = "http";

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

}
