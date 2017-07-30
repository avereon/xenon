package com.xeomar.xenon.resource;

public class ResourceException extends Exception {

	private static final long serialVersionUID = -4061564873726896880L;

	private Resource resource;

	public ResourceException( Resource resource ) {
		this( resource, null, null );
	}

	public ResourceException( Resource resource, String message ) {
		this( resource, message, null );
	}

	public ResourceException( Resource resource, Throwable cause ) {
		this( resource, cause.getMessage(), cause );
	}

	public ResourceException( Resource resource, String message, Throwable cause ) {
		super( message, cause );
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

}
