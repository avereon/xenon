package com.avereon.xenon.scheme;

import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.NullCodecException;
import com.avereon.xenon.asset.AssetException;
import java.lang.System.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class FileScheme extends BaseScheme {

	public static final String ID = "file";

	private static final Logger log = Log.log();

	/**
	 * The key to the cached file object.
	 */
	public static final String FILE_CACHE = "scheme.file.cache";

	private List<Asset> roots;

	//private FileAssetWatcher assetWatcher;

	public FileScheme( Program program ) {
		super( program );
		//assetWatcher = new FileAssetWatcher();
	}

	@Override
	public String getName() {
		return ID;
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
	public void init( Asset asset ) throws AssetException {
		super.init( asset );

		File file = getFile( asset );
		boolean folder = file.isDirectory();

		//		// Set the asset display icon.
		//		String iconName = "file";
		//		if( folder ) iconName = "folder";
		//		if( drive ) iconName = "drive";
		//		asset.putAsset( FxUtil.DISPLAY_ICON, program.getIconLibrary().getIcon( iconName ) );
		//
		//		// Set the asset display name.
		//		asset.putAsset( FxUtil.DISPLAY_NAME, fsv.getSystemDisplayName( file ) );
		//
		//		// Set the asset display description.
		//		String description = fsv.getSystemTypeDescription( file );
		//		if( folder && StringUtils.isEmpty( description ) ) description = ProductUtil.getString( program, BundleKey.LABELS, "folder" );
		//		asset.putAsset( FxUtil.DISPLAY_DESC, description );
	}

	@Override
	public void open( Asset asset ) throws AssetException {
		super.open( asset );
	}

	@Override
	public void load( Asset asset, Codec codec ) throws AssetException {
		if( codec == null ) throw new NullCodecException( asset );

		File file = getFile( asset );
		try(InputStream stream = new FileInputStream( file ) ) {
			codec.load( asset, stream );
		} catch( MalformedURLException exception ) {
			throw new AssetException( asset, exception );
		} catch( IOException exception ) {
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
		try(OutputStream stream = new FileOutputStream( file ) ) {
			codec.save( asset, stream );
		} catch( MalformedURLException exception ) {
			throw new AssetException( asset, exception );
		} catch( IOException exception ) {
			throw new AssetException( asset, exception );
		} finally {
			asset.putResource( ASSET_LAST_SAVED_KEY, System.currentTimeMillis() );
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
	public void saveAs( Asset asset, Asset target ) throws AssetException {
		// Change the URI.
		asset.setUri( target.getUri() );

		// Save the asset.
		asset.getScheme().save( asset, asset.getCodec() );
	}

	@Override
	public boolean rename( Asset asset, Asset target ) throws AssetException {
		// Change the URI.
		asset.setUri( target.getUri() );

		// Rename the file.
		try {
			return getFile( asset ).renameTo( getFile( target ) );
		} catch( Exception exception ) {
			throw new AssetException( asset, exception );
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

		return new ArrayList<Asset>( roots );
	}

	@Override
	public List<Asset> listAssets( Asset asset ) throws AssetException {
		if( !isFolder( asset ) ) return new ArrayList<>();

		File file = getFile( asset );
		File[] children = file.listFiles();

		if( children == null ) return new ArrayList<>();

		//		return program.getAssetManager().createAssets( (Object[])children );
		return null;
	}

	@Override
	public long getSize( Asset asset ) throws AssetException {
		return getFile( asset ).length();
	}

	@Override
	public long getModifiedDate( Asset asset ) throws AssetException {
		File file = getFile( asset );
		//if( isFolder( asset ) || FileSystemView.getFileSystemView().isDrive( file ) ) throw new AssetException( asset, "Folders do not have a modified date." );
		return file.lastModified();
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
		File file = asset.getResource( FILE_CACHE );
		if( file != null ) return file;

		// Get the canonical file.
		try {
			file = new File( asset.getUri() ).getCanonicalFile();
		} catch( IOException exception ) {
			throw new AssetException( asset, exception );
		}

		asset.putResource( FILE_CACHE, file );

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
