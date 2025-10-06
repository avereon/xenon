package com.avereon.xenon.asset.exception;

import com.avereon.xenon.asset.Asset;

public class ResourceException extends Exception {

	private static final long serialVersionUID = -4061564873726896880L;

	private Asset asset;

	public ResourceException( Asset asset ) {
		this( asset, null, null );
	}

	public ResourceException( Asset asset, String message ) {
		this( asset, message, null );
	}

	public ResourceException( Asset asset, Throwable cause ) {
		this( asset, cause.getMessage(), cause );
	}

	public ResourceException( Asset asset, String message, Throwable cause ) {
		super( message, cause );
		this.asset = asset;
	}

	public Asset getAsset() {
		return asset;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + ": " + asset;
	}

}
