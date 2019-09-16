package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RefreshUpdatableProducts extends Task<Void> {

	private ProductTool productTool;

	private boolean force;

	RefreshUpdatableProducts( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		Platform.runLater( () -> productTool.getUpdatesPage().showUpdating() );
		List<ProductCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().findAvailableUpdates( force ) );
		cards.sort( new ProgramProductCardComparator( productTool.getProgram(), ProductCardComparator.Field.NAME ) );

		//productTool.getUpdatesPage().reloadProducts( cards );
		Map<String, ProductCard> installedProducts = new HashMap<>();
		Map<String, ProductCard> productUpdates = new HashMap<>();

		// Installed product map
		for( ProductCard card : productTool.getProgram().getProductManager().getInstalledProductCards( false ) ) {
			installedProducts.put( card.getProductKey(), card );
		}

		// Product update map
		for( ProductCard card : cards ) {
			productUpdates.put( card.getProductKey(), card );
		}

		// Installed product list
		List<ProductCard> newCards = new ArrayList<>();
		for( ProductCard card : cards ) {
			newCards.add( installedProducts.get( card.getProductKey() ) );
		}

		Platform.runLater( () -> productTool.getUpdatesPage().setProducts( newCards, productUpdates ) );

		return null;
	}

}
