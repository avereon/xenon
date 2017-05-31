package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.product.Product;

public class ResourceType {

	private Product product;

	private String name;

	private String description;

	public ResourceType( Product product, String key, String descriptionBundleKey ) {
		this.product = product;
		name = product.getResourceBundle().getString( "resource", key + "-name" );
		description = product.getResourceBundle().getString( "resource", key + "-description" );
	}

}
