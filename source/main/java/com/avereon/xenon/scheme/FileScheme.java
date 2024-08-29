package com.avereon.xenon.scheme;

import com.avereon.util.FileUtil;
import com.avereon.util.TextUtil;
import com.avereon.util.UriUtil;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.StandardMediaTypes;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.asset.exception.NullCodecException;
import lombok.CustomLog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CustomLog
public class FileScheme extends BaseScheme {

	public static final String ID = "file";

	private static final String FILE = "file";

	private static final String TEMP_EXTENSION = ".xenonprior";

	private List<Asset> roots;

	public FileScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return isSupported( asset ) && getFile( asset ).canRead();
	}

	@Override
	public boolean canSave( Asset asset ) throws AssetException {
		return isSupported( asset ) && getFile( asset ).canWrite();
	}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		if( codec == null ) throw new NullCodecException( asset );

		File file = getFile( asset );
		try( InputStream stream = new FileInputStream( file ) ) {
			codec.load( asset, stream );
		} catch( Throwable exception ) {
			throw new AssetException( asset, exception );
		} finally {
			// TODO asset.setExternallyModified( false );
		}

		//assetWatcher.registerWatch( asset );
	}

	@Override
	public void save( Asset asset, Codec codec ) throws AssetException {
		if( codec == null ) throw new NullCodecException( asset );

		Path file = getFile( asset ).toPath();
		Path temp = null;

		// NOTE The two-step strategy needs to keep the temp file and real file on
		// the same file system because the move operation does not work across file
		// systems (two Unix mount points or two Windows drives). Network files
		// systems are also considered separate from local file systems.

		// A prior two-step implementation caused problems with cloud storage products.
		// This is presumably because the initial move step looked like a delete
		// operation to these products. This caused a race condition between the
		// second move step and the cloud storage product, occasionally leading to
		// a lost file.

		try {
			// Step one - copy current file as backup
			String tempFileName = file.getFileName() + TEMP_EXTENSION;
			temp = file.getParent().resolve( tempFileName );
			Files.deleteIfExists( temp );
			if( Files.exists( file ) ) Files.copy( file, temp );

			// Step two - save asset to file
			try( OutputStream stream = new FileOutputStream( file.toFile() ) ) {
				codec.save( asset, stream );
				if( !Files.exists( file ) ) throw new IOException( "File lost: " + file );
				asset.setLastSaved( System.currentTimeMillis() );
			}
		} catch( IOException exception ) {
			// Error recovery - move temp file back to real file
			try {
				if( Files.exists( temp ) ) Files.move( temp, file );
			} catch( IOException restoreException ) {
				log.atWarn().withCause( restoreException ).log( "Unable to restore temp file: " + temp );
			}
			throw new AssetException( asset, exception );
		} finally {
			// Cleanup - remove the temp file regardless of the outcome
			if( temp != null ) {
				try {
					Files.deleteIfExists( temp );
				} catch( IOException cleanupException ) {
					log.atWarn().withCause( cleanupException ).log( "Unable to cleanup temp file: " + temp );
				}
			}
		}
	}

	@Override
	public void close( Asset asset ) throws AssetException {
		//assetWatcher.removeWatch( asset );
		super.close( asset );
	}

	@Override
	public boolean exists( Asset asset ) throws AssetException {
		return getFile( asset ).exists();
	}

	@Override
	public boolean create( Asset asset ) throws AssetException {
		try {
			return getFile( asset ).createNewFile();
		} catch( IOException exception ) {
			throw new AssetException( asset, exception );
		}
	}

	@Override
	public boolean createFolder( Asset asset ) throws AssetException {
		return getFile( asset ).mkdirs();
	}

	@Override
	public void saveAs( Asset source, Asset target ) throws AssetException {
		// NOTE This method should not modify the source asset

		// Set the target model to the same as the source
		target.setModel( source.getModel() );

		log.atConfig().log( "Saving %s to %s", source, target );

		// Save the asset
		try {
			target.getScheme().save( target, target.getCodec() );
		} catch( Throwable throwable ) {
			throw new AssetException( source, throwable );
		}
	}

	@Override
	public boolean rename( Asset source, Asset target ) throws AssetException {
		// NOTE This method should not modify the source asset

		// Rename the file
		try {
			return getFile( source ).renameTo( getFile( target ) );
		} catch( Throwable throwable ) {
			throw new AssetException( source, throwable );
		}
	}

	@Override
	public boolean delete( Asset asset ) throws AssetException {
		try {
			File file = getFile( asset );
			FileUtil.delete( file.toPath() );
			return !file.exists();
		} catch( Exception exception ) {
			throw new AssetException( asset, exception );
		}
	}

	@Override
	public boolean isFolder( Asset asset ) throws AssetException {
		return getFile( asset ).isDirectory();
	}

	@Override
	public boolean isHidden( Asset asset ) throws AssetException {
		return getFile( asset ).isHidden();
	}

	@Override
	public List<Asset> getRoots() throws AssetException {
		if( roots == null ) {
			roots = new ArrayList<>();
			for( File root : File.listRoots() ) {
				roots.add( program.getAssetManager().createAsset( root.getPath() ) );
			}
		}

		return new ArrayList<>( roots );
	}

	@Override
	public List<Asset> listAssets( Asset asset ) throws AssetException {
		if( !isFolder( asset ) ) return new ArrayList<>();

		File file = getFile( asset );
		File[] children = file.listFiles();
		if( children == null ) return new ArrayList<>();

		return (List<Asset>)program.getAssetManager().createAssets( Arrays.asList( children ) );
	}

	@Override
	public long getSize( Asset asset ) throws AssetException {
		File file = getFile( asset );
		if( file == null ) return -1;
		if( file.isDirectory() ) {
			File[] files = file.listFiles();
			return files == null ? -1 : files.length;
		}
		return file.length();
	}

	@Override
	public long getModifiedDate( Asset asset ) throws AssetException {
		File file = getFile( asset );
		//if( isFolder( asset ) || FileSystemView.getFileSystemView().isDrive( file ) ) throw new AssetException( asset, "Folders do not have a modified date." );
		return file.lastModified();
	}

	@Override
	public String getMediaType( Asset asset ) {
		try {
			File file = getFile( asset );
			return Files.probeContentType( file.toPath() );
		} catch( IOException | AssetException exception ) {
			log.atWarning().withCause( exception ).log( "Error determining media type for asset" );
			return StandardMediaTypes.APPLICATION_OCTET_STREAM;
		}
	}

	@Override
	public String getFirstLine( Asset asset ) {
		try( FileInputStream input = new FileInputStream( getFile( asset ) ) ) {
			return readFirstLine( input, asset.getEncoding() );
		} catch( IOException | AssetException exception ) {
			log.atWarning().log( "Error determining first line for asset" );
			log.atTrace().withCause( exception ).log();
			return TextUtil.EMPTY;
		}
	}

	/**
	 * Get the file.
	 */
	public File getFile( Asset asset ) throws AssetException {
		File file = asset.getValue( FILE );

		if( file == null ) {
			try {
				String fileString = UriUtil.decode( asset.getUri().getPath() );
				asset.setValue( FILE, file = new File( fileString ).getCanonicalFile() );
			} catch( IOException exception ) {
				throw new AssetException( asset, exception );
			}
		}

		return file;
	}

}
