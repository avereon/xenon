package com.avereon.xenon.asset;

public record ResourceWatchEvent(Type type, Resource resource) {

	public enum Type {
		CREATE,
		MODIFY,
		DELETE
	}

}
