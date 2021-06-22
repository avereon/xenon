package com.avereon.xenon;

import com.avereon.util.FileUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

public final class ThemeMetadata implements Comparable<ThemeMetadata> {

	private final String id;

	private final String name;

	private final String stylesheet;

	private String style;

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

	public String getStyle() throws IOException {
		if( style == null ) style = FileUtil.load( Paths.get( URI.create( stylesheet ).getPath() ) );
		return style;
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
