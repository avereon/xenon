package com.xeomar.xenon;

import com.xeomar.xenon.event.SettingsLoadedEvent;
import com.xeomar.xenon.event.SettingsSavedEvent;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.settings.DelayedStoreSettings;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.settings.SettingsListener;
import com.xeomar.xenon.util.Controllable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SettingsManager implements Controllable<SettingsManager> {

	private static final Logger log = LoggerFactory.getLogger( SettingsManager.class );

	private Program program;

	private Map<String, File> paths;

	private Map<File,Settings> settingsMap;

	private SettingsListener settingsWatcher;

	public SettingsManager( Program program ) {
		this.program = program;
		this.settingsWatcher = new SettingsWatcher( program );

		paths = new ConcurrentHashMap<>();
		paths.put( "program", new File( program.getDataFolder(), ProgramSettings.PROGRAM ) );
		paths.put( "resource", new File( program.getDataFolder(), ProgramSettings.RESOURCE ) );
	}

	public Settings getSettings( String path ) {
		return getSettings( new File( paths.get( "program" ), path ), null );
	}

	public Settings getProgramSettings() {
		return getSettings( getSettingsFile( "program", "program" ), "PROGRAM" );
	}

	public Settings getResourceSettings( Resource resource ) {
		if( resource.getUri() == null ) return null;

		String id = IdGenerator.getId();
		return getSettings( getSettingsFile( "resource", id ), "RESOURCE" );
	}

	public Settings getSettings( File file, String scope ) {
		Settings settings = new DelayedStoreSettings( program.getExecutor(), file );
		settings.addSettingsListener( settingsWatcher );
		settingsMap.put( file, settings );
		return settings;
	}

	@Override
	public boolean isRunning() {
		return settingsMap != null;
	}

	@Override
	public SettingsManager start() {
		settingsMap = new ConcurrentHashMap<>(  );
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
		for( Settings settings : settingsMap.values() ) {
			//wrapper.close();
		}
		settingsMap = null;
		return this;
	}

	@Override
	public SettingsManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	private File getSettingsFile( String pathKey, String name ) {
		return new File( paths.get( pathKey ), name + Program.SETTINGS_EXTENSION );
	}

	private static class SettingsWatcher implements SettingsListener {

		private Program program;

		private SettingsWatcher( Program program ) {
			this.program = program;
		}

		@Override
		public void event( SettingsEvent event ) {
			switch( event.getType() ) {
				case LOADED: {
					program.fireEvent( new SettingsLoadedEvent( this, event.getRoot() ) );
					break;
				}
				case SAVED: {
					program.fireEvent( new SettingsSavedEvent( this, event.getRoot() ) );
					break;
				}
			}
		}

	}

}
