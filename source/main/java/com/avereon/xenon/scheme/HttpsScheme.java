package com.avereon.xenon.scheme;

import com.avereon.xenon.Program;

public class HttpsScheme extends HttpScheme {

	public static final String ID = "https";

	public HttpsScheme( Program program ) {
		super( program, ID );
	}

}
