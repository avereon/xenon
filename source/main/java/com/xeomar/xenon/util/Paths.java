package com.xeomar.xenon.util;

import java.util.ArrayList;
import java.util.List;

public class Paths {

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

	/**
	 * NOTE: This method does not normalize the path.
	 *
	 * @param root
	 * @param path
	 * @return
	 */
	public static String resolve( String root, String path ) {
		if( root == null || path == null ) return null;

		if( EMPTY.equals( root ) ) return path;
		if( EMPTY.equals( path ) ) return root;
		if( isAbsolute( path ) ) return path;

		return root.endsWith( SEPARATOR ) ? root + path : root + SEPARATOR + path;
	}

	/**
	 * NOTE: This method does not normalize the path.
	 *
	 * @param path
	 * @return
	 */
	public static String getParent( String path ) {
		path = cleanTrailingSeparator( path );
		if( path == null ) return null;
		if( isRoot( path ) ) return null;
		if( EMPTY.equals( path ) ) return null;
		int index = path.lastIndexOf( SEPARATOR );
		if( index < 0 ) return EMPTY;
		if( index == 0 ) return SEPARATOR;
		return path.substring( 0, index );
	}

	public static String normalize( String path ) {
		if( path == null ) return null;
		return cleanTrailingSeparator( normalizeSeparators( normalizeParents( path ) ) );
	}

	public static String relativize( String source, String target ) {
		if( source == null || target == null ) return null;

		if( target.equals( source ) ) {
			return EMPTY;
		} else if( isAbsolute( source ) != isAbsolute( target ) ) {
			throw new IllegalArgumentException( "Target is different type of path" );
		} else if( EMPTY.equals( source ) ) {
			return target;
		} else {
			String[] sourceNames = parseNames( source );
			String[] targetNames = parseNames( target );
			int sourceCount = sourceNames.length;
			int targetCount = targetNames.length;
			int minimumCount = Math.min( sourceCount, targetCount );

			int matchCount = 0;
			while( matchCount < minimumCount && sourceNames[ matchCount ].equals( targetNames[ matchCount ] ) ) matchCount++;

			if( matchCount < targetCount ) {
				String subpath = subpath( targetNames, matchCount, targetCount );
				if( sourceCount == matchCount ) {
					return subpath;
				} else {
					int sourceIndex = matchCount;
					StringBuilder builder = new StringBuilder();
					while( sourceIndex < sourceCount ) {
						builder.append( PARENT );
						builder.append( SEPARATOR );
						sourceIndex++;
					}
					int targetIndex = matchCount;
					while( targetIndex < targetCount ) {
						if( targetIndex > matchCount ) builder.append( SEPARATOR );
						builder.append( targetNames[ targetIndex ] );
						targetIndex++;
					}

					return builder.toString();
				}
			} else {
				int index = matchCount;
				StringBuilder builder = new StringBuilder();
				while( index < sourceCount ) {
					if( index > matchCount ) builder.append( SEPARATOR );
					builder.append( PARENT );
					index++;
				}

				return builder.toString();
			}
		}
	}

	public static String getChild( String root, String path ) {
		if( root == null || path == null ) return null;
		String child = relativize( root, path );
		int index = child.indexOf( SEPARATOR );
		return index < 0 ? child : child.substring(0,index);
	}

	private static String subpath( String[] names, int startIndex, int endIndex ) {
		StringBuilder builder = new StringBuilder();
		for( int index = startIndex; index < endIndex; index++ ) {
			if( index > startIndex ) builder.append( SEPARATOR );
			builder.append( names[ index ] );
		}
		return builder.toString();
	}

	static String[] parseNames( String path ) {
		if( path == null ) return null;
		if( EMPTY.equals( path ) ) return new String[]{ EMPTY };

		List<String> names = new ArrayList<>();
		int separatorLength = SEPARATOR.length();

		int lastIndex = 0;
		int index = path.indexOf( SEPARATOR, lastIndex );
		if( index == 0 ) {
			names.add( SEPARATOR );
			lastIndex = index + separatorLength;
			index = path.indexOf( SEPARATOR, lastIndex );
		}
		while( index > -1 ) {
			names.add( path.substring( lastIndex, index ) );
			lastIndex = index + separatorLength;
			index = path.indexOf( SEPARATOR, lastIndex );
		}

		if( path.length() > lastIndex ) names.add( path.substring( lastIndex ) );

		return names.toArray( new String[ names.size() ] );
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

	private static String normalizeSeparators( String path ) {
		if( path == null ) return null;
		while( (path.indexOf( DOUBLE_SEPARATOR )) > -1 ) {
			path = path.replace( DOUBLE_SEPARATOR, SEPARATOR );
		}
		return path;
	}

	private static String normalizeParents( String path ) {
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
