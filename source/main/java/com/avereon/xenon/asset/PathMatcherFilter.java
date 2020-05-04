package com.avereon.xenon.asset;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PathMatcherFilter implements DirectoryStream.Filter<Path> {

	private final Set<PathMatcher> matchers;

	public PathMatcherFilter( PathMatcher... matchers ) {
		this.matchers = new HashSet<>( Arrays.asList( matchers ) );
	}

	@Override
	public boolean accept( Path path ) {
		for( PathMatcher matcher : matchers ) {
			if( matcher.matches( path ) ) return true;
		}
		return false;
	}

}
