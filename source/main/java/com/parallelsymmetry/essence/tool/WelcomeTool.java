package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;

public class WelcomeTool extends ProductTool {

	public WelcomeTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-welcome" );
		setTitle( product.getResourceBundle().getString( "tool", "welcome-name" ) );
	}

}
