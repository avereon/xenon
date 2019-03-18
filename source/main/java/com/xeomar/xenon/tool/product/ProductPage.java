package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Program;
import javafx.scene.Node;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class ProductPage extends ProductToolPage {

	private ProductTool productTool;

	private List<ProductPane> sources;

	ProductPage( Program program, ProductTool productTool ) {
		this.productTool = productTool;
		sources = new CopyOnWriteArrayList<>();
	}

	List<ProductPane> getSourcePanels() {
		return Collections.unmodifiableList( sources );
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, false );
	}

	void setProducts( List<ProductCard> cards, boolean isUpdate ) {
		// Create a map of the updates
		Map<String, ProductCard> installedProducts = new HashMap<>();
		Map<String, ProductCard> productUpdates = new HashMap<>();
		if( isUpdate ) {
			// Installed product map
			for( ProductCard card : productTool.getProgram().getProductManager().getProductCards() ) {
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
			cards = newCards;
		}

		// Add a product pane for each card
		sources.clear();
		for( ProductCard source : productTool.createSourceList( cards ) ) {
			sources.add( new ProductPane( productTool, source, productUpdates.get( source.getProductKey() ) ) );
		}

		getChildren().clear();
		getChildren().addAll( sources );

		updateProductStates();
	}

	private void updateProductStates() {
		for( Node node : getChildren() ) {
			((ProductPane)node).updateProductState();
		}
	}

	private void updateProductState( ProductCard card ) {
		for( Node node : getChildren() ) {
			ProductPane panel = (ProductPane)node;
			if( panel.getSource().equals( card ) ) panel.updateProductState();
		}
	}

}
