package com.avereon.xenon.scheme;

import com.avereon.util.FileUtil;
import com.avereon.util.TextUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.*;
import lombok.CustomLog;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CustomLog
public class FileScheme extends BaseScheme {

	public static final String ID = "file";

	private static final String FILE = "file";

	private List<Asset> roots;

	//private FileAssetWatcher assetWatcher;

	public FileScheme( Program program ) {
		super( program, ID );
		//assetWatcher = new FileAssetWatcher();

		// NOTE Temporary
		//	private FileSystemManager fsManager;
		//		try {
		//			fsManager = VFS.getManager();
		//		} catch( FileSystemException e ) {
		//			log.log( Log.ERROR, e );
		//		}
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

		File file = getFile( asset );

		// Step one - move current file out of the way
		File temp = null;
		try {
			temp = File.createTempFile( "asset", null );
			if( !file.renameTo( temp ) ) throw new IOException( "Unable to move " + file + " > " + temp );
		} catch( IOException exception ) {
			if( temp != null && !temp.delete() ) log.atWarn().log( "Unable to remove temp file: " + temp );
		}

		// Step two - save asset to file
		try( OutputStream stream = new FileOutputStream( file ) ) {
			codec.save( asset, stream );
			if( temp != null && !temp.delete() ) log.atWarn().log( "Unable to remove temp file: " + temp );
		} catch( IOException exception ) {
			if( temp != null && !temp.renameTo( file ) ) throw new AssetException( asset, "Unable to restore " + temp + " > " + file );
			throw new AssetException( asset, exception );
		} finally {
			asset.setLastSaved( System.currentTimeMillis() );
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
			//			for( File root : File.listRoots() ) {
			//				roots.add( program.getAssetManager().createAsset( root ) );
			//			}
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

	//	public void startAssetWatching() {
	//		assetWatcher.start();
	//	}
	//
	//	public void stopAssetWatching() {
	//		assetWatcher.stop();
	//	}

	/**
	 * Get the file.
	 */
	private File getFile( Asset asset ) throws AssetException {
		File file = asset.getValue( FILE );

		if( file == null ) {
			try {
				asset.setValue( FILE, file = new File( asset.getUri() ).getCanonicalFile() );
			} catch( IOException exception ) {
				throw new AssetException( asset, exception );
			}
		}

		return file;
	}

	//	/*
	//	 * Reference:
	//	 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
	//	 */
	//	private class FileAssetWatcher extends Worker {
	//
	//		private WatchService watchService;
	//
	//		private Map<WatchKey, Path> watchServicePaths;
	//
	//		public FileAssetWatcher() {
	//			super( "File Asset Watcher", true );
	//			watchServicePaths = new ConcurrentHashMap<>();
	//		}
	//
	//		@Override
	//		public void startWorker() throws Exception {
	//			super.startWorker();
	//			watchService = FileSystems.getDefault().newWatchService();
	//		}
	//
	//		@Override
	//		public void stopWorker() throws Exception {
	//			if( watchService != null ) watchService.close();
	//			super.stopWorker();
	//		}
	//
	//		@Override
	//		public void run() {
	//			Path path = null;
	//			WatchKey key = null;
	//			Set<Asset> assets = new HashSet<Asset>();
	//			while( isExecutable() ) {
	//				try {
	//					try {
	//						key = watchService.take();
	//					} catch( InterruptedException exception ) {
	//						continue;
	//					} catch( ClosedWatchServiceException exception ) {
	//						return;
	//					}
	//
	//					//Log.write( Log.DEVEL, "Watch key: ", System.currentTimeMillis(), " ", key );
	//
	//					path = null;
	//					path = watchServicePaths.get( key );
	//					if( path == null ) continue;
	//
	//					// It is common to have multiple events for a single asset.
	//					for( WatchEvent<?> event : key.pollEvents() ) {
	//						WatchEvent.Kind<?> kind = event.kind();
	//						//Log.write( Log.DEVEL, "Watch event: ", event.kind(), " ", event.context(), " ", event.count() );
	//
	//						if( kind == OVERFLOW ) continue;
	//						if( event.context() == null ) continue;
	//
	//						URI uri = path.resolve( (Path)event.context() ).toUri();
	//						Asset asset = program.getAssetManager().createAsset( uri );
	//						if( !asset.isOpen() ) continue;
	//
	//						// This logic is intended to catch double events and events from our own save.
	//						Long lastSavedTime = asset.getResource( Asset.RESOURCE_LAST_SAVED_KEY );
	//						asset.putResource( Asset.RESOURCE_LAST_SAVED_KEY, System.currentTimeMillis() );
	//
	//						// This timeout needs to be long enough for the OS to react.
	//						// In the case of network assets it can take a couple of seconds.
	//						if( lastSavedTime != null && System.currentTimeMillis() - lastSavedTime < 2500 ) continue;
	//
	//						assets.add( asset );
	//					}
	//
	//					for( Asset asset : assets ) {
	//						asset.setExternallyModified( true );
	//						//Log.write( Log.DEVEL, "Asset externally modified: ", asset );
	//					}
	//				} finally {
	//					if( assets != null ) assets.clear();
	//					if( key != null ) key.reset();
	//				}
	//			}
	//		}
	//
	//		public void registerWatch( Asset asset ) throws AssetException {
	//			Path path = getFile( asset ).getParentFile().toPath();
	//			try {
	//				WatchKey key = path.register( watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE );
	//				watchServicePaths.add( key, path );
	//				asset.putResource( "java.nio.file.WatchKey", key );
	//			} catch( IOException exception ) {
	//				Log.write( exception );
	//			}
	//		}
	//
	//		public void removeWatch( Asset asset ) {
	//			WatchKey key = asset.getResource( "java.nio.file.WatchKey" );
	//			if( key == null ) return;
	//			key.cancel();
	//			watchServicePaths.remove( key );
	//		}
	//
	//	}

}
