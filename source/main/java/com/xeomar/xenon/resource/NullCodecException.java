package com.xeomar.xenon.resource;

public class NullCodecException extends ResourceException {

	private static final long serialVersionUID = 3835455673089626753L;

	public NullCodecException( Resource resource ) {
		super( resource );
	}

	public NullCodecException( Resource resource, String message ) {
		super( resource, message );
	}

	public NullCodecException( Resource resource, Throwable cause ) {
		super( resource, cause );
	}

	public NullCodecException( Resource resource, String message, Throwable cause ) {
		super( resource, message, cause );
	}

}
