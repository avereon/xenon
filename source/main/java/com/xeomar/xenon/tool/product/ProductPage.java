package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Program;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class ProductPage extends ProductToolPage {

	private ProductTool productTool;

	private List<ProductPane> sources;

	private VBox productList;

	public ProductPage( Program program, ProductTool productTool ) {
		this.productTool = productTool;
		sources = new CopyOnWriteArrayList<>();
		setCenter( productList = new VBox() );
	}

	public List<ProductPane> getSourcePanels() {
		return Collections.unmodifiableList( sources );
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, false );
	}

	public void setProducts( List<ProductCard> cards, boolean isUpdate ) {
		// Create a map of the updates
		Map<String, ProductCard> installedProducts = new HashMap<>();
		Map<String, ProductCard> productUpdates = new HashMap<>();
		if( isUpdate ) {
			// Installed product map
			for( ProductCard card : productTool.getProgram().getUpdateManager().getProductCards() ) {
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

		productList.getChildren().clear();
		productList.getChildren().addAll( sources );

		updateProductStates();
	}

	void updateProductStates() {
		for( Node node : productList.getChildren() ) {
			((ProductPane)node).updateProductState();
		}
	}

	public void updateProductState( ProductCard card ) {
		for( Node node : productList.getChildren() ) {
			ProductPane panel = (ProductPane)node;
			if( panel.getSource().equals( card ) ) panel.updateProductState();
		}
	}

}
