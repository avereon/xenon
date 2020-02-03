package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;

public class AssetTool extends ProgramTool {

	public AssetTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-asset" );

		setGraphic( product.getProgram().getIconLibrary().getIcon( "asset" ) );
	}

}
