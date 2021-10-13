package com.avereon.xenon;

import com.avereon.product.ProductCard;

import java.net.URI;
import java.nio.file.Path;

class ProgramConfig {

	private static ProductCard card;

	static ProductCard loadProductInfo() {
		if( card == null ) card = ProductCard.info( Program.class );
		return card;
	}

	static void configureCustomLauncherName() {
		ProductCard card = loadProductInfo();

		// Java 11-16
		if( System.getProperty( "java.launcher.path" ) != null ) System.setProperty( "java.launcher.name", card.getName() );

		// Java 17
		if( System.getProperty( "jpackage.app-path" ) != null ) {
			Path jpackageAppPath = Path.of( URI.create( System.getProperty( "jpackage.app-path" ) ) );
			System.setProperty( "java.launcher.path", jpackageAppPath.getParent().toString() );
			System.setProperty( "java.launcher.name", jpackageAppPath.getFileName().toString() );
		}
	}

}
