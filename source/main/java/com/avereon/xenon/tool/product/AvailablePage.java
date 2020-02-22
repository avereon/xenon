package com.avereon.xenon.tool.product;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import javafx.scene.control.Button;

class AvailablePage extends ProductPage {

	private ProductTool productTool;

	AvailablePage( Program program, ProductTool productTool ) {
		super( program, productTool, ProductTool.AVAILABLE );
		this.productTool = productTool;

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );
		Button downloadAllButton = new Button( "", program.getIconLibrary().getIcon( "download" ) );
		downloadAllButton.setOnAction( event -> installProducts( getSourcePanels() ) );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState( boolean force ) {
		ProductTool.log.log( Log.TRACE,  "Update available products" );
		productTool.getProgram().getTaskManager().submit( new RefreshAvailableProducts( productTool, force ) );
	}

}
