package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.tool.guide.GuidedTool;

public class ThemesTool extends GuidedTool {

	public ThemesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "themes" ) );
		setTitle( product.rb().text( "tool", "themes-name" ) );
	}

}
