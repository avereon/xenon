package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramProductType;
import javafx.application.Platform;
import javafx.scene.control.Button;

class UpdatesPage extends ProductPage {

	private ProductTool productTool;

	UpdatesPage( Program program, ProductTool productTool ) {
		super( program, productTool );
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.UPDATES ) );

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		Button downloadAllButton = new Button( "", program.getIconLibrary().getIcon( "download" ) );

		refreshButton.setOnAction( event -> productTool.getProgram().getExecutor().submit( new UpdateUpdatableProducts( productTool, true ) ) );
		downloadAllButton.setOnAction( event -> downloadAll() );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.trace( "Update available updates" );
		productTool.getProgram().getExecutor().submit( new UpdateUpdatableProducts( productTool ) );
	}

	private void downloadAll() {
		ProductTool.log.trace( "Download all available updates" );
		productTool.getProgram().getExecutor().submit( () -> {
			try {
				productTool.getProgram().getUpdateManager().stagePostedUpdates();
				Platform.runLater( productTool::handleStagedUpdates );
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error staging updates", exception );
			}
		} );
	}
}
