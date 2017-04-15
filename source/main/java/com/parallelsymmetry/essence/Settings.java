package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.SettingsLoadedEvent;
import com.parallelsymmetry.essence.event.SettingsSavedEvent;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class Settings extends FileBasedConfigurationBuilder<PropertiesConfiguration> {

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long MIN_PERSIST_LIMIT = 100;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static Logger log = LoggerFactory.getLogger( Settings.class );

	private static Timer timer = new Timer( Settings.class.getSimpleName(), true );

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private ExecutorService executor;

	private String id;

	private SaveTask task;

	private final Object taskLock = new Object();

	private Set<ProgramEventListener> listeners;

	public Settings( ExecutorService executor, File file ) {
		this( executor, file, null );
	}

	public Settings( ExecutorService executor, File file, String id ) {
		super( PropertiesConfiguration.class, null, true );
		this.executor = executor;
		this.listeners = new CopyOnWriteArraySet<>();
		this.id = id;

		configure( new Parameters().properties().setFile( file ) );
		setAutoSave( true );
	}

	@Override
	public PropertiesConfiguration getConfiguration() throws ConfigurationException {
		PropertiesConfiguration config = super.getConfiguration();
		new SettingsLoadedEvent( this, getFileHandler().getFile(), id ).fire( listeners );
		return config;
	}

	@Override
	public void save() throws ConfigurationException {
		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave();
	}

	public void addProgramEventListener( ProgramEventListener listener ) {
		listeners.add( listener );
	}

	public void removeProgramEventListener( ProgramEventListener listener ) {
		listeners.remove( listener );
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
					Settings.super.save();
					new SettingsSavedEvent( Settings.this, getFileHandler().getFile(), id ).fire( listeners );

					lastStoreTime.set( System.currentTimeMillis() );
				} catch( ConfigurationException exception ) {
					log.error( "Error saving settings file: " + getFileHandler().getFileName(), exception );
				}
			} );
		}

	}

}
