package com.parallelsymmetry.essence.scheme;

import com.parallelsymmetry.essence.resource.ResourceType;
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

	private static final Map<String, ResourceType> resourceTypes = new HashMap<>();

	/**
	 * Get a scheme by the scheme name.
	 *
	 * @param name
	 * @return
	 */
	public static Scheme getScheme( String name ) {
		return schemes.get( name );
	}

	/**
	 * Add a scheme.
	 *
	 * @param scheme
	 */
	public static void addScheme( Scheme scheme ) {
		schemes.put( scheme.getName(), scheme );
	}

	/**
	 * Add a scheme and associate a specific resource type to it.
	 *
	 * @param scheme
	 * @param type
	 */
	public static void addScheme( Scheme scheme, ResourceType type ) {
		System.out.println( "Scheme registered: scheme=" + scheme + " type=" + type );
		schemes.put( scheme.getName(), scheme );
		resourceTypes.put( scheme.getName(), type );
	}

	/**
	 * Remove a scheme.
	 *
	 * @param scheme
	 */
	public static void removeScheme( Scheme scheme ) {
		schemes.remove( scheme.getName() );
		resourceTypes.remove( scheme.getName() );
	}

	/**
	 * Get the registered schemes.
	 *
	 * @return
	 */
	public static Set<Scheme> getSchemes() {
		return new HashSet<Scheme>( schemes.values() );
	}

	/**
	 * Get the registered scheme names.
	 * @return
	 */
	public static Set<String> getSchemeNames() {
		return new HashSet<String>( schemes.keySet() );
	}

	/**
	 * Get the resource type assigned to a scheme.
	 *
	 * @param scheme
	 * @return
	 */
	public static ResourceType getResourceType( Scheme scheme ) {
		return resourceTypes.get( scheme.getName() );
	}

}
