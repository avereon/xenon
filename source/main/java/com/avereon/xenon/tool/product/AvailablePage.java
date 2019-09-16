package com.avereon.xenon.tool.product;

import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramProductType;
import javafx.scene.control.Button;

class AvailablePage extends ProductPage {

	private ProductTool productTool;

	AvailablePage( Program program, ProductTool productTool ) {
		super( program, productTool, ProgramProductType.AVAILABLE );
		this.productTool = productTool;

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState( boolean force ) {
		ProductTool.log.trace( "Update available products" );
		productTool.getProgram().getTaskManager().submit( new RefreshAvailableProducts( productTool, force ) );
	}

}
