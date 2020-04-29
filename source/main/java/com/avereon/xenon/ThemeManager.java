package com.avereon.xenon;

import com.avereon.util.Controllable;
import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import javafx.scene.paint.Color;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
		createProvidedThemes();
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

	private void registerTheme( String id, String name, String stylesheet ) {
		Path path = profileThemeFolder.resolve( stylesheet );
		themes.put( id, new ThemeMetadata( id, name, path.toUri().toString() ) );
		log.log( Log.DEBUG, "Theme registered: " + name );
	}

	private void reloadProfileThemes() {
		try {
			themes.clear();
			Files.list( profileThemeFolder ).forEach( p -> {
				if( Files.isDirectory( p ) ) return;
				try {
					List<String> lines = TextUtil.getLines( FileUtil.load( p ) );
					String id = getProperty( lines, "id" );
					String name = getProperty( lines, "name" );
					String theme = p.toAbsolutePath().toString();
					registerTheme( id, name, theme );
				} catch( IOException exception ) {
					exception.printStackTrace();
				}
			} );
		} catch( IOException exception ) {
			throw new RuntimeException( "Unable to start theme manager", exception );
		}
	}

	private String getProperty( List<String> lines, String key ) {
		key += "=";
		String line = TextUtil.findLine( lines, "^.*" + key + ".*" );
		return line == null ? null : line.substring( line.indexOf( key ) + key.length() );
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

	private void createProvidedThemes() {
		createTheme( "Xenon Dark", "#333333", "#3592C4", "#C05000", null, null );
		createTheme( "Xenon Light", "#ECECEC", "#3592C4", "#DC7000", null, null );
		createTheme( "Xenon Evening Field", "#FEF7C9", "#ABC6AC", "#657DA0", "#345879", "#1C1E27" );
		createTheme( "Xenon Evening Sky", "#1A2C3A", "#EEDC9D", "#C9CE6B", "#223854", "#68685E" );
	}

	private void createTheme( String name, String a, String b, String c, String d, String e ) {
		Color colorA = a == null ? null : Color.web( a );
		Color colorB = b == null ? null : Color.web( b );
		Color colorC = c == null ? null : Color.web( c );
		Color colorD = d == null ? null : Color.web( d );
		Color colorE = e == null ? null : Color.web( e );
		createTheme( name, colorA, colorB, colorC, colorD, colorE );
	}

	private void createTheme( String name, Color a, Color b, Color c, Color d, Color e ) {
		String id = name.replace( ' ', '-' ).toLowerCase();
		Path path = profileThemeFolder.resolve( id + ".css" );
		try( FileWriter writer = new FileWriter( path.toFile() ) ) {
			new ThemeWriter( a, b, c, d, e ).write( id, name, writer );
		} catch( IOException exception ) {
			log.log( Log.ERROR, exception );
		}
	}

}
