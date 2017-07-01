package com.parallelsymmetry.essence.scheme;

import com.parallelsymmetry.essence.LogUtil;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.NullCodecException;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class FileScheme extends BaseScheme {

	private static Logger log = LogUtil.get( FileScheme.class );

	/**
	 * The key to the cached file object.
	 */
	public static final String FILE_CACHE = "scheme.file.cache";

	private List<Resource> roots;

	//private FileResourceWatcher resourceWatcher;

	public FileScheme( Program program ) {
		super( program );
		//resourceWatcher = new FileResourceWatcher();
	}

	@Override
	public String getName() {
		return "file";
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
	public void init( Resource resource ) throws ResourceException {
		super.init( resource );

		File file = getFile( resource );
		FileSystemView fsv = FileSystemView.getFileSystemView();

		boolean folder = file.isDirectory();
		boolean drive = fsv.isDrive( file );

//		// Set the resource display icon.
//		String iconName = "file";
//		if( folder ) iconName = "folder";
//		if( drive ) iconName = "drive";
//		resource.putResource( UiUtil.DISPLAY_ICON, program.getIconLibrary().getIcon( iconName ) );
//
//		// Set the resource display name.
//		resource.putResource( UiUtil.DISPLAY_NAME, fsv.getSystemDisplayName( file ) );
//
//		// Set the resource display description.
//		String description = fsv.getSystemTypeDescription( file );
//		if( folder && StringUtils.isEmpty( description ) ) description = ProductUtil.getString( program, BundleKey.LABELS, "folder" );
//		resource.putResource( UiUtil.DISPLAY_DESC, description );
	}

	@Override
	public void open( Resource resource ) throws ResourceException {
		super.open( resource );
	}

	@Override
	public void load( Resource resource, Codec codec ) throws ResourceException {
		if( codec == null ) throw new NullCodecException( resource );

		InputStream stream = null;
		File file = getFile( resource );
		try {
			stream = new FileInputStream( file );
			codec.load( resource, stream );
		} catch( MalformedURLException exception ) {
			throw new ResourceException( resource, exception );
		} catch( IOException exception ) {
			throw new ResourceException( resource, exception );
		} finally {
			if( stream != null ) {
				try {
					stream.close();
				} catch( IOException exception ) {
					throw new ResourceException( resource, exception );
				}
			}
			// TODO resource.setExternallyModified( false );
		}

		//resourceWatcher.registerWatch( resource );
	}

	@Override
	public void save( Resource resource, Codec codec ) throws ResourceException {
		if( codec == null ) throw new NullCodecException( resource );

		OutputStream stream = null;
		File file = getFile( resource );
		try {
			stream = new FileOutputStream( file );
			codec.save( resource, stream );
		} catch( MalformedURLException exception ) {
			throw new ResourceException( resource, exception );
		} catch( IOException exception ) {
			throw new ResourceException( resource, exception );
		} finally {
			if( stream != null ) {
				try {
					stream.close();
				} catch( IOException exception ) {
					throw new ResourceException( resource, exception );
				}
			}
			resource.putResource( RESOURCE_LAST_SAVED_KEY, System.currentTimeMillis() );
		}
	}

	@Override
	public void close( Resource resource ) throws ResourceException {
		//resourceWatcher.removeWatch( resource );
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
	public void saveAs( Resource resource, Resource target ) throws ResourceException {
		// Change the URI.
		resource.setUri( target.getUri() );

		// Save the resource.
		resource.getScheme().save( resource, resource.getCodec() );
	}

	@Override
	public boolean rename( Resource resource, Resource target ) throws ResourceException {
		// Change the URI.
		resource.setUri( target.getUri() );

		// Rename the file.
		try {
			return getFile( resource ).renameTo( getFile( target ) );
		} catch( Exception exception ) {
			throw new ResourceException( resource, exception );
		}
	}

	@Override
	public boolean delete( Resource resource ) throws ResourceException {
		try {
			File file = getFile( resource );
			FileUtils.forceDelete( file );
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
//			for( File root : File.listRoots() ) {
//				roots.add( program.getResourceManager().createResource( root ) );
//			}
		}

		return new ArrayList<Resource>( roots );
	}

	@Override
	public List<Resource> listResources( Resource resource ) throws ResourceException {
		if( !isFolder( resource ) ) return new ArrayList<>();

		File file = getFile( resource );
		File[] children = file.listFiles();

		if( children == null ) return new ArrayList<>();

//		return program.getResourceManager().createResources( (Object[])children );
		return null;
	}

	@Override
	public long getSize( Resource resource ) throws ResourceException {
		return getFile( resource ).length();
	}

	@Override
	public long getModifiedDate( Resource resource ) throws ResourceException {
		File file = getFile( resource );
		//if( isFolder( resource ) || FileSystemView.getFileSystemView().isDrive( file ) ) throw new ResourceException( resource, "Folders do not have a modified date." );
		return file.lastModified();
	}

//	public void startResourceWatching() {
//		resourceWatcher.start();
//	}
//
//	public void stopResourceWatching() {
//		resourceWatcher.stop();
//	}

	/**
	 * Get the file.
	 */
	private File getFile( Resource resource ) throws ResourceException {
		File file = ( (File)resource.getResource( FILE_CACHE ) );

		if( file != null ) return file;

		file = new File( resource.getUri() );

		// Get the canonical file.
		if( !FileSystemView.getFileSystemView().isDrive( file ) ) {
			try {
				file = file.getCanonicalFile();
			} catch( IOException exception ) {
				throw new ResourceException( resource, exception );
			}
		}

		resource.putResource( FILE_CACHE, file );

		return file;
	}

//	/*
//	 * Reference:
//	 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
//	 */
//	private class FileResourceWatcher extends Worker {
//
//		private WatchService watchService;
//
//		private Map<WatchKey, Path> watchServicePaths;
//
//		public FileResourceWatcher() {
//			super( "File Resource Watcher", true );
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
//			Set<Resource> resources = new HashSet<Resource>();
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
//					// It is common to have multiple events for a single resource.
//					for( WatchEvent<?> event : key.pollEvents() ) {
//						WatchEvent.Kind<?> kind = event.kind();
//						//Log.write( Log.DEVEL, "Watch event: ", event.kind(), " ", event.context(), " ", event.count() );
//
//						if( kind == OVERFLOW ) continue;
//						if( event.context() == null ) continue;
//
//						URI uri = path.resolve( (Path)event.context() ).toUri();
//						Resource resource = program.getResourceManager().createResource( uri );
//						if( !resource.isOpen() ) continue;
//
//						// This logic is intended to catch double events and events from our own save.
//						Long lastSavedTime = resource.getResource( Resource.RESOURCE_LAST_SAVED_KEY );
//						resource.putResource( Resource.RESOURCE_LAST_SAVED_KEY, System.currentTimeMillis() );
//
//						// This timeout needs to be long enough for the OS to react.
//						// In the case of network resources it can take a couple of seconds.
//						if( lastSavedTime != null && System.currentTimeMillis() - lastSavedTime < 2500 ) continue;
//
//						resources.add( resource );
//					}
//
//					for( Resource resource : resources ) {
//						resource.setExternallyModified( true );
//						//Log.write( Log.DEVEL, "Resource externally modified: ", resource );
//					}
//				} finally {
//					if( resources != null ) resources.clear();
//					if( key != null ) key.reset();
//				}
//			}
//		}
//
//		public void registerWatch( Resource resource ) throws ResourceException {
//			Path path = getFile( resource ).getParentFile().toPath();
//			try {
//				WatchKey key = path.register( watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE );
//				watchServicePaths.put( key, path );
//				resource.putResource( "java.nio.file.WatchKey", key );
//			} catch( IOException exception ) {
//				Log.write( exception );
//			}
//		}
//
//		public void removeWatch( Resource resource ) {
//			WatchKey key = resource.getResource( "java.nio.file.WatchKey" );
//			if( key == null ) return;
//			key.cancel();
//			watchServicePaths.remove( key );
//		}
//
//	}

}
