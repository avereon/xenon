package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.settings.ProgramConfigurationBuilder;
import com.parallelsymmetry.essence.settings.Settings;
import com.parallelsymmetry.essence.util.Controllable;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class SettingsManager implements Controllable<SettingsManager> {

	private static final Logger log = LoggerFactory.getLogger( SettingsManager.class );

	private Program program;

	private Set<ConfigWrapper> wrappers;

	public SettingsManager( Program program ) {
		this.program = program;
	}

	public Settings getSettings( File file ) {
		return getSettings( file, null );
	}

	public Settings getSettings( File file, String id ) {
		ProgramConfigurationBuilder builder = new ProgramConfigurationBuilder( program, file, id );
		//builder.addProgramEventListener( program.getEventWatcher() );
		builder.setExecutor( program.getExecutor() );

		ConfigWrapper wrapper = new ConfigWrapper( builder );
		wrappers.add( wrapper );

		return wrapper;
	}

	@Override
	public boolean isRunning() {
		return wrappers != null;
	}

	@Override
	public SettingsManager start() {
		wrappers = new CopyOnWriteArraySet<>();
		return this;
	}

	@Override
	public SettingsManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public SettingsManager restart() {
		stop();
		start();
		return this;
	}

	@Override
	public SettingsManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public SettingsManager stop() {
		for( ConfigWrapper wrapper : wrappers ) {
			//wrapper.close();
		}
		wrappers = null;
		return this;
	}

	@Override
	public SettingsManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	private class ConfigWrapper implements Settings {

		private ProgramConfigurationBuilder builder;

		private PropertiesConfiguration properties;

		public ConfigWrapper( ProgramConfigurationBuilder builder ) {
			this.builder = builder;
			try {
				this.properties = builder.getConfiguration();
			} catch( ConfigurationException exception ) {
				exception.printStackTrace();
				log.error( "Error creating wrappers: " + builder, exception );
			}
		}

		@Override
		public Boolean getBoolean( String key ) {
			return properties.getBoolean( key, null );
		}

		@Override
		public Boolean getBoolean( String key, Boolean defaultValue ) {
			return properties.getBoolean( key, defaultValue );
		}

		@Override
		public Integer getInteger( String key ) {
			return properties.getInteger( key, null );
		}

		@Override
		public Integer getInteger( String key, Integer defaultValue ) {
			return properties.getInteger( key, defaultValue );
		}

		@Override
		public Long getLong( String key ) {
			return properties.getLong( key, null );
		}

		@Override
		public Long getLong( String key, Long defaultValue ) {
			return properties.getLong( key, defaultValue );
		}

		@Override
		public Float getFloat( String key ) {
			return properties.getFloat( key, null );
		}

		@Override
		public Float getFloat( String key, Float defaultValue ) {
			return properties.getFloat( key, defaultValue );
		}

		@Override
		public Double getDouble( String key ) {
			return properties.getDouble( key, null );
		}

		@Override
		public Double getDouble( String key, Double defaultValue ) {
			return properties.getDouble( key, defaultValue );
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public String getString( String key ) {
			return properties.getString( key, null );
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public String getString( String key, String defaultValue ) {
			return properties.getString( key, defaultValue );
		}

		@Override
		public void set( String key, Object value ) {
			if( value == null ) {
				properties.clearProperty( key );
			} else {
				properties.setProperty( key, value );
			}
		}

	}

}
