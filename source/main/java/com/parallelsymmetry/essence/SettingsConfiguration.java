package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.SettingsLoadedEvent;
import com.parallelsymmetry.essence.event.SettingsSavedEvent;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
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

	public static final EventType<ConfigurationBuilderEvent> LOAD = new EventType<>( Event.ANY, "SETTINGS_LOAD" );

	public static final EventType<ConfigurationBuilderEvent> SAVE = new EventType<>( Event.ANY, "SETTINGS_SAVE" );

	public static final EventType<ConfigurationBuilderEvent> SAVE_ERROR = new EventType<>( Event.ANY, "SETTINGS_SAVE_ERROR" );

	private static Logger log = LoggerFactory.getLogger( SettingsConfiguration.class );

	private static Timer timer = new Timer( SettingsConfiguration.class.getSimpleName(), true );

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private ExecutorService executor;

	private SaveTask task;

	private final Object taskLock = new Object();

	public static Configuration getConfiguration( Program program,  File file ) throws Exception {
		PropertiesBuilderParameters params = new Parameters().properties();
		params.setFile( file );

		ConfigurationEventWatcher watcher = new ConfigurationEventWatcher( program, file );
		SettingsConfiguration<PropertiesConfiguration> builder = new SettingsConfiguration<>( PropertiesConfiguration.class, null, true, program.getExecutor() );
		builder.addEventListener( SettingsConfiguration.LOAD, watcher );
		builder.addEventListener( SettingsConfiguration.SAVE, watcher );
		builder.configure( params );
		builder.setAutoSave( true );

		return builder.getConfiguration();
	}

	public SettingsConfiguration( Class<? extends T> resCls, Map<String, Object> params, boolean allowFailOnInit, ExecutorService executor ) {
		super( resCls, params, allowFailOnInit );
		this.executor = executor;
	}

	@Override
	public T getConfiguration() throws ConfigurationException {
		T config = super.getConfiguration();
		fireBuilderEvent( new ConfigurationBuilderEvent( this, LOAD ) );
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
					fireBuilderEvent( new ConfigurationBuilderEvent( SettingsConfiguration.this, SAVE ) );
					lastStoreTime.set( System.currentTimeMillis() );
				} catch( ConfigurationException exception ) {
					log.error( "Error saving settings file: " + getFileHandler().getFileName(), exception );
					fireBuilderEvent( new ConfigurationBuilderEvent( SettingsConfiguration.this, SAVE_ERROR ) );
				}
			} );
		}

	}
	private static class ConfigurationEventWatcher implements EventListener<ConfigurationBuilderEvent> {

		private Program program;

		private File file;

		public ConfigurationEventWatcher( Program program, File file ) {
			this.program = program;
			this.file = file;
		}

		@Override
		public void onEvent( ConfigurationBuilderEvent configurationEvent ) {
			if( configurationEvent.getEventType() == SettingsConfiguration.SAVE ) {
				program.dispatchEvent( new SettingsSavedEvent( configurationEvent.getSource(), file ) );
			} else if( configurationEvent.getEventType() == SettingsConfiguration.LOAD ) {
				program.dispatchEvent( new SettingsLoadedEvent( configurationEvent.getSource(), file ) );
			}
		}
	}


}
