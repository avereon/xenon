package com.avereon.xenon;

import com.avereon.util.Controllable;
import com.avereon.util.FileUtil;
import com.avereon.util.Log;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ThemeManager implements Controllable<ThemeManager> {

	private static final System.Logger log = Log.get();

	private Program program;

	private Map<String, ThemeMetadata> themes;

	public ThemeManager( Program program ) {
		this.program = program;
		this.themes = new ConcurrentHashMap<>();
	}

	public Program getProgram() {
		return program;
	}

	@Override
	public boolean isRunning() {
		return themes.size() > 0;
	}

	@Override
	public ThemeManager start() {
		if( Profile.DEV.equals( getProgram().getProfile() ) ) updateThemesInProfile();

		try {
			Path themeFolder = getProgram().getDataFolder().resolve( "themes" );
			Files.list( themeFolder ).forEach( p -> {
				if( !Files.isDirectory( p ) ) return;
				try {
					Path propertiesFile = p.resolve( "theme.properties" );
					Properties properties = new Properties();
					properties.load( new FileReader( propertiesFile.toFile() ) );
					registerTheme( properties );
				} catch( IOException exception ) {
					exception.printStackTrace();
				}
			} );
		} catch( IOException exception ) {
			throw new RuntimeException( "Unable to start theme manager", exception );
		}

		return this;
	}

	@Override
	public ThemeManager stop() {
		themes.clear();
		return this;
	}

	public Collection<ThemeMetadata> getThemes() {
		return Collections.unmodifiableCollection( themes.values() );
	}

	public ThemeMetadata getMetadata( String id ) {
		return themes.get( id );
	}

	private void registerTheme( Properties properties ) {
		String id = properties.getProperty( "id" );
		String name = properties.getProperty( "name" );
		String stylesheet = properties.getProperty( "stylesheet" );

		themes.put( id, new ThemeMetadata( id, name, stylesheet ) );

		log.log( Log.DEBUG, "Theme registered: " + name );
	}

	private void updateThemesInProfile() {
		// Copy the themes
		try {
			FileUtil.delete( getProgram().getDataFolder().resolve( "themes" ) );
			FileUtil.copy( Paths.get( "source/main/assembly/resources/themes" ), getProgram().getDataFolder(), true );
		} catch( IOException e ) {
			log.log( Log.ERROR, e );
		}
	}

}
