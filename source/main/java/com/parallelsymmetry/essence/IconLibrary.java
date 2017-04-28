package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.icon.BrokenIcon;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	public static final String BROKEN = "broken";

	private Map<String, IconRenderer> icons;

	private IconRenderer brokenIcon;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		register( BROKEN, new BrokenIcon() );
	}

	public Image getIcon( String id ) {
		return getIcon( id, DEFAULT_SIZE );
	}

	public Image getIcon( String id, int size ) {
		IconRenderer icon = icons.get( id );
		if( icon == null ) icon = icons.get( BROKEN );
		return icon.getImage( size );
	}

	public void register( String id, IconRenderer icon ) {
		icons.put( id, icon );
	}

}
