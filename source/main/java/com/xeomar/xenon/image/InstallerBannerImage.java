package com.xeomar.xenon.image;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramImage;
import com.xeomar.xenon.icon.WingDiscLargeIcon;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.net.URL;

public class InstallerBannerImage extends ProgramImage {

	public InstallerBannerImage() {
		setWidth( 900 );
		setHeight( 300 );
	}

	@Override
	protected void render() {
		double zoom = 0.9;
		double offset = (zoom * 0.5) - 0.5;
		move( -offset, -offset );
		zoom( zoom, zoom );
		draw( new WingDiscLargeIcon() );
		reset();

		setFillPaint( Color.BLACK );

		ProductCard card = new ProductCard();
		String providerUrl = "";
		try {
			card.load( Program.class );
			providerUrl = new URL( card.getProviderUrl()).getHost();
		} catch( Exception exception ) {
			exception.printStackTrace();
		}

		// Draw the program name
		setTextAlign( TextAlignment.CENTER );
		fillText( card.getName(), 2, 0.5, 0.5, 2 );

		// Draw the program web address
		setTextAlign( TextAlignment.CENTER );
		fillText( providerUrl, 2, 13.0 / 16, 3.0 / 16.0, 2 );
	}

	public static void main( String[] commands ) {
		proof( new InstallerBannerImage() );
		//save( new InstallerBannerImage(), "../../software/xenon/source/main/izpack/banner.png" );
	}

}
