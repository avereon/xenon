package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.product.ProgramProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.zerra.javafx.Fx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
class RefreshUpdatableProducts extends Task<Void> {

	private final ProductTool productTool;

	private final boolean force;

	RefreshUpdatableProducts( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		Fx.run( () -> productTool.getUpdatesPage().showUpdating() );
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

		Fx.run( () -> productTool.getUpdatesPage().setProducts( newCards, productUpdates ) );

		return null;
	}

}
