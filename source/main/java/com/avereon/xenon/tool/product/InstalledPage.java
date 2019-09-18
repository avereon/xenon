package com.avereon.xenon.tool.product;

import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramProductType;
import javafx.scene.control.Button;

class InstalledPage extends ProductPage {

	private ProductTool productTool;

	InstalledPage( Program program, ProductTool productTool ) {
		super( program, productTool, ProgramProductType.INSTALLED );
		this.productTool = productTool;

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState( boolean force ) {
		ProductTool.log.trace( "Update installed products" );
		productTool.getProgram().getTaskManager().submit( new RefreshInstalledProducts( productTool, force ) );
	}

}
