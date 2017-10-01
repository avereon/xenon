package com.xeomar.xenon.settings;

import com.xeomar.xenon.LogUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
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

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private final Object taskLock = new Object();

	private ExecutorService executor;

	private File root;

	private File file;

	private Properties properties;

	private SaveTask task;

	private Settings defaultSettings;

	public StoredSettings( File file ) {
		this( file, null );
	}

	public StoredSettings( File file, ExecutorService executor ) {
		this( file, "", executor );
	}

	private StoredSettings( File root, String path, ExecutorService executor ) {
		if( path.endsWith( SETTINGS_EXTENSION ) ) path = path.substring( 0, path.lastIndexOf( SETTINGS_EXTENSION ) );
		this.root = root;
		this.file = new File( root, path );
		this.executor = executor;
		this.properties = new Properties();
		load();
	}

	@Override
	public String getPath() {
		return "/" + root.toURI().relativize( file.toURI() ).toString();
	}

	@Override
	public Settings getSettings( String path ) {
		File newFile = new File( file, path );
		return new StoredSettings( root, root.toURI().relativize( newFile.toURI() ).toString(), executor );
	}

	@Override
	public String[] getChildren() {
		return file.list();
	}

	@Override
	public void set( String key, Object value ) {
		String oldValue = properties.getProperty( key );
		String newValue = value == null ? null : String.valueOf( value );
		if( value == null ) {
			properties.remove( key );
		} else {
			properties.setProperty( key, newValue );
		}
		if( !Objects.equals( oldValue, value ) ) fireEvent( new SettingsEvent( this, SettingsEvent.Type.UPDATED, getPath(), key, oldValue, newValue ) );
		save();
	}

	@Override
	@Deprecated
	public String get( String key ) {
		return get( key, null );
	}

	@Override
	@Deprecated
	public String get( String key, Object defaultValue ) {
		String value = properties.getProperty( key );
		if( value == null && defaultSettings != null ) value = defaultSettings.get( key );
		if( value == null ) value = defaultValue == null ? null : defaultValue.toString();
		return value;
	}

	public Settings getDefaultSettings() {
		return defaultSettings;
	}

	public void setDefaultSettings( Settings settings ) {
		this.defaultSettings = settings;
	}

	@Override
	public void flush() {
		scheduleSave( true );
	}

	@Override
	public String toString() {
		return root.toString();
	}

	private void save() {
		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave( false );
	}

	private void load() {
		File realFile = getFile();
		if( !realFile.exists() ) return;
		try( FileInputStream fis = new FileInputStream( realFile ) ) {
			properties.load( fis );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.LOADED, getPath() ) );
		} catch( IOException exception ) {
			log.error( "Error loading settings file: " + realFile, exception );
		}
	}

	private void scheduleSave( boolean force ) {
		synchronized( taskLock ) {
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

	private void persist() {
		File realFile = getFile();
		File parent = realFile.getParentFile();
		if( !parent.mkdirs() ) return;
		try( FileOutputStream fos = new FileOutputStream( realFile ) ) {
			properties.store( fos, null );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.SAVED, getPath() ) );
			lastStoreTime.set( System.currentTimeMillis() );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + realFile, exception );
		}
	}

	private File getFile() {
		return new File( file, "settings" + SETTINGS_EXTENSION );
	}

	private class SaveTask extends TimerTask {

		@Override
		public void run() {
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( StoredSettings.this::persist );
			} else {
				persist();
			}
		}

	}

}
