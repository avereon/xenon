package com.xeomar.xenon.settings;

public class SettingsPaths {

	private static final String SEPARATOR = "/";

	private static final String PARENT = "..";

	private static final String DOUBLE_SEPARATOR = SEPARATOR + SEPARATOR;

	public static boolean isAbsolute( String path ) {
		return path != null && path.startsWith( SEPARATOR );
	}

	public static boolean isRelative( String path ) {
		return path != null && !isAbsolute( path );
	}

	public static boolean isRoot( String path ) {
		return path != null && SEPARATOR.equals( path );
	}

	public static String resolve( String path ) {
		if( path == null ) return null;
		return cleanTrailingSeparator( cleanMultiSeparators( collapseParentReferences( path ) ) );
	}

	public static String resolve( String root, String path ) {
		if( path != null && isAbsolute( path ) ) return path;
		if( root == null ) return null;
		if( path == null ) path = "";
		return resolve( root + (root.endsWith( SEPARATOR ) ? "" : SEPARATOR) + path );
	}

	public static String getParent( String path ) {
		if( path == null ) return null;
		if( isRoot( path ) ) return null;
		path = cleanTrailingSeparator( path );
		int index = path.lastIndexOf( SEPARATOR );
		if( index < 0 ) return "";
		return path.substring( 0, index );
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
		while( (path.indexOf( DOUBLE_SEPARATOR )) > -1 ) {
			path = path.replace( DOUBLE_SEPARATOR, SEPARATOR );
		}
		return path;
	}

	private static String collapseParentReferences( String path ) {
		String search = SEPARATOR + PARENT;
		int searchLength = search.length();
		int index = 0;
		while( (index = path.indexOf( search, index )) > -1 ) {
			path = getParent( path.substring( 0, index ) );
			if( path == null ) return null;
			path += path.substring( index + searchLength );
		}
		return path;
	}

}
