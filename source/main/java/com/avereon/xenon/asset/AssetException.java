package com.avereon.xenon.asset;

public class AssetException extends Exception {

	private static final long serialVersionUID = -4061564873726896880L;

	private Asset asset;

	public AssetException( Asset asset ) {
		this( asset, null, null );
	}

	public AssetException( Asset asset, String message ) {
		this( asset, message, null );
	}

	public AssetException( Asset asset, Throwable cause ) {
		this( asset, cause.getMessage(), cause );
	}

	public AssetException( Asset asset, String message, Throwable cause ) {
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
