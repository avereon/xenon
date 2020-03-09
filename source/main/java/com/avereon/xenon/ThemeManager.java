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

	private Path profileThemeFolder;

	public ThemeManager( Program program ) {
		this.program = program;
		this.themes = new ConcurrentHashMap<>();
		this.profileThemeFolder = getProgram().getDataFolder().resolve( "themes" );
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
		updateProvidedThemes();
		reloadProfileThemes();
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

		Path path = getProgram().getDataFolder().resolve( "themes" ).resolve( id ).resolve( stylesheet );

		themes.put( id, new ThemeMetadata( id, name, path.toUri().toString() ) );

		log.log( Log.DEBUG, "Theme registered: " + name );
	}

	private void reloadProfileThemes() {
		try {
			themes.clear();
			Files.list( profileThemeFolder ).forEach( p -> {
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
	}

	private void updateProvidedThemes() {
		Path source = getProgram().getHomeFolder().resolve( "themes" );
		if( !Files.exists( source ) ) source = Paths.get( "source/main/assembly/resources/themes" );
		Path target = profileThemeFolder;

		// Copy the themes
		try {
			Files.createDirectories( target );
			if( !Files.exists( source ) ) return;

			Files.list( source ).forEach( f -> {
				try {
					log.log( Log.DEBUG, "Replacing theme: {0}", target.resolve( f.getFileName() ) );
					FileUtil.delete( target.resolve( f.getFileName() ) );
					FileUtil.copy( f, target, true );
				} catch( IOException exception ) {
					log.log( Log.ERROR, exception );
				}
			} );
		} catch( IOException exception ) {
			log.log( Log.ERROR, exception );
		}
	}

}
