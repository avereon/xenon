package com.avereon.xenon.asset.exception;

import com.avereon.xenon.asset.Asset;

public class NullCodecException extends ResourceException {

	private static final long serialVersionUID = 3835455673089626753L;

	public NullCodecException( Asset asset ) {
		super( asset );
	}

	public NullCodecException( Asset asset, String message ) {
		super( asset, message );
	}

	public NullCodecException( Asset asset, Throwable cause ) {
		super( asset, cause );
	}

	public NullCodecException( Asset asset, String message, Throwable cause ) {
		super( asset, message, cause );
	}

}
