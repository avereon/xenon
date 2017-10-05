package com.xeomar.xenon.settings;

public class SettingsPaths {

	private static final String SEPARATOR = "/";

	private static final String PARENT = "..";

	private static final String DOUBLE_SEPARATOR = SEPARATOR + SEPARATOR;

	public static final String EMPTY = "";

	public static boolean isAbsolute( String path ) {
		return path != null && path.startsWith( SEPARATOR );
	}

	public static boolean isRelative( String path ) {
		return path != null && !isAbsolute( path );
	}

	public static boolean isRoot( String path ) {
		return path != null && SEPARATOR.equals( path );
	}

	public static String path( String root, String path ) {
		if( path != null && isAbsolute( path ) ) return path;
		if( root == null ) return null;
		if( path == null ) path = EMPTY;
		return resolve( root + (root.endsWith( SEPARATOR ) ? EMPTY : SEPARATOR) + path );
	}

	public static String getParent( String path ) {
		path = cleanTrailingSeparator( collapseParentReferences( path ) );
		if( path == null ) return null;
		if( isRoot( path ) ) return null;
		if( EMPTY.equals( path ) ) return null;
		int index = path.lastIndexOf( SEPARATOR );
		if( index < 0 ) return EMPTY;
		if( index == 0 ) return SEPARATOR;
		return path.substring( 0, index );
	}

	public static String resolve( String path ) {
		if( path == null ) return null;
		return cleanTrailingSeparator( cleanMultiSeparators( collapseParentReferences( path ) ) );
	}

	private static String cleanTrailingSeparator( String path ) {
		// Null path
		if( path == null ) return null;

		// Root path
		if( SEPARATOR.equals( path ) ) return path;

		// Path with trailing separator
		if( path.endsWith( SEPARATOR ) ) return path.substring( 0, path.length() - SEPARATOR.length() );

		return path;
	}

	private static String cleanMultiSeparators( String path ) {
		if( path == null ) return null;
		while( (path.indexOf( DOUBLE_SEPARATOR )) > -1 ) {
			path = path.replace( DOUBLE_SEPARATOR, SEPARATOR );
		}
		return path;
	}

	private static String collapseParentReferences( String path ) {
		if( path == null ) return null;
		String search = SEPARATOR + PARENT;
		int searchLength = search.length();
		int index = 0;
		String newPath = path;
		while( (index = newPath.indexOf( search, index )) > -1 ) {
			newPath = getParent( path.substring( 0, index ) );
			if( newPath == null ) return null;
			newPath += path.substring( index + searchLength );
		}
		return newPath;
	}

}
