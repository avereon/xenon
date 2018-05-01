package com.xeomar.xenon.image;

import com.xeomar.xenon.ProgramImage;
import com.xeomar.xenon.icon.XRingIcon;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class InstallerBannerImage extends ProgramImage {

	public InstallerBannerImage() {
		setWidth( 900 );
		setHeight( 300 );
	}

	@Override
	protected void render() {
		double zoom = 1.25;
		double offset = (zoom * 0.5) - 0.5;
		move( -offset, -offset );
		zoom( zoom, zoom );
		draw( new XRingIcon() );
		reset();

		setFillPaint( Color.BLACK );

		// Draw the program name
		setTextAlign( TextAlignment.CENTER );
		fillText( "Xenon", 2, 0.5, 0.5, 2 );

		// Draw the program web address
		setTextAlign( TextAlignment.CENTER );
		fillText( "www.xeomar.com", 2, 13.0 / 16, 3.0 / 16.0, 2 );
	}

	public static void main( String[] commands ) {
		//proof( new InstallerBannerImage() );
		save( new InstallerBannerImage(), "../../software/xenon/source/main/izpack/banner.png" );
	}

}
