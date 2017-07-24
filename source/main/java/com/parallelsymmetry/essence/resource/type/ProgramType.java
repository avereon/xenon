package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.ResourceType;

/**
 * This resource type is used to register tools that belong to the program
 * such as the GuideTool.
 */
public class ProgramType extends ResourceType {

	public ProgramType( Product product ) {
		super( product, "program" );
	}

}
