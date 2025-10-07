package com.avereon.xenon.scheme;

import com.avereon.util.FileUtil;
import com.avereon.util.TextUtil;
import com.avereon.util.UriUtil;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.StandardMediaTypes;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.asset.exception.NullCodecException;
import lombok.CustomLog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CustomLog
public class FileScheme extends ProgramScheme {

	public static final String ID = "file";

	private static final String FILE = "file";

	private static final String TEMP_EXTENSION = ".xenonprior";

	private List<Resource> roots;

	public FileScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean canLoad( Resource resource ) throws ResourceException {
		return isSupported( resource ) && getFile( resource ).canRead();
	}

	@Override
	public boolean canSave( Resource resource ) throws ResourceException {
		return isSupported( resource ) && getFile( resource ).canWrite();
	}

	@Override
	public void load( Resource resource, Codec codec ) throws ResourceException {
		if( codec == null ) throw new NullCodecException( resource );

		File file = getFile( resource );
		try( InputStream stream = new FileInputStream( file ) ) {
			codec.load( resource, stream );
		} catch( Throwable exception ) {
			throw new ResourceException( resource, exception );
		} finally {
			// TODO asset.setExternallyModified( false );
		}

		//assetWatcher.registerWatch( asset );
	}

	@Override
	public void save( Resource resource, Codec codec ) throws ResourceException {
		if( codec == null ) throw new NullCodecException( resource );

		Path file = getFile( resource ).toPath();
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
				codec.save( resource, stream );
				if( !Files.exists( file ) ) throw new IOException( "File lost: " + file );
				resource.setLastSaved( System.currentTimeMillis() );
			}
		} catch( IOException exception ) {
			// Error recovery - move temp file back to real file
			try {
				if( Files.exists( temp ) ) Files.move( temp, file );
			} catch( IOException restoreException ) {
				log.atWarn().withCause( restoreException ).log( "Unable to restore temp file: " + temp );
			}
			throw new ResourceException( resource, exception );
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
	public void close( Resource resource ) throws ResourceException {
		//assetWatcher.removeWatch( asset );
		super.close( resource );
	}

	@Override
	public boolean exists( Resource resource ) throws ResourceException {
		return getFile( resource ).exists();
	}

	@Override
	public boolean create( Resource resource ) throws ResourceException {
		try {
			return getFile( resource ).createNewFile();
		} catch( IOException exception ) {
			throw new ResourceException( resource, exception );
		}
	}

	@Override
	public boolean createFolder( Resource resource ) throws ResourceException {
		return getFile( resource ).mkdirs();
	}

	@Override
	public void saveAs( Resource source, Resource target ) throws ResourceException {
		// NOTE This method should not modify the source asset

		// Set the target model to the same as the source
		target.setModel( source.getModel() );

		log.atConfig().log( "Saving %s to %s", source, target );

		// Save the asset
		try {
			target.getScheme().save( target, target.getCodec() );
		} catch( Throwable throwable ) {
			throw new ResourceException( source, throwable );
		}
	}

	@Override
	public boolean rename( Resource source, Resource target ) throws ResourceException {
		// NOTE This method should not modify the source asset

		// Rename the file
		try {
			return getFile( source ).renameTo( getFile( target ) );
		} catch( Throwable throwable ) {
			throw new ResourceException( source, throwable );
		}
	}

	@Override
	public boolean delete( Resource resource ) throws ResourceException {
		try {
			File file = getFile( resource );
			FileUtil.delete( file.toPath() );
			return !file.exists();
		} catch( Exception exception ) {
			throw new ResourceException( resource, exception );
		}
	}

	@Override
	public boolean isFolder( Resource resource ) throws ResourceException {
		return getFile( resource ).isDirectory();
	}

	@Override
	public boolean isHidden( Resource resource ) throws ResourceException {
		return getFile( resource ).isHidden();
	}

	@Override
	public List<Resource> getRoots() throws ResourceException {
		if( roots == null ) {
			roots = new ArrayList<>();
			for( File root : File.listRoots() ) {
				roots.add( program.getResourceManager().createAsset( root.getPath() ) );
			}
		}

		return new ArrayList<>( roots );
	}

	@Override
	public List<Resource> listAssets( Resource resource ) throws ResourceException {
		if( !isFolder( resource ) ) return new ArrayList<>();

		File file = getFile( resource );
		File[] children = file.listFiles();
		if( children == null ) return new ArrayList<>();

		return (List<Resource>)program.getResourceManager().createAssets( Arrays.asList( children ) );
	}

	@Override
	public long getSize( Resource resource ) throws ResourceException {
		File file = getFile( resource );
		if( file == null ) return -1;
		if( file.isDirectory() ) {
			File[] files = file.listFiles();
			return files == null ? -1 : files.length;
		}
		return file.length();
	}

	@Override
	public long getModifiedDate( Resource resource ) throws ResourceException {
		File file = getFile( resource );
		//if( isFolder( asset ) || FileSystemView.getFileSystemView().isDrive( file ) ) throw new AssetException( asset, "Folders do not have a modified date." );
		return file.lastModified();
	}

	@Override
	public String getMediaType( Resource resource ) {
		try {
			File file = getFile( resource );
			return Files.probeContentType( file.toPath() );
		} catch( IOException | ResourceException exception ) {
			log.atWarning().withCause( exception ).log( "Error determining media type for asset" );
			return StandardMediaTypes.APPLICATION_OCTET_STREAM;
		}
	}

	@Override
	public String getFirstLine( Resource resource ) {
		try( FileInputStream input = new FileInputStream( getFile( resource ) ) ) {
			return readFirstLine( input, resource.getEncoding() );
		} catch( IOException | ResourceException exception ) {
			log.atWarning().log( "Error determining first line for asset" );
			log.atTrace().withCause( exception ).log();
			return TextUtil.EMPTY;
		}
	}

	/**
	 * Get the file.
	 */
	public File getFile( Resource resource ) throws ResourceException {
		File file = resource.getValue( FILE );

		if( file == null ) {
			try {
				String fileString = UriUtil.decode( resource.getUri().getPath() );
				resource.setValue( FILE, file = new File( fileString ).getCanonicalFile() );
			} catch( IOException exception ) {
				throw new ResourceException( resource, exception );
			}
		}

		return file;
	}

}
