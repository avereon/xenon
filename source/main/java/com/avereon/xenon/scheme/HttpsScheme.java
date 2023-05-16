package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;

public class HttpsScheme extends HttpScheme {

	public static final String ID = "https";

	public HttpsScheme( Xenon program ) {
		super( program, ID );
	}

}
