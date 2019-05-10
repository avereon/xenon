package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramProductType;
import javafx.scene.control.Button;

class InstalledPage extends ProductPage {

	private ProductTool productTool;

	InstalledPage( Program program, ProductTool productTool ) {
		super( program, productTool );
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.INSTALLED ) );

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState() );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.trace( "Update installed products" );
		productTool.getProgram().getExecutor().submit( new RefreshInstalledProducts(productTool) );
	}

}
