package com.avereon.xenon.asset;

import com.avereon.skill.Controllable;
import com.avereon.xenon.ProgramThreadFactory;
import com.avereon.xenon.XenonProgram;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.scheme.FileScheme;
import javafx.util.Callback;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

/*
 * Reference:
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
@CustomLog
public class ResourceWatchService implements Controllable<ResourceWatchService> {

	private static final String JAVA_NIO_FILE_WATCH_KEY = "java.nio.file.WatchKey";

	private static final long OS_REACTION_TIME = 2500;

	@Getter
	private final XenonProgram program;

	@Getter
	private final FileScheme fileScheme;

	private final Map<WatchKey, Path> watchServicePaths;

	private ExecutorService executor;

	private WatchService watchService;

	private final Map<Resource, Set<Callback<ResourceWatchEvent, ?>>> callbacks;

	public ResourceWatchService( XenonProgram program ) {
		this.program = program;
		this.fileScheme = (FileScheme)getProgram().getResourceManager().getScheme( FileScheme.ID );
		this.watchServicePaths = new ConcurrentHashMap<>();
		this.callbacks = new ConcurrentHashMap<>();
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public ResourceWatchService start() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			executor = Executors.newCachedThreadPool( new ProgramThreadFactory() );
			executor.submit( this::doWatch );
		} catch( IOException exception ) {
			log.atWarn( exception ).log();
		}
		return this;
	}

	@Override
	public ResourceWatchService stop() {
		try {
			if( executor != null ) executor.shutdown();
			if( watchService != null ) watchService.close();
		} catch( IOException exception ) {
			log.atWarn( exception ).log();
		}
		return this;
	}

	private boolean isExecutable() {
		return executor != null && !executor.isShutdown();
	}

	private void doWatch() {
		Path path;
		WatchKey key = null;
		while( isExecutable() ) {
			try {
				try {
					key = watchService.take();
				} catch( InterruptedException exception ) {
					continue;
				} catch( ClosedWatchServiceException exception ) {
					return;
				}

				path = watchServicePaths.get( key );
				if( path == null ) continue;

				// It is common to have multiple events for a single asset.
				for( WatchEvent<?> event : key.pollEvents() ) {
					WatchEvent.Kind<?> kind = event.kind();

					//log.atConfig().log( "Watch event: %s %s %s", event.kind(), event.context(), event.count() );

					if( kind == OVERFLOW ) continue;
					if( event.context() == null ) continue;
					if( event.context() instanceof Path eventPath ) {
						try {
							Path parentPath = (Path)key.watchable();
							Path assetPath = parentPath.resolve( eventPath );
							Resource resource = getProgram().getResourceManager().createAsset( assetPath );

							// This logic is intended to catch double events and events from our own save.
							long lastSavedTime = resource.getLastSaved();

							// This timeout needs to be long enough for the OS to react.
							// In the case of network assets it can take a couple of seconds.
							if( System.currentTimeMillis() - lastSavedTime < OS_REACTION_TIME ) continue;
							resource.setLastSaved( System.currentTimeMillis() );

							// Update the externally modified flag
							resource.setExternallyModified( true );

							// Dispatch the event
							ResourceWatchEvent.Type type = ResourceWatchEvent.Type.valueOf( kind.name().substring( "ENTRY_".length() ));
							dispatch( resource, new ResourceWatchEvent( type, resource ) );
						} catch( ResourceException exception ) {
							log.atWarn( exception ).log();
						}
					}
				}
			} finally {
				if( key != null ) key.reset();
			}
		}
	}

	private void dispatch( Resource resource, ResourceWatchEvent event ) throws ResourceException {
		for( Callback<ResourceWatchEvent, ?> callback : this.callbacks.getOrDefault( resource, Set.of() ) ) {
			// Don't let an individual callback stop the rest of the callbacks
			try {
				callback.call( event );
			} catch( Exception exception ) {
				log.atWarn( exception ).log();
			}
		}

		// Dispatch to the parent folder if the asset is a file
		if( !resource.isFolder() ) dispatch( getProgram().getResourceManager().getParent( resource ), event );
	}

	public void registerWatch( Resource resource, Callback<ResourceWatchEvent, ?> callback ) throws ResourceException {
		Path path = getFileScheme().getFile( resource ).toPath();
		try {
			callbacks.computeIfAbsent( resource, k -> ConcurrentHashMap.newKeySet() ).add( callback );

			WatchKey key = path.register( watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE );
			watchServicePaths.put( key, path );
			resource.setValue( JAVA_NIO_FILE_WATCH_KEY, key );
		} catch( IOException exception ) {
			throw new ResourceException( resource, exception );
		}
		//log.atConfig().log( "Registered watch for %s", asset );
	}

	public void removeWatch( Resource resource, Callback<ResourceWatchEvent, ?> callback ) {
		//log.atConfig().log( "Removing watch for %s", asset );
		WatchKey key = resource.getValue( JAVA_NIO_FILE_WATCH_KEY );
		if( key == null ) return;
		key.cancel();
		watchServicePaths.remove( key );

		// Remove the callback
		callbacks.computeIfPresent(
			resource, ( k, v ) -> {
			v.remove( callback );
			return v.isEmpty() ? null : v;
		} );
	}

}
