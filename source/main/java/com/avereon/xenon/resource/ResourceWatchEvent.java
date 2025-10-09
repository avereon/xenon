package com.avereon.xenon.resource;

public record ResourceWatchEvent(Type type, Resource resource) {

	public enum Type {
		CREATE,
		MODIFY,
		DELETE
	}

}
