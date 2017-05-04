package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.icon.*;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	private Map<String, Class<? extends ProgramIcon>> icons;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		register( "program", AppProgramIcon.class );
		register( "close", CloseProgramIcon.class );
		register( "about", AboutProgramIcon.class );
	}

	public ProgramIcon getIcon( String id ) {
		Class<? extends ProgramIcon> renderer = icons.get( id );
		if( renderer == null ) return new MissingProgramIcon();

		ProgramIcon icon;
		try {
			icon = renderer.newInstance();
		} catch( Exception exception ) {
			icon = new BrokenProgramIcon();
		}

		return icon;
	}

	public ProgramIcon getIcon( String id, double size ) {
		ProgramIcon icon = getIcon( id );
		icon.setWidth( size );
		icon.setHeight( size );
		return icon;
	}

	public Image getIconImage( String id ) {
		return getIconImage( id, DEFAULT_SIZE );
	}

	public Image getIconImage( String id, int size ) {
		return ProgramIcon.getImage( getIcon( id ).setSize( size ) );
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

	public void register( String id, Class<? extends ProgramIcon> icon ) {
		icons.put( id, icon );
	}

}
