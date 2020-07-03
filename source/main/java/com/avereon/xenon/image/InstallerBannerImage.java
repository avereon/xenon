package com.avereon.xenon.image;

import com.avereon.product.ProductCard;
import com.avereon.rossa.icon.XRingLargeIcon;
import com.avereon.venza.image.RenderedImage;
import com.avereon.venza.image.Theme;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.net.URL;

public class InstallerBannerImage extends RenderedImage {

	private ProductCard card;

	private URL providerUrl;

	@SuppressWarnings( "WeakerAccess" )
	public InstallerBannerImage() {
		setWidth( 540 );
		setHeight( 180 );

		try {
			card = new ProductCard().jsonCard( getClass() );
			providerUrl = new URL( card.getProviderUrl() );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

	@Override
	protected void render() {
		double scale = 0.9;
		double offset = 0.5 - (scale * 0.5);
		move( offset, offset );
		zoom( scale, scale );
		render( new XRingLargeIcon() );
		reset();

		// Draw the program name
		setFillPaint( getStrokePaint() );
		setTextAlign( TextAlignment.CENTER );
		fillText( card.getName(), 2.0, 0.6, 0.65, 2.0 );

		// Draw the program web address
		setFillPaint( getStrokePaint() );
		setTextAlign( TextAlignment.CENTER );
		fillText( providerUrl.getHost(), 2.0, 0.85, 0.2, 2 );
	}

	public static void main( String[] commands ) {
		RenderedImage image = new InstallerBannerImage();
		image.setTheme( Theme.LIGHT );
		image.relocate( 50,50 );
		proof( image, image.getWidth() + 100, image.getHeight() + 100, Color.web( "#E0E0E0") );
	}

}
