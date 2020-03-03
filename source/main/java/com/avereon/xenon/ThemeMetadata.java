package com.avereon.xenon;

public final class ThemeMetadata {

	private String id;

	private String name;

	private String stylesheet;

	public ThemeMetadata( String id, String name, String stylesheet ) {
		this.id = id;
		this.name = name;
		this.stylesheet = stylesheet;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStylesheet() {
		return stylesheet;
	}

}
