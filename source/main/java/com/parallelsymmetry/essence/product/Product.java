package com.parallelsymmetry.essence.product;

import java.io.File;

public interface Product {

	ProductMetadata getMetadata();

	ProductBundle getResourceBundle();

	/**
	 * Get the product data folder. This is the location where the product should
	 * be able to store files that are specific to the product. This path is
	 * operating system specific and can be different between different versions
	 * of operating system.
	 * <p>
	 * Note: This folder is shared by multiple instances of the product.
	 *
	 * @return The product data folder.
	 */
	File getDataFolder();

}
