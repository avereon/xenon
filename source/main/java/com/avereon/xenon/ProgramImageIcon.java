package com.avereon.xenon;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramImageIcon extends ProgramIcon {

	private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

	private String url;

	/**
	 * This constructor is used to copy the icon.
	 */
	@SuppressWarnings( "unused" )
	ProgramImageIcon() {}

	public ProgramImageIcon( String url ) {
		this.url = url;
	}

	@Override
	protected ProgramImage copy() {
		ProgramImageIcon image = (ProgramImageIcon)super.copy();
		image.url = this.url;
		return image;
	}

	@Override
	protected void render() {
		drawImage( getImage( url ) );
	}

	Runnable getPreloadRunner() {
		return () -> getImage( url );
	}

	public static void main( String[] commands ) {
		ProgramImageIcon icon = new ProgramImageIcon( "https://www.avereon.com/download/latest/xenon/product/icon" );
		proof( icon );
	}

	private Image getImage( String url ) {
		Image source = CACHE.computeIfAbsent( url, Image::new );

		// TODO This was an attempt to store pre-scaled images
		//		int w = (int)getWidth();
		//		int h = (int)getHeight();
		//		String key = url + "-" + w + "-" + h;
		//		Image cached = CACHE.computeIfAbsent( key, ( k) -> {
		//			return Images.scaleImage( source, w,h );
		//		} );

		return source;
	}

}
