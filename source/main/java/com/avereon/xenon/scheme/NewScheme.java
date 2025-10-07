package com.avereon.xenon.scheme;

import com.avereon.util.IdGenerator;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.exception.ResourceException;
import lombok.CustomLog;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@CustomLog
public class NewScheme extends ProgramScheme {

	public static final String ID = "new";

	public static final String NEW_ASSET_TEMP_PATH = "new-asset-temp-path";

	public static final String NEW_ASSET_TEMP_STORAGE_FOLDER = "storage";

	public static final String NEW_ASSET_TEMP_STORAGE_CONTENT = "content";

	public NewScheme( Xenon program ) {
		super( program, ID );
	}

	public static URI uri() {
		return URI.create( ID + ":" + IdGenerator.getId() );
	}

	@Override
	public boolean canLoad( Resource resource ) throws ResourceException {
		return true;
	}

	@Override
	public boolean canSave( Resource resource ) throws ResourceException {
		return true;
	}

	@Override
	public void load( Resource resource, Codec codec ) throws ResourceException {
		// New assets should be loadable from a temporary location
		if( codec != null ) {
			Path temporaryPath = getTemporaryPath( resource );
			if( temporaryPath != null && Files.exists( temporaryPath ) ) {
				try( InputStream inputStream = Files.newInputStream( temporaryPath ) ) {
					codec.load( resource, inputStream );
				} catch( Exception exception ) {
					throw new ResourceException( resource, "Unable to load " + resource.getUri(), exception );
				}
			}
		}
	}

	@Override
	public void save( Resource resource, Codec codec ) throws ResourceException {
		// New assets should be savable to a temporary location
		if( codec != null ) {
			Path temporaryPath = getTemporaryPath( resource );
			if( temporaryPath != null ) {
				try {
					Files.createDirectories( temporaryPath.getParent() );
				} catch( Exception exception ) {
					throw new ResourceException( resource, "Unable to create directories for " + resource.getUri(), exception );
				}
				try( OutputStream outputStream = Files.newOutputStream( temporaryPath ) ) {
					if( outputStream != null ) {
						codec.save( resource, outputStream );
					}
				} catch( Exception exception ) {
					throw new ResourceException( resource, "Unable to save " + resource.getUri(), exception );
				}
			}
		}
	}

	Path getTemporaryPath( Resource resource ) {
		if( resource == null ) return null;

		// The URI should have a unique ID in the uri fragment
		URI uri = resource.getUri();
		if( uri == null ) return null;
		String id = uri.getSchemeSpecificPart();
		if( id == null ) return null;

		Path path = resource.getValue( NEW_ASSET_TEMP_PATH );
		if( path != null ) return path;

		Path tempPath = getProgram().getDataFolder().resolve( NEW_ASSET_TEMP_STORAGE_FOLDER ).resolve( id ).resolve( NEW_ASSET_TEMP_STORAGE_CONTENT );
		resource.setValue( NEW_ASSET_TEMP_PATH, tempPath );
		return tempPath;
	}

}
