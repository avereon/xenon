package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskManager;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

class RefreshUpdatableProducts extends Task<Void> {

	private ProductTool productTool;

	private boolean force;

	RefreshUpdatableProducts( ProductTool productTool ) {
		this( productTool, false );
	}

	RefreshUpdatableProducts( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		try {
			List<ProductCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().findPostedUpdates( force ) );
			cards.sort( new ProductCardComparator( productTool.getProgram(), ProductCardComparator.Field.NAME ) );
			Platform.runLater( () -> productTool.getUpdatesPage().setProducts( cards, true ) );
		} catch( Exception exception ) {
			ProductTool.log.warn( "Error checking for updates", exception );
			// TODO Notify the user there was a problem getting posted updates
		}
		return null;
	}

}
