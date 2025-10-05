package com.avereon.xenon.resource;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;

import java.net.URI;

/**
 * Resource is the new name for the former Asset class.
 *
 * This class extends the existing Asset implementation to provide a
 * backwards-compatible transition path while enabling new code to use the
 * Resource name directly.
 */
public class Resource extends Asset {

	public Resource( URI uri ) {
		super( uri );
	}

	public Resource( String uri ) {
		super( uri );
	}

	public Resource( AssetType type ) {
		super( type );
	}

	public Resource( AssetType type, URI uri ) {
		super( type, uri );
	}
}
