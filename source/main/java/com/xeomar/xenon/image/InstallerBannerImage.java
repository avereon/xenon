package com.xeomar.xenon.image;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramImage;
import com.xeomar.xenon.icon.XRingLargeIcon;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.net.URL;

public class InstallerBannerImage extends ProgramImage {

	private ProductCard card;

	private URL providerUrl;

	public InstallerBannerImage() {
		setWidth( 400 );
		setHeight( 400 );

		try {
			card = new ProductCard();
			card.load( Program.class );
			providerUrl = new URL( card.getProviderUrl() );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}

	}

	@Override
	protected void render() {
		double ratio = getHeight() / getWidth();
		double scale = 0.9;
		double offset = 0.5 - (scale * 0.5);
		move( offset, 0.5 * (ratio - 1) + offset );
		zoom( scale, scale );
		draw( new XRingLargeIcon() );
		reset();

//		setFillPaint( Color.web( "#202020" ) );
//
//		// Draw the program name
//		setTextAlign( TextAlignment.CENTER );
//		fillText( card.getName(), 0.5, 0.2 + offset, 0.3, 0.9 );
//
//		// Draw the program web address
//		setTextAlign( TextAlignment.CENTER );
//		fillText( providerUrl.getHost(), 0.5, ratio - offset, 2.5 / 16.0, 2 );
	}

	public static void main( String[] commands ) {
		proof( new InstallerBannerImage() );
		//save( new InstallerBannerImage(), "../../software/xenon/source/main/izpack/banner.png" );
	}

}
