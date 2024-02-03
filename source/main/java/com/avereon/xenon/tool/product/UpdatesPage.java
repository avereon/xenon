package com.avereon.xenon.tool.product;

import com.avereon.xenon.Xenon;
import javafx.scene.control.Button;
import lombok.CustomLog;

@CustomLog
class UpdatesPage extends ProductPage {

	private final ProductTool productTool;

	UpdatesPage( Xenon program, ProductTool productTool ) {
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
		log.atFiner().log( "Update available updates" );
		productTool.getProgram().getTaskManager().submit( new RefreshUpdatableProducts( productTool, force ) );
	}

}
