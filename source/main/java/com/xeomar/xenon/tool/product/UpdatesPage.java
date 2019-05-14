package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.update.ProgramProductManager;
import javafx.scene.control.Button;

import java.util.HashSet;
import java.util.Set;

class UpdatesPage extends ProductPage {

	private ProductTool productTool;

	UpdatesPage( Program program, ProductTool productTool ) {
		super( program, productTool );
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.UPDATES ) );

		Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
		Button downloadAllButton = new Button( "", program.getIconLibrary().getIcon( "download" ) );

		refreshButton.setOnAction( event -> productTool.getProgram().getTaskManager().submit( new RefreshUpdatableProducts( productTool, true ) ) );
		downloadAllButton.setOnAction( event -> downloadAllSelected() );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.trace( "Update available updates" );
		productTool.getProgram().getTaskManager().submit( new RefreshUpdatableProducts( productTool ) );
	}

	private Set<ProductCard> getSelectedUpdates() {
		Set<ProductCard> updates = new HashSet<>();
		for( ProductPane pane : getSourcePanels() ) {
			// TODO Only add if selected
			updates.add( pane.getUpdate() );
		}
		return updates;
	}

	private void downloadAllSelected() {
		ProductTool.log.trace( "Download all available updates" );
		ProgramProductManager updateManager = (ProgramProductManager)productTool.getProgram().getProductManager();
		updateManager.applySelectedUpdates( getSelectedUpdates(), true );
	}

}
