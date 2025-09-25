package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.zerra.theme.Theme;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

public final class ThemeMetadata implements Comparable<ThemeMetadata> {

	private final String id;

	private final String name;

	private final boolean dark;

	private final String url;

	private String style;

	public ThemeMetadata( String id, String name, boolean dark, String url ) {
		this.id = id;
		this.name = name;
		this.dark = dark;
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Theme getMotif() {
		return dark ? Theme.DARK : Theme.LIGHT;
	}

	public boolean isDark() {
		return dark;
	}

	public String getUrl() {
		return url;
	}

	public String getStyle() throws IOException {
		if( style == null ) style = FileUtil.load( Paths.get( URI.create( url ).getPath() ) );
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
