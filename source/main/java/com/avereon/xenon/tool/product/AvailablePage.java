package com.avereon.xenon.tool.product;

import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramProductType;
import javafx.scene.control.Button;

class AvailablePage extends ProductPage {

	private ProductTool productTool;

	AvailablePage( Program program, ProductTool productTool ) {
		super( program, productTool );
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.AVAILABLE ) );

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> productTool.getProgram().getTaskManager().submit( new RefreshAvailableProducts( productTool, true ) ) );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.trace( "Update available products" );
		productTool.getProgram().getTaskManager().submit( new RefreshAvailableProducts( productTool ) );
	}

}
