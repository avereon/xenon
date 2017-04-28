package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.icon.BrokenIcon;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	private Map<String, ProgramIcon> icons;

	private ProgramIcon brokenIcon;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		register( "broken", new BrokenIcon() );
	}

	public Image getIcon( String id ) {
		return getIcon( id, DEFAULT_SIZE );
	}

	public Image getIcon( String id, int size ) {
		ProgramIcon icon = icons.get( id );
		if( icon == null ) icon = brokenIcon;
		return icon.getImage( size );
	}

	public void register( String id, ProgramIcon icon ) {
		icons.put( id, icon );
	}

}
