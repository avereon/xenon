package com.avereon.xenon.tool.product;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import javafx.scene.control.Button;

class InstalledPage extends ProductPage {

	private ProductTool productTool;

	InstalledPage( Program program, ProductTool productTool ) {
		super( program, productTool, ProductTool.INSTALLED );
		this.productTool = productTool;

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState( boolean force ) {
		ProductTool.log.log( Log.TRACE,  "Update installed products" );
		productTool.getProgram().getTaskManager().submit( new RefreshInstalledProducts( productTool, force ) );
	}

}
