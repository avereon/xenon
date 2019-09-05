package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Should be run on the FX platform thread.
 */
class RefreshInstalledProducts extends Task<Void> {

	private ProductTool productTool;

	public RefreshInstalledProducts( ProductTool productTool ) {this.productTool = productTool;}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		try {
			List<ProductCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().getInstalledProductCards() );
			cards.sort( new ProgramProductCardComparator( productTool.getProgram(), ProductCardComparator.Field.NAME ) );
			Platform.runLater( () -> productTool.getInstalledPage().setProducts( cards ) );
		} catch( Exception exception ) {
			ProductTool.log.warn( "Error refreshing installed products", exception );
			// TODO Notify the user there was a problem refreshing the installed products
		}
		return null;
	}

}
