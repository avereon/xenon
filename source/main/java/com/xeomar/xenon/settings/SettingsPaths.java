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

	/**
	 * This method does not normalize the path.
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

		return root + (root.endsWith( SEPARATOR ) ? EMPTY : SEPARATOR) + path;
	}

	/**
	 * This method does not normalize the path.
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
			// NEXT Continue to implement relativize
		}

		return null;
		//		UnixPath var2 = toUnixPath(var1);
		//		if (var2.equals(this)) {
		//			return this.emptyPath();
		//		} else if (this.isAbsolute() != var2.isAbsolute()) {
		//			throw new IllegalArgumentException("'other' is different type of Path");
		//		} else if (this.isEmpty()) {
		//			return var2;
		//		} else {
		//			int var3 = this.getNameCount();
		//			int var4 = var2.getNameCount();
		//			int var5 = var3 > var4 ? var4 : var3;
		//
		//			int var6;
		//			for(var6 = 0; var6 < var5 && this.getName(var6).equals(var2.getName(var6)); ++var6) {
		//				;
		//			}
		//
		//			int var7 = var3 - var6;
		//			if (var6 < var4) {
		//				UnixPath var13 = var2.subpath(var6, var4);
		//				if (var7 == 0) {
		//					return var13;
		//				} else {
		//					boolean var14 = var2.isEmpty();
		//					int var10 = var7 * 3 + var13.path.length;
		//					if (var14) {
		//						assert var13.isEmpty();
		//
		//						--var10;
		//					}
		//
		//					byte[] var11 = new byte[var10];
		//
		//					int var12;
		//					for(var12 = 0; var7 > 0; --var7) {
		//						var11[var12++] = 46;
		//						var11[var12++] = 46;
		//						if (var14) {
		//							if (var7 > 1) {
		//								var11[var12++] = 47;
		//							}
		//						} else {
		//							var11[var12++] = 47;
		//						}
		//					}
		//
		//					System.arraycopy(var13.path, 0, var11, var12, var13.path.length);
		//					return new UnixPath(this.getFileSystem(), var11);
		//				}
		//			} else {
		//				byte[] var8 = new byte[var7 * 3 - 1];
		//
		//				for(int var9 = 0; var7 > 0; --var7) {
		//					var8[var9++] = 46;
		//					var8[var9++] = 46;
		//					if (var7 > 1) {
		//						var8[var9++] = 47;
		//					}
		//				}
		//
		//				return new UnixPath(this.getFileSystem(), var8);
		//			}
		//		}
	}

	private static String[] parseNames( String path ) {
		int count = countSeparators( path );
		String[] names = new String[count];
		int length = SEPARATOR.length();

		int index = -length;
		int lastIndex = 0;
		int arrayIndex= 0;
		while( (index = path.indexOf( SEPARATOR, index + length )) > -1 ) {
			names[arrayIndex] = path.substring( lastIndex, index + length );
			lastIndex = index + length;
		}

		return names;
	}

	private static int countSeparators( String path ) {
		if( path == null ) throw new IllegalArgumentException( "Path cannot be null" );
		int length = SEPARATOR.length();
		int count = 0;
		int index = -length;
		while( (index = path.indexOf( SEPARATOR, index + length )) > -1 ) count++;
		return count;
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
