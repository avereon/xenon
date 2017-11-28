package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductTool extends com.xeomar.xenon.ProductTool {

	private static final Logger log = LoggerFactory.getLogger( ProductTool.class );

	public ProductTool( Product product, Resource resource ) {
		super( product, resource );
	}

	// TODO Register a listener to listen for updates available events

}
