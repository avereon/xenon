package com.avereon.xenon;

public final class ThemeMetadata implements Comparable<ThemeMetadata> {

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

	@Override
	public int compareTo( ThemeMetadata that ) {
		return this.name.compareTo( that.name );
	}

	@Override
	public String toString() {
		return name;
	}
}
