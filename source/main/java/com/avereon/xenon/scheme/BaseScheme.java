package com.avereon.xenon.scheme;

import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.Scheme;

import java.net.URI;
import java.net.URLConnection;
import java.util.List;

public abstract class BaseScheme implements Scheme {

	protected final Program program;

	private final String id;

	public BaseScheme( Program program, String id ) {
		this.program = program;
		this.id = id;
	}

	public Program getProgram() {
		return program;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return false;
	}

	@Override
	public boolean canSave( Asset asset ) throws AssetException {
		return false;
	}

	@Override
	public void init( Asset asset ) throws AssetException {}

	@Override
	public void open( Asset asset ) throws AssetException {}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {}

	@Override
	public void save( Asset asset, Codec codec ) throws AssetException {}

	@Override
	public void close( Asset asset ) throws AssetException {}

	@Override
	public boolean create( Asset asset ) throws AssetException {
		return false;
	}

	/**
	 * Does the asset exist according to the scheme. Must return true for a
	 * asset to be loaded.
	 */
	@Override
	public boolean exists( Asset asset ) throws AssetException {
		return false;
	}

	@Override
	public void saveAs( Asset asset, Asset aoDestination ) throws AssetException {}

	@Override
	public boolean rename( Asset asset, Asset aoDestination ) throws AssetException {
		return false;
	}

	@Override
	public boolean delete( Asset asset ) throws AssetException {
		return false;
	}

	@Override
	public boolean isFolder( Asset asset ) throws AssetException {
		return false;
	}

	@Override
	public boolean isHidden( Asset asset ) throws AssetException {
		return false;
	}

	@Override
	public List<Asset> getRoots() throws AssetException {
		return null;
	}

	@Override
	public List<Asset> listAssets( Asset asset ) throws AssetException {
		return null;
	}

	@Override
	public long getSize( Asset asset ) throws AssetException {
		return -1;
	}

	@Override
	public long getModifiedDate( Asset asset ) throws AssetException {
		return -1;
	}

	// FIXME Should this be a URLConnection or a more general AssetConnection?
	@Override
	public URLConnection getConnection( Asset asset ) {
		return null;
	}

	protected boolean isSupported( Asset asset ) {
		URI uri = asset.getUri();
		return uri == null || uri.getScheme().equals( getName() );
	}

}
