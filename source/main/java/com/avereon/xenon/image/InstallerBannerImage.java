package com.avereon.xenon.image;

import com.avereon.product.ProductCard;
import com.avereon.zenna.icon.XRingLargeIcon;
import com.avereon.zerra.image.Proof;
import com.avereon.zerra.image.RenderedImage;
import com.avereon.zerra.style.Theme;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.net.URL;

public class InstallerBannerImage extends RenderedImage {

	private static final double grid = DEFAULT_GRID;

	private ProductCard card;

	private URL providerUrl;

	@SuppressWarnings( "WeakerAccess" )
	public InstallerBannerImage() {
		// The grid of the icon that is used is 32x32 so that is a natural starting
		// point for the grid of the banner. However, since the banner is three
		// times wider than the height then the grid also needs to be three times
		// wider than the height. Therefore, the grid is 96x32.
		super( 3 * grid, grid );

		// The width and height should maintain the grid width to height (3:1)
		// ratio. Otherwise, it will look squished.
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
		fillText( card.getName(), 2 * grid, 0.6 * grid, 0.65 * grid, 2 * grid );

		// Draw the program web address
		setFillPaint( getStrokePaint() );
		setTextAlign( TextAlignment.CENTER );
		fillText( providerUrl.getHost(), 2 * grid, 0.85 * grid, 0.2 * grid, 2 * grid );
	}

	public static void main( String[] commands ) {
		RenderedImage image = new InstallerBannerImage();
		image.setTheme( Theme.LIGHT );
		image.relocate( 50, 50 );
		Proof.proof( image, image.getWidth() + 100, image.getHeight() + 100, Color.web( "#E0E0E0" ) );
	}

}
