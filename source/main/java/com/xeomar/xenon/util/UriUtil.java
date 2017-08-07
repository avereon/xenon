package com.xeomar.xenon.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class UriUtil {

	private static final Logger log = LoggerFactory.getLogger( UriUtil.class );

	/**
	 * Resolve an absolute URI from a string. The string may be in any of the
	 * following formats:
	 * <ul>
	 * <li>Absolute URI</li>
	 * <li>Relative URI</li>
	 * <li>Windows Path (Windows only)</li>
	 * <li>Windows UNC (Windows only)</li>
	 * </ul>
	 * Every reasonable attempt is made to create a valid URI from the string. If
	 * a valid absolute URI cannot be created directly from the string then a File
	 * object is used to generate a URI based on the string under the following
	 * situations:
	 * <ul>
	 * <li>The URI is malformed</li>
	 * <li>The URI is relative because scheme is missing</li>
	 * <li>The URI is a drive letter because the scheme is only one character long
	 * </li>
	 * </ul>
	 *
	 * @param string A URI string
	 * @return A new URI based on the specified string.
	 */
	public static URI resolve( String string ) {
		URI uri = null;

		// Try to create a URI directly from the string.
		try {
			uri = new URI( string );
		} catch( URISyntaxException exception ) {
			// Intentionally ignore exception.
		}

		// Catch common URI issues.
		boolean nullUri = uri == null;
		boolean relativeUri = uri != null && !uri.isAbsolute();
		boolean windowsDrive = uri != null && uri.getScheme() != null && uri.getScheme().length() == 1;

		if( nullUri || relativeUri || windowsDrive ) uri = new File( string ).toURI();

		// Canonicalize file URIs.
		if( "file".equals( uri.getScheme() ) ) {
			try {
				uri = new File( uri ).getCanonicalFile().toURI();
			} catch( IOException exception ) {
				// Intentionally ignore exception.
				log.error( "Error creating file URI", exception );
			}
		}

		return uri;
	}

	public static URI resolve( URI uri, URI ref ) {
		if( ref == null ) return null;
		if( uri == null ) return ref;

		Deque<String> queue = new LinkedList<String>();

		if( "jar".equals( uri.getScheme() ) ) {
			while( uri.isOpaque() ) {
				queue.add( uri.getScheme() );
				uri = URI.create( uri.getRawSchemeSpecificPart() );
			}
		}

		uri = uri.resolve( ref );

		if( "file".equals( uri.getScheme() ) ) {
			String scheme;
			while( (scheme = queue.pollLast()) != null ) {
				uri = URI.create( scheme + ":" + uri.toString() );
			}
		}

		return uri;
	}

	/**
	 * Get the parent URI taking into account opaque URI's.
	 *
	 * @param uri The URI to get the parent from
	 * @return The parent URI
	 */
	public static URI getParent( URI uri ) {
		Deque<String> queue = new LinkedList<String>();

		while( uri.isOpaque() ) {
			queue.add( uri.getScheme() );
			uri = URI.create( uri.getRawSchemeSpecificPart() );
		}

		uri = uri.resolve( "." );

		String scheme;
		while( (scheme = queue.pollLast()) != null ) {
			uri = URI.create( scheme + ":" + uri.toString() );
		}

		return uri;
	}

	public static Map<String, String> parseQuery( URI uri ) {
		if( uri == null ) return null;
		return parseQuery( uri.getQuery() );
	}

	static Map<String, String> parseQuery( String query ) {
		if( query == null ) return null;

		Map<String, String> parameters = new HashMap<String, String>();

		String[] values = query.split( "\\&" );

		for( String value : values ) {
			int index = value.indexOf( "=" );
			if( index < 0 ) {
				parameters.put( value, "true" );
			} else {
				parameters.put( value.substring( 0, index ), value.substring( index + 1 ) );
			}
		}

		return parameters;
	}

}
