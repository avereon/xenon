package com.avereon.xenon.asset;

public record AssetWatchEvent(Type type, Asset asset) {

	public enum Type {
		CREATE,
		MODIFY,
		DELETE
	}

}
