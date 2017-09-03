package com.xeomar.xenon.product;

import com.xeomar.xenon.Program;

import java.io.File;

public interface Product {

	Program getProgram();

	ProductMetadata getMetadata();

	ClassLoader getClassLoader();

	ProductBundle getResourceBundle();

	/**
	 * Get the shared product data folder. This is the location where the product
	 * should be able to store files that are specific to the product. This path
	 * is operating system specific and can be different between different
	 * versions of operating system.
	 * <p>
	 * Note: This folder is shared by multiple instances of the product.
	 *
	 * @return The product data folder.
	 */
	File getDataFolder();

}
