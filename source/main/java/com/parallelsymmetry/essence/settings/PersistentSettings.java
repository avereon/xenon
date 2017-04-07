package com.parallelsymmetry.essence.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of WritableSettings that persists the values to a file on
 * a regular basis. The settings are persisted regularly but will not cause
 * high resource loads under high write conditions.
 */
public class PersistentSettings implements WritableSettings {

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

	private File file;

	private ExecutorService executor;

	private AtomicLong lastValueTime;

	private AtomicLong lastStoreTime;

	public PersistentSettings( ExecutorService executor, File file ) throws IOException {
		this.executor = executor;
		this.file = file;
		load();
	}

	@Override
	public String get( String key ) {
		return map.get( key );
	}

	@Override
	public void put( String key, String value ) {
		map.put( key, value );
		lastValueTime.set( System.currentTimeMillis() );

		// NEXT Manage the store task
		synchronized( taskLock ) {

		}
	}

	public void save() throws IOException {
		synchronized( fileLock ) {
			// Populate the properties from the map
			Properties properties = new Properties();
			properties.putAll( map );

			// Write the properties to the file
			FileWriter writer = new FileWriter( file );
			properties.store( writer, null );

			// Set the last store time
			lastStoreTime.set( System.currentTimeMillis() );
		}
	}

	private void load() throws IOException {
		synchronized( fileLock ) {
			// Load the properties from the file
			Properties properties = new Properties();
			FileReader reader = new FileReader( file );
			properties.load( reader );

			// Populate the map from the properties
			for( Object keyObject : properties.keySet() ) {
				String key = (String)keyObject;
				map.put( key, properties.getProperty( key ) );
			}

			// Set the last value time
			lastValueTime.set( System.currentTimeMillis() );
		}
	}

}
