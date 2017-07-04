package com.parallelsymmetry.essence.scheme;

import com.parallelsymmetry.essence.resource.Scheme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Scheme implementations should be registered here. The program will add
 * default schemes and other schemes may be added by product modules.
 * <p>
 * Java can support file, jar, http, https and ftp natively.
 * <p>
 * Other scheme considerations could be ssh, sftp, scp, jdbc, webdav and zip.
 */
public final class Schemes {

	private static final Map<String, Scheme> schemes = new HashMap<String, Scheme>();

	/**
	 * Get a scheme by the scheme name.
	 *
	 * @param name The scheme name
	 * @return The scheme registered to the name
	 */
	public static Scheme getScheme( String name ) {
		return schemes.get( name );
	}

	/**
	 * Add a scheme.
	 *
	 * @param scheme The scheme to add
	 */
	public static void addScheme( Scheme scheme ) {
		schemes.put( scheme.getName(), scheme );
	}

	/**
	 * Remove a scheme.
	 *
	 * @param name The name of the scheme to remove
	 */
	public static void removeScheme( String name ) {
		schemes.remove( name );
	}

	/**
	 * Get the registered schemes.
	 *
	 * @return The set of registered schemes
	 */
	public static Set<Scheme> getSchemes() {
		return new HashSet<Scheme>( schemes.values() );
	}

	/**
	 * Get the registered scheme names.
	 *
	 * @return The set of registered scheme names
	 */
	public static Set<String> getSchemeNames() {
		return new HashSet<String>( schemes.keySet() );
	}

}
