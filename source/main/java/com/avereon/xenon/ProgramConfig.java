package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.util.OperatingSystem;

class ProgramConfig {

	private static ProductCard card;

	static ProductCard loadProductInfo() {
		if( card == null ) card = ProductCard.info( Xenon.class );
		return card;
	}

	static void configureCustomLauncherName() {
		ProductCard card = loadProductInfo();

		// Java 11-16
		if( System.getProperty( OperatingSystem.CUSTOM_LAUNCHER_PATH ) != null ) System.setProperty( OperatingSystem.CUSTOM_LAUNCHER_NAME, card.getName() );
	}

}
