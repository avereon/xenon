package com.avereon.xenon.asset.exception;

import lombok.Getter;

@Getter
public class AssetTypeNotFoundException extends RuntimeException {

	private final String assetTypeKey;

	public AssetTypeNotFoundException( String assetTypeKey ) {
		this( assetTypeKey, null );
	}

	public AssetTypeNotFoundException( String assetTypeKey, Throwable cause ) {
		super( "Asset type not found: " + assetTypeKey, cause );
		this.assetTypeKey = assetTypeKey;
	}

}
