package com.avereon.xenon;

import com.avereon.product.ProductCard;

class ProgramConfig {

	private static ProductCard card;

	static ProductCard loadProductInfo() {
		if( card == null ) card = ProductCard.info( Program.class );
		return card;
	}

	static void configureCustomLauncherName() {
		ProductCard card = loadProductInfo();
		if( System.getProperty( "java.launcher.path" ) != null ) System.setProperty( "java.launcher.name", card.getName() );
	}

}
