package com.avereon.xenon.scheme;

import com.avereon.util.IdGenerator;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.exception.AssetException;
import lombok.CustomLog;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@CustomLog
public class NewScheme extends ProgramScheme {

	public static final String ID = "new";

	public NewScheme( Xenon program ) {
		super( program, ID );
	}

	public static URI uri() {
		return URI.create( ID + ":" + IdGenerator.getId() );
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return true;
	}

	@Override
	public boolean canSave( Asset asset ) throws AssetException {
		return true;
	}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		// New assets should be loadable from a temporary location
		if( codec != null ) {
			Path temporaryPath = getTemporaryPath( asset );
			if( temporaryPath != null && Files.exists( temporaryPath ) ) {
				try( InputStream inputStream = Files.newInputStream( temporaryPath ) ) {
					codec.load( asset, inputStream );
				} catch( Exception exception ) {
					throw new AssetException( asset, "Unable to load " + asset.getUri(), exception );
				}
			}
		}
	}

	@Override
	public void save( Asset asset, Codec codec ) throws AssetException {
		// New assets should be savable to a temporary location
		if( codec != null ) {
			Path temporaryPath = getTemporaryPath( asset );
			if( temporaryPath != null ) {
				try {
					Files.createDirectories( temporaryPath.getParent() );
				} catch( Exception exception ) {
					throw new AssetException( asset, "Unable to create directories for " + asset.getUri(), exception );
				}
				try( OutputStream outputStream = Files.newOutputStream( temporaryPath ) ) {
					if( outputStream != null ) {
						codec.save( asset, outputStream );
					}
				} catch( Exception exception ) {
					throw new AssetException( asset, "Unable to save " + asset.getUri(), exception );
				}
			}
		}
	}

	private Path getTemporaryPath( Asset asset ) {
		if( asset == null ) return null;

		// The URI should have a unique ID in the uri fragment
		URI uri = asset.getUri();
		if( uri == null ) return null;
		String id = uri.getSchemeSpecificPart();
		if( id == null ) return null;

		Path path = asset.getValue( "new-asset-temp-path" );
		if( path != null ) return path;

		Path tempPath = getProgram().getDataFolder().resolve( "storage" ).resolve( id ).resolve( "content" );
		asset.setValue( "new-asset-temp-path", tempPath );
		return tempPath;
	}

}
