package com.avereon.xenon.asset;

import com.avereon.product.ProductEvent;

public abstract class AssetEvent extends ProductEvent {

	public enum Type {
		OPENED,
		LOADED,
		READY,
		REFRESHED,
		MODIFIED,
		UNMODIFIED,
		SAVED,
		CLOSED
	}

	private Type type;

	private Asset asset;

	protected AssetEvent( Object source, Type type, Asset asset ) {
		super( source );
		this.type = type;
		this.asset = asset;
	}

	public Type getType() {
		return type;
	}

	public Asset getAsset() {
		return asset;
	}

	@Override
	public String toString() {
		Asset asset = getAsset();
		if( asset == null ) return super.toString() + ": null";
		return super.toString() + ": " + asset.toString();
	}

}
