package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.SettingsErrorEvent;
import com.parallelsymmetry.essence.event.SettingsLoadedEvent;
import com.parallelsymmetry.essence.event.SettingsSavedEvent;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class SettingsConfiguration<T extends FileBasedConfiguration> extends FileBasedConfigurationBuilder<T> {

	/**
	 * Data will be persisted at most this fast.
	 */
	public static final long MIN_PERSIST_LIMIT = 50;

	/**
	 * Data will be persisted at least this often.
	 */
	public static final long MAX_PERSIST_LIMIT = 5000;

	private static Logger log = LoggerFactory.getLogger( SettingsConfiguration.class );

	private static Timer timer = new Timer( SettingsConfiguration.class.getSimpleName(), true );

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private Program program;

	private ExecutorService executor;

	private SaveTask task;

	private final Object taskLock = new Object();

	public static Configuration getConfiguration( Program program, File file ) throws Exception {
		PropertiesBuilderParameters params = new Parameters().properties();
		params.setFile( file );

		SettingsConfiguration<PropertiesConfiguration> builder = new SettingsConfiguration<>( PropertiesConfiguration.class, null, true, program );
		builder.configure( params );
		builder.setAutoSave( true );

		return builder.getConfiguration();
	}

	public SettingsConfiguration( Class<? extends T> resCls, Map<String, Object> params, boolean allowFailOnInit, Program program ) {
		super( resCls, params, allowFailOnInit );
		this.program = program;
		this.executor = program.getExecutor();
	}

	@Override
	public T getConfiguration() throws ConfigurationException {
		T config = super.getConfiguration();
		program.dispatchEvent( new SettingsLoadedEvent( this, getFileHandler().getFile() ) );
		return config;
	}

	@Override
	public void save() throws ConfigurationException {
		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave();
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
			executor.submit( () -> {
				try {
					SettingsConfiguration.super.save();
					program.dispatchEvent( new SettingsSavedEvent( this, getFileHandler().getFile() ) );

					lastStoreTime.set( System.currentTimeMillis() );
				} catch( ConfigurationException exception ) {
					log.error( "Error saving settings file: " + getFileHandler().getFileName(), exception );
					program.dispatchEvent( new SettingsErrorEvent( this, getFileHandler().getFile() ) );
				}
			} );
		}

	}

}
