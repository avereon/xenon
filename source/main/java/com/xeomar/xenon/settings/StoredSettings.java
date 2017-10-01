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

	private static Logger log = LogUtil.get( StoredSettings.class );

	private static Timer timer = new Timer( StoredSettings.class.getSimpleName(), true );

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private final Object taskLock = new Object();

	private ExecutorService executor;

	private File file;

	private Properties properties;

	private SaveTask task;

	private Settings defaultSettings;

	public StoredSettings( File file ) {
		this( file, null );
	}

	public StoredSettings( File file, ExecutorService executor ) {
		this.executor = executor;
		this.file = file;
		this.properties = new Properties();
		load();
	}

	@Override
	public String getPath() {
		return file.toString();
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
		if( !Objects.equals( oldValue, value ) ) fireEvent( new SettingsEvent( this, SettingsEvent.Type.UPDATED, file.toString(), key, oldValue, newValue ) );
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
		return file.toString();
	}

	private void save() {
		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave( false );
	}

	private void load() {
		if( !file.exists() ) return;
		try( FileInputStream fis = new FileInputStream( file ) ) {
			properties.load( fis );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.LOADED, file.toString() ) );
		} catch( IOException exception ) {
			log.error( "Error loading settings file: " + file, exception );
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
		if( file == null ) return;
		File parent = file.getParentFile();
		if( !parent.exists() ) parent.mkdirs();
		try( FileOutputStream fos = new FileOutputStream( file ) ) {
			properties.store( fos, null );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.SAVED, file.toString() ) );
			lastStoreTime.set( System.currentTimeMillis() );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + file, exception );
		}
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
