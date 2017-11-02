package com.xeomar.xenon.settings;

import com.xeomar.razor.LogUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class StoredSettings extends AbstractSettings {

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long MIN_PERSIST_LIMIT = 100;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static final String SETTINGS_EXTENSION = ".properties";

	private static Logger log = LogUtil.get( StoredSettings.class );

	private static Timer timer = new Timer( StoredSettings.class.getSimpleName(), true );

	// Settings map store in root node
	private Map<String, StoredSettings> settings;

	private ExecutorService executor;

	private StoredSettings root;

	private String path;

	private File folder;

	private Properties values;

	private Map<String, String> defaultValues;

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private final Object scheduleLock = new Object();

	private SaveTask task;

	public StoredSettings( File folder ) {
		this( folder, null );
	}

	public StoredSettings( File folder, ExecutorService executor ) {
		this( null, "/", folder, null, executor );
	}

	private StoredSettings( StoredSettings root, String path, File folder, Map<String, String> values, ExecutorService executor ) {
		if( root == null ) {
			this.settings = new ConcurrentHashMap<>();
			this.root = this;
		} else {
			this.root = root;
		}
		this.path = path;
		this.folder = folder;
		this.values = new Properties();
		if( values != null ) this.values.putAll( values );
		this.root.settings.put( path, this );
		this.executor = executor;
		load();
	}

	@Override
	public String getName() {
		return folder.getName();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean exists( String path ) {
		String nodePath = getNodePath( this.path, path );
		return new File( root.folder, nodePath ).exists();
	}

	@Override
	public Settings getNode( String path ) {
		return getNode( path, (Map<String, String>)null );
	}

	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		String nodePath = getNodePath( this.path, path );

		// Get or create settings node
		Settings child = root.settings.get( nodePath );

		if( child == null ) child = new StoredSettings( root, nodePath, new File( root.folder, nodePath ), values, executor );

		return child;
	}

	@Override
	public String[] getNodes() {
		String[] names = folder.list();
		return names == null ? new String[ 0 ] : names;
	}

	@Override
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<>();
		for( Object key : values.keySet() ) {
			keys.add( key.toString() );
		}
		return keys;
	}

	@Override
	public void set( String key, Object value ) {
		String oldValue = values.getProperty( key );
		String newValue = value == null ? null : String.valueOf( value );
		if( value == null ) {
			values.remove( key );
		} else {
			values.setProperty( key, newValue );
		}
		if( !Objects.equals( oldValue, value ) ) fireEvent( new SettingsEvent( this, SettingsEvent.Type.UPDATED, getPath(), key, oldValue, newValue ) );

		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave( false );
	}

	@Override
	@Deprecated
	public String get( String key ) {
		return get( key, null );
	}

	@Override
	@Deprecated
	public String get( String key, Object defaultValue ) {
		String value = values.getProperty( key );
		if( value == null && defaultValues != null ) value = defaultValues.get( key );
		if( value == null ) value = defaultValue == null ? null : defaultValue.toString();
		return value;
	}

	public Map<String, String> getDefaultValues() {
		return defaultValues;
	}

	public void setDefaultValues( Map<String, String> settings ) {
		this.defaultValues = settings;
	}

	@Override
	public void flush() {
		scheduleSave( true );
	}

	@Override
	public synchronized void delete() {
		synchronized( scheduleLock ) {
			if( task != null ) task.cancel();
		}

		root.settings.remove( getPath() );
		try {
			File file = getFile();
			if( file.exists() ) FileUtils.forceDelete( file );
			String[] files = folder.list();
			if( files != null && files.length == 0 ) FileUtils.forceDelete( folder );
		} catch( IOException exception ) {
			log.error( "Unable to delete settings file: " + folder, exception );
		}
	}

	@Override
	public String toString() {
		return folder.toString();
	}

	private synchronized void load() {
		File file = getFile();
		if( !file.exists() ) return;
		try( FileInputStream input = new FileInputStream( file ) ) {
			values.load( input );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.LOADED, getPath() ) );
		} catch( IOException exception ) {
			log.error( "Error loading settings file: " + file, exception );
		}
	}

	private synchronized void save() {
		File file = getFile();
		try {
			FileUtils.forceMkdir( file.getParentFile() );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + file, exception );
		}
		try( FileOutputStream output = new FileOutputStream( file ) ) {
			values.store( output, null );
			lastStoreTime.set( System.currentTimeMillis() );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.SAVED, getPath() ) );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + file, exception );
		}
	}

	private void scheduleSave( boolean force ) {
		synchronized( scheduleLock ) {
			long storeTime = lastStoreTime.get();
			long dirtyTime = lastDirtyTime.get();

			// If there are no changes since the last store time just return
			if( !force && (dirtyTime < storeTime) ) return;

			long valueTime = lastValueTime.get();
			long softNext = valueTime + MIN_PERSIST_LIMIT;
			long hardNext = Math.max( dirtyTime, storeTime ) + MAX_PERSIST_LIMIT;
			long nextTime = Math.min( softNext, hardNext );
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			// If the existing task time is already set to the next time just return
			if( !force && (taskTime == nextTime) ) return;

			// Cancel the existing task and schedule a new one
			if( task != null ) task.cancel();
			task = new SaveTask();
			if( force ) {
				task.run();
			} else {
				timer.schedule( task, new Date( nextTime ) );
			}
		}
	}

	private File getFile() {
		return new File( folder, "settings" + SETTINGS_EXTENSION );
	}

	private class SaveTask extends TimerTask {

		@Override
		public void run() {
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( StoredSettings.this::save );
			} else {
				save();
			}
		}

	}

}
