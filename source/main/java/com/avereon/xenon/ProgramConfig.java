package com.avereon.xenon;

import com.avereon.product.ProductCard;

import java.io.IOException;

class ProgramConfig {

	private static ProductCard card;

	static ProductCard loadProductCard() {
		if( card == null ) {
			try {
				card = new ProductCard().init( Program.class );
			} catch( IOException exception ) {
				throw new RuntimeException( exception );
			}
		}

		return card;
	}

	static void configureCustomLauncherName() {
		ProductCard card = loadProductCard();
		if( System.getProperty( "java.launcher.path" ) != null ) System.setProperty( "java.launcher.name", card.getName() );
	}

}
