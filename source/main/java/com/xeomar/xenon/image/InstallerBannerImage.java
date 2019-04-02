package com.xeomar.xenon.image;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.ProgramImage;
import com.xeomar.xenon.icon.XRingLargeIcon;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.net.URL;

public class InstallerBannerImage extends ProgramImage {

	private ProductCard card;

	private URL providerUrl;

	public InstallerBannerImage() {
		setWidth( 540 );
		setHeight( 180 );

		try {
			try( InputStream input = getClass().getResourceAsStream( ProductCard.CARD ) ) {
				card = new ProductCard().load( input, null );
			}
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
		move( offset, offset );
		zoom( scale, scale );
		draw( new XRingLargeIcon() );
		reset();

		// Draw the program name
		setFillPaint( Color.web( "#202020" ) );
		setTextAlign( TextAlignment.CENTER );
		fillText( card.getName(), 2.0, 0.6, 0.65, 2.0 );

		// Draw the program web address
		setFillPaint( Color.web( "#202020" ) );
		setTextAlign( TextAlignment.CENTER );
		fillText( providerUrl.getHost(), 2.0, 0.85, 0.2, 2 );
	}

	public static void main( String[] commands ) {
		proof( new InstallerBannerImage() );
		//save( new InstallerBannerImage(), "../../software/xenon/source/main/izpack/banner.png" );
	}

}
