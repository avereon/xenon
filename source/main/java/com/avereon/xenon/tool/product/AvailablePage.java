package com.avereon.xenon.tool.product;

import com.avereon.xenon.Xenon;
import javafx.scene.control.Button;
import lombok.CustomLog;

@CustomLog
@Deprecated
class AvailablePage extends ProductPage {

	private final ProductTool productTool;

	AvailablePage( Xenon program, ProductTool productTool ) {
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
		log.atFiner().log(  "Update available products" );
		productTool.getProgram().getTaskManager().submit( new RefreshAvailableProducts( productTool, force ) );
	}

}
