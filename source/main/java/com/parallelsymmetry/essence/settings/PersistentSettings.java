package com.parallelsymmetry.essence.settings;

import com.parallelsymmetry.essence.ProgramEventListener;
import com.parallelsymmetry.essence.event.SettingsPersistedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of WritableSettings that persists the values to a file on
 * a regular basis. The settings are persisted regularly but will not cause
 * high resource loads under high write conditions.
 */
public class PersistentSettings implements WritableSettings {

	private static Logger log = LoggerFactory.getLogger( PersistentSettings.class );

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long highPersistLimit = 1000;

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long lowPersistLimit = 50;

	private final Object taskLock = new Object();

	private final Object fileLock = new Object();

	private Map<String, String> map = new ConcurrentHashMap<>();

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private ExecutorService executor;

	private File file;

	private Timer timer;

	private SaveTask task;

	private Set<ProgramEventListener> listeners = new CopyOnWriteArraySet<>();

	public PersistentSettings( ExecutorService executor, File file ) throws IOException {
		this.executor = executor;
		this.file = file;
		this.timer = new Timer( file.getName(), true );
		load();
	}

	@Override
	public String get( String key ) {
		return map.get( key );
	}

	@Override
	public void put( String key, String value ) {
		if( value == null ) {
			map.remove( key );
		} else {
			map.put( key, value );
		}

		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() < lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave();
	}

	@Override
	public int size() {
		return map.size();
	}

	public Set<String> keySet() {
		return new HashSet<>( map.keySet() );
	}

	public void addEventListener( ProgramEventListener listener ) {
		this.listeners.add( listener );
	}

	public void removeEventListener( ProgramEventListener listener ) {
		this.listeners.remove( listener );
	}

	// NEXT FINISH
	private void fireEvent( SettingsPersistedEvent event ) {
		for( ProgramEventListener listener : listeners ) {
			listener.eventOccurred( event );
		}
	}

	/**
	 * Force the settings to be persisted. It is not recommended to
	 * call this method often but to allow the PersistentSettings class
	 * automatically manage when to persist.
	 *
	 * @throws IOException
	 */
	public void save() throws IOException {
		// If the last store time is greater than the last value time there is no need to save
		if( lastStoreTime.get() > lastDirtyTime.get() ) return;

		synchronized( fileLock ) {
			// Populate the properties from the map
			Properties properties = new Properties();
			properties.putAll( map );

			// Write the properties to the file
			FileWriter writer = new FileWriter( file );
			properties.store( writer, null );

			// Set the last store time
			lastStoreTime.set( System.currentTimeMillis() );

			fireEvent( new SettingsPersistedEvent() );
		}
	}

	private void load() throws IOException {
		synchronized( fileLock ) {
			// Load the properties from the file
			Properties properties = new Properties();
			if( file.exists() ) {
				FileReader reader = new FileReader( file );
				properties.load( reader );
			}

			// Populate the map from the properties
			for( Object keyObject : properties.keySet() ) {
				String key = (String)keyObject;
				map.put( key, properties.getProperty( key ) );
			}

			// Set the last value time
			lastDirtyTime.set( System.currentTimeMillis() );
		}
	}

	private void persist() {
		try {
			save();
		} catch( IOException exception ) {
			log.error( "Error persisting settings: " + file, exception );
		}
	}

	/**
	 * This method gets called a lot so it needs to perform
	 */
	private void scheduleSave() {
		synchronized( taskLock ) {
			long storeTime = lastStoreTime.get();
			long dirtyTime = lastDirtyTime.get();

			if( dirtyTime < storeTime ) return;

			// Now we know we have to save, eventually

			long valueTime = lastValueTime.get();
			long softNext = valueTime + lowPersistLimit;
			long hardNext = dirtyTime + highPersistLimit;
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			// If task time is sooner than the next hard time...let it run?


//			long nextTime = Math.min( nextSoft, nextHard );
//
//			if( taskTime <= nextTime ) return;
//
//
//			// If now is more than last save time plus
//			SaveTask task = new SaveTask();
//			timer.schedule( task, delay );

		}
	}

	private class SaveTask extends TimerTask {

		@Override
		public void run() {
			executor.submit( PersistentSettings.this::persist );
		}

	}

}
