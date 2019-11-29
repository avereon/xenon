package com.avereon.xenon.tool.product;

import com.avereon.xenon.Program;
import javafx.scene.control.Button;

class UpdatesPage extends ProductPage {

	private ProductTool productTool;

	UpdatesPage( Program program, ProductTool productTool ) {
		super( program, productTool, ProductTool.UPDATES );
		this.productTool = productTool;

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		Button downloadAllButton = new Button( "", program.getIconLibrary().getIcon( "download" ) );

		refreshButton.setOnAction( event -> updateState( true ) );
		downloadAllButton.setOnAction( event -> downloadAllSelected() );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState( boolean force ) {
		ProductTool.log.trace( "Update available updates" );
		productTool.getProgram().getTaskManager().submit( new RefreshUpdatableProducts( productTool, force ) );
	}

	private void downloadAllSelected() {
		getSourcePanels().stream().filter( ProductPane::isSelected ).forEach( ProductPane::updateProduct );
	}

}
