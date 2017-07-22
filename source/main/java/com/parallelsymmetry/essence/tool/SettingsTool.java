package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.worktool.ToolInfo;

public class SettingsTool extends ProductTool {

	private static ToolInfo toolInfo = new ToolInfo();

	static {
		getToolInfo().addRequiredToolClass( GuideTool.class );
	}

	public SettingsTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-settings" );

		setTitle( product.getResourceBundle().getString( "tool", "settings-name" ) );
	}

	public static ToolInfo getToolInfo() {
		return toolInfo;
	}

}
