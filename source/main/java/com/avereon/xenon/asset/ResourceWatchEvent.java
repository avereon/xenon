package com.avereon.xenon.asset;

public record ResourceWatchEvent(Type type, Asset asset) {

	public enum Type {
		CREATE,
		MODIFY,
		DELETE
	}

}
