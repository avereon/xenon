package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;

public class AboutTool extends ProductInfoTool {

	public AboutTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-about" );
	}

}
