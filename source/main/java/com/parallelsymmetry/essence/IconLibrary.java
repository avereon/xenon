package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.icon.BrokenIcon;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private Map<String, ProgramIcon> icons;

	private ProgramIcon brokenIcon;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		brokenIcon = new BrokenIcon( 24 );
	}

	public Image getIcon( String id ) {
		ProgramIcon icon = icons.get( id );
		if( icon == null ) icon = brokenIcon;
		System.out.println( "Found icon: " + id + "=" + icon );
		return icon.getImage();
	}

	public void register( String id, ProgramIcon icon ) {
		icons.put( id, icon );
	}

}
