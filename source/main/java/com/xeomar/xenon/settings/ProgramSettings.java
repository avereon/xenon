package com.xeomar.xenon.settings;

import com.xeomar.xenon.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.event.SettingsLoadedEvent;
import com.xeomar.xenon.event.SettingsSavedEvent;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class ProgramSettings implements Settings {

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long MIN_PERSIST_LIMIT = 100;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static Logger log = LogUtil.get( ProgramSettings.class );

	private static Timer timer = new Timer( ProgramSettings.class.getSimpleName(), true );

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private Program program;

	private File file;

	private String scope;

	private Properties properties;

	private SaveTask task;

	private final Object taskLock = new Object();

	public ProgramSettings( Program program, File file, String scope ) {
		if( program == null ) throw new IllegalArgumentException( "Program cannot be null" );
		this.program = program;
		this.file = file;
		this.scope = scope;
		this.properties = new Properties();
		load();
	}

	@Override
	public <T> T get( String key ) {
		// FIXME This probably cannot be done with generics
		return (T)properties.getProperty( key );
	}

	@Override
	public <T> T get( String key, T defaultValue ) {
		// FIXME This probably cannot be done with generics
		return (T)properties.getProperty( key );
	}

	@Override
	public <T> void set( String key, T value ) {
		properties.setProperty( key, String.valueOf( value ) );
	}

	@Override
	@Deprecated
	public Boolean getBoolean( String key ) {
		return null;
	}

	@Override
	@Deprecated
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		return null;
	}

	@Override
	@Deprecated
	public Integer getInteger( String key ) {
		return null;
	}

	@Override
	@Deprecated
	public Integer getInteger( String key, Integer defaultValue ) {
		return null;
	}

	@Override
	@Deprecated
	public Long getLong( String key ) {
		return null;
	}

	@Override
	@Deprecated
	public Long getLong( String key, Long defaultValue ) {
		return null;
	}

	@Override
	@Deprecated
	public Float getFloat( String key ) {
		return null;
	}

	@Override
	@Deprecated
	public Float getFloat( String key, Float defaultValue ) {
		return null;
	}

	@Override
	@Deprecated
	public Double getDouble( String key ) {
		return null;
	}

	@Override
	@Deprecated
	public Double getDouble( String key, Double defaultValue ) {
		return null;
	}

	@Override
	@Deprecated
	public String getString( String key ) {
		return get( key );
	}

	@Override
	@Deprecated
	public String getString( String key, String defaultValue ) {
		return get( key, defaultValue );
	}

	@Override
	public String toString() {
		return file.toString();
	}

	public void load() {
		try {
			properties.load( new FileInputStream( file ) );
			program.fireEvent( new SettingsLoadedEvent( this, file, scope ) );
		} catch( IOException exception ) {
			log.error( "Error loading settings file: " + file, exception );
		}
	}

	public void save() {
		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave();
	}

	private void persist() {
		try {
			properties.store( new FileOutputStream( file ), null );
			program.fireEvent( new SettingsSavedEvent( this, file, scope ) );
			lastStoreTime.set( System.currentTimeMillis() );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + file, exception );
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

	private class SaveTask extends TimerTask {

		@Override
		public void run() {
			ExecutorService executor = program.getExecutor();
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( ProgramSettings.this::persist );
			} else {
				persist();
			}
		}

	}

}
