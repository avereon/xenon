package com.xeomar.xenon.settings;

import com.xeomar.xenon.LogUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class DelayedStoredSettings implements Settings {

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long MIN_PERSIST_LIMIT = 100;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static Logger log = LogUtil.get( DelayedStoredSettings.class );

	private static Timer timer = new Timer( DelayedStoredSettings.class.getSimpleName(), true );

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private final Object taskLock = new Object();

	private ExecutorService executor;

	private File file;

	private Properties properties;

	private SaveTask task;

	private Set<SettingsListener> listeners;

	public DelayedStoredSettings( File file ) {
		this( null, file );
	}

	public DelayedStoredSettings( ExecutorService executor, File file ) {
		this.executor = executor;
		this.file = file;
		this.properties = new Properties();
		this.listeners = new CopyOnWriteArraySet<>();
		load();
	}

	@Override
	public void set( String key, Object value ) {
		if( value == null ) {
			properties.remove( key );
		} else {
			properties.setProperty( key, String.valueOf( value ) );
		}
		save();
	}

	@Override
	@Deprecated
	public Boolean getBoolean( String key ) {
		return getBoolean( key, null );
	}

	@Override
	@Deprecated
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		String value = getString( key );
		if( value == null ) return defaultValue;
		try {
			return Boolean.parseBoolean( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Integer getInteger( String key ) {
		return getInteger( key, null );
	}

	@Override
	@Deprecated
	public Integer getInteger( String key, Integer defaultValue ) {
		String value = getString( key );
		if( value == null ) return defaultValue;
		try {
			return Integer.parseInt( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Long getLong( String key ) {
		return getLong( key, null );
	}

	@Override
	@Deprecated
	public Long getLong( String key, Long defaultValue ) {
		String value = getString( key );
		if( value == null ) return defaultValue;
		try {
			return Long.parseLong( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Float getFloat( String key ) {
		return getFloat( key, null );
	}

	@Override
	@Deprecated
	public Float getFloat( String key, Float defaultValue ) {
		String value = getString( key );
		if( value == null ) return defaultValue;
		try {
			return Float.parseFloat( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Double getDouble( String key ) {
		return getDouble( key, null );
	}

	@Override
	@Deprecated
	public Double getDouble( String key, Double defaultValue ) {
		String value = getString( key );
		if( value == null ) return defaultValue;
		try {
			return Double.parseDouble( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public String getString( String key ) {
		return getString( key, null );
	}

	@Override
	@Deprecated
	public String getString( String key, String defaultValue ) {
		return properties.getProperty( key, defaultValue );
	}

	@Override
	public void addSettingsListener( SettingsListener listener ) {
		listeners.add( listener );
	}

	@Override
	public void removeSettingsListener( SettingsListener listener ) {
		listeners.remove( listener );
	}

	@Override
	public String toString() {
		return file.toString();
	}

	private void save() {
		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave();
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

	private void scheduleSave() {
		synchronized( taskLock ) {
			long storeTime = lastStoreTime.get();
			long dirtyTime = lastDirtyTime.get();

			// If there are no changes since the last store time just return
			if( dirtyTime < storeTime ) return;

			long valueTime = lastValueTime.get();
			long softNext = valueTime + MIN_PERSIST_LIMIT;
			long hardNext = Math.max( dirtyTime, storeTime ) + MAX_PERSIST_LIMIT;
			long nextTime = Math.min( softNext, hardNext );
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			// If the existing task time is already set to the next time just return
			if( taskTime == nextTime ) return;

			// Cancel the existing task and schedule a new one
			if( task != null ) task.cancel();
			task = new SaveTask();
			timer.schedule( task, new Date( nextTime ) );
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

	private void fireEvent( SettingsEvent event ) {
		for( SettingsListener listener : new HashSet<>( listeners ) ) {
			listener.event( event );
		}
	}

	private class SaveTask extends TimerTask {

		@Override
		public void run() {
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( DelayedStoredSettings.this::persist );
			} else {
				persist();
			}
		}

	}

}
