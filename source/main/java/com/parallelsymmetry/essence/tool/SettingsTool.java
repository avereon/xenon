package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.type.ProgramGuideType;

import java.util.HashSet;
import java.util.Set;

public class SettingsTool extends ProductTool {

	public SettingsTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-settings" );
		setTitle( product.getResourceBundle().getString( "tool", "settings-name" ) );
	}

	public Set<String> getResourceDependencies() {
		Set<String> resources = new HashSet<>();
		resources.add( ProgramGuideType.URI );
		return resources;
	}

}
