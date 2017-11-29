package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactTool extends com.xeomar.xenon.ProductTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	public ArtifactTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-artifact" );

		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );
	}

	// TODO Register a listener to listen for updates available events

}
