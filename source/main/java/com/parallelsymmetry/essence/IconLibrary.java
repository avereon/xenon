package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.icon.*;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	private Map<String, Class<? extends IconRenderer>> icons;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		register( "program", ProgramIcon.class );
		register( "close", CloseIcon.class );
		register( "about", AboutIcon.class );
	}

	public IconRenderer getIcon( String id ) {
		Class<? extends IconRenderer> renderer = icons.get( id );
		if( renderer == null ) return new MissingIcon();

		IconRenderer icon;
		try {
			icon = renderer.newInstance();
		} catch( Exception exception ) {
			icon = new BrokenIcon();
		}

		return icon;
	}

	public IconRenderer getIcon( String id, double size ) {
		IconRenderer icon = getIcon( id );
		icon.setWidth( size );
		icon.setHeight( size );
		return icon;
	}

	public Image getIconImage( String id ) {
		return getIconImage( id, DEFAULT_SIZE );
	}

	public Image getIconImage( String id, int size ) {
		return IconRenderer.getImage( getIcon( id ).setSize( size ) );
	}

	public Image[] getIconImages( String id ) {
		return getIconImages( id, 16, 24, 32, 48, 64, 128, 256 );
	}

	public Image[] getIconImages( String id, int... sizes ) {
		Image[] images = new Image[ sizes.length ];
		for( int index = 0; index < sizes.length; index++ ) {
			images[ index ] = getIconImage( id, sizes[ index ] );
		}
		return images;
	}

	public void register( String id, Class<? extends IconRenderer> icon ) {
		icons.put( id, icon );
	}

}
