package com.avereon.xenon.tool.product;

import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import javafx.scene.control.Button;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

class UpdatesPage extends ProductPage {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

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
		ProductTool.log.trace( "Update available updates" );
		productTool.getProgram().getTaskManager().submit( new RefreshUpdatableProducts( productTool, force ) );
	}

}
