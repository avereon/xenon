package com.avereon.xenon;

import com.avereon.util.Controllable;
import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.venza.color.Colors;
import javafx.scene.paint.Color;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThemeManager implements Controllable<ThemeManager> {

	private static final System.Logger log = Log.get();

	private final Program program;

	private final Map<String, ThemeMetadata> themes;

	private final Path profileThemeFolder;

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
		try {
			Files.createDirectories( profileThemeFolder );
		} catch( IOException exception ) {
			log.log( Log.ERROR, exception );
		}
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

	private void createProvidedThemes() {
		createTheme( "Xenon Dark", "#333333", "#3592C4", "#C05000" );
		createTheme( "Xenon Light", "#ECECEC", "#3592C4", "#DC7000" );
		createTheme( "Xenon Evening Field", "#FEF7C9", "#ABC6AC", "#657DA0" );
		createTheme( "Xenon Evening Sky", "#1A2C3A", "#EEDC9D", "#C9CE6B" );

		for( String id : MaterialColor.getIds() ) {
			createMaterialDarkTheme( id );
		}

		for( String id : MaterialColor.getIds() ) {
			createMaterialLightTheme( id );
		}
	}

	private void createMaterialDarkTheme( String id ) {
		String name = "Xenon Dark Material " + MaterialColor.getName( id );
		Color ref = MaterialColor.getColor( id );
		Color base = Colors.mix( Colors.web( "#333333" ), ref, 0.1 );
		Color accent = Colors.mix( ref, Color.WHITE, 0.1 );
		Color focus = ref;
		createTheme( name, format( base ), format( accent ), format( focus ) );
	}

	private void createMaterialLightTheme( String id ) {
		String name = "Xenon Light Material " + MaterialColor.getName( id );
		Color ref = MaterialColor.getColor( id );
		Color base = Colors.mix( Colors.web( "#ECECEC" ), ref, 0.1 );
		Color accent = Colors.mix( ref, Color.WHITE, 0.1 );
		Color focus = ref;
		createTheme( name, format( base ), format( accent ), format( focus ) );
	}

	private void createTheme( String name, String base, String accent, String focus ) {
		Color colorA = base == null ? null : Color.web( base );
		Color colorB = accent == null ? null : Color.web( accent );
		Color colorC = focus == null ? null : Color.web( focus );
		createTheme( name, colorA, colorB, colorC );
	}

	private void createTheme( String name, Color base, Color accent, Color focus ) {
		String id = name.replace( ' ', '-' ).toLowerCase();
		Path path = profileThemeFolder.resolve( id + ".css" );
		try( FileWriter writer = new FileWriter( path.toFile() ) ) {
			new ThemeWriter( base, accent, focus ).write( id, name, writer );
		} catch( IOException exception ) {
			log.log( Log.ERROR, exception );
		}
	}

	private String format( Color color ) {
		return color.toString().replace( "0x", "#" ).toUpperCase();
	}

}
