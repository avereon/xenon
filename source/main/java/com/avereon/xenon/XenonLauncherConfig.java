package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.util.OperatingSystem;

class XenonLauncherConfig {

	private static ProductCard card;

	/**
	 * This is an optimization to allow the program launcher to load as quickly as
	 * possible.
	 *
	 * @return The product info for the program.
	 */
	static ProductCard loadProductInfo() {
		// NEXT Where to load a rebrand file from?
		return updateProductCard( ProductCard.info( Xenon.class ) );
	}

	static ProductCard loadProductCard() {
		// NEXT Where to load a rebrand file from?
		return updateProductCard( ProductCard.card( Xenon.class ) );
	}

	private static ProductCard updateProductCard( ProductCard fullCard ) {
		// Fill out the rest of the product card
		// Some fields may already be populated, like installation folder
		if( card != null ) {
			fullCard.setInstallFolder( card.getInstallFolder() );
		}
		return card = fullCard;
	}

	/**
	 * Set the custom launcher system property.
	 * See {@link OperatingSystem#getJavaLauncherPath()} for more information.
	 */
	static void setCustomLauncherSystemProperty() {
		if( System.getProperty( OperatingSystem.CUSTOM_LAUNCHER_PATH ) != null ) {
			System.setProperty( OperatingSystem.CUSTOM_LAUNCHER_NAME, loadProductInfo().getName() );
		}
	}

}
