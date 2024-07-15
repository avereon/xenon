package com.avereon.xenon;

import com.avereon.skill.Controllable;
import com.avereon.util.FileUtil;
import com.avereon.util.TextUtil;
import com.avereon.zarra.color.Colors;
import com.avereon.zarra.color.MaterialColor;
import javafx.scene.paint.Color;
import lombok.CustomLog;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class ThemeManager implements Controllable<ThemeManager> {

private final String DEFAULT_THEME_ID = "xenon-dark";

	private final Xenon program;

	private final Map<String, ThemeMetadata> themes;

	private final Path profileThemeFolder;

	public ThemeManager( Xenon program ) {
		this.program = program;
		this.themes = new ConcurrentHashMap<>();
		this.profileThemeFolder = getProgram().getDataFolder().resolve( "themes" );
	}

	public Xenon getProgram() {
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
			log.atSevere().withCause( exception ).log();
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
		return themes.get( id == null ? DEFAULT_THEME_ID : id );
	}

	private void registerTheme( String id, String name, boolean isDark, String url ) {
		if( id == null ) throw new NullPointerException( "Theme ID cannot be null" );
		Path path = profileThemeFolder.resolve( url );
		themes.put( id, new ThemeMetadata( id, name, isDark, path.toUri().toString() ) );
		log.atFiner().log( "Theme registered: %s", name );
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
					boolean isDark = Boolean.parseBoolean( getProperty( lines, "dark" ) );
					String url = p.toAbsolutePath().toString();
					registerTheme( id, name, isDark, url );
				} catch( IOException exception ) {
					//log.atWarn().withCause( exception ).log();
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

		for( Color color : MaterialColor.getColors() ) {
			createTheme( "Xenon Dark Material " + MaterialColor.getName( color ), Color.TRANSPARENT, color, false );
		}

		for( Color color : MaterialColor.getColors() ) {
			createTheme( "Xenon Dark Material " + MaterialColor.getName( color ) + " Tint", color, false );
		}

		for( Color color : MaterialColor.getColors() ) {
			createTheme( "Xenon Light Material " + MaterialColor.getName( color ), Color.TRANSPARENT, color, true );
		}

		for( Color color : MaterialColor.getColors() ) {
			createTheme( "Xenon Light Material " + MaterialColor.getName( color ) + " Tint", color, true );
		}
	}

	private void createTheme( String name, Color color, boolean light ) {
		createTheme( name, color, color, color, light );
	}

	private void createTheme( String name, Color tint, Color accent, boolean light ) {
		createTheme( name, tint, accent, accent, light );
	}

	/**
	 * Generate a light or dark theme tinted with the tint color and using the
	 * specified accent and focus colors. The accent color is left unchanged and
	 * the focus color is lightened just a bit. This will produce reasonable
	 * results if all three colors are the same color. More variety can be
	 * obtained by using different accent and focus colors.
	 * <p/>
	 * The base color is forced to be opaque, otherwise rendering artifacts will
	 * occur. The accent and focus colors are allowed to have transparency.
	 *
	 * @param name The name of the theme
	 * @param tint The tint color
	 * @param accent The accent color
	 * @param focus The focus color
	 * @param light False for dark theme, true for light theme
	 */
	private void createTheme( String name, Color tint, Color accent, Color focus, boolean light ) {
		Color base = light ? Colors.parse( "#E0E0E0" ) : Colors.parse( "#303030" );
		if( tint != Color.TRANSPARENT ) base = Colors.mix( base, tint, 0.1 );
		createTheme( name, Colors.opaque( base ), accent, Colors.mix( focus, Color.WHITE, 0.1 ) );
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
		if( Files.exists( path ) ) return;
		try( FileWriter writer = new FileWriter( path.toFile() ) ) {
			new ThemeWriter( base, accent, focus ).write( id, name, writer );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log();
		}
	}

	private String format( Color color ) {
		return color.toString().replace( "0x", "#" ).toUpperCase();
	}

}
