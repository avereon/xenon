package com.avereon.xenon.tool.product;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import javafx.scene.control.Button;

import java.lang.System.Logger;

class UpdatesPage extends ProductPage {

	private static final Logger log = Log.get();

	private ProductTool productTool;

	UpdatesPage( Program program, ProductTool productTool ) {
		super( program, productTool, ProductTool.UPDATES );
		this.productTool = productTool;

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );
		Button downloadAllButton = new Button( "", program.getIconLibrary().getIcon( "download" ) );
		downloadAllButton.setOnAction( event -> updateProducts( getSourcePanels() ) );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState( boolean force ) {
		ProductTool.log.log( Log.TRACE,  "Update available updates" );
		productTool.getProgram().getTaskManager().submit( new RefreshUpdatableProducts( productTool, force ) );
	}

}
