package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramProductType;
import javafx.scene.control.Button;

class AvailablePage extends ProductPage {

	private ProductTool productTool;

	AvailablePage( Program program, ProductTool productTool ) {
		super( program, productTool );
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.AVAILABLE ) );

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> productTool.getProgram().getExecutor().submit( new UpdateAvailableProducts( true ) ) );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.trace( "Update available products" );
		productTool.getProgram().getExecutor().submit( new UpdateAvailableProducts() );
	}

}
