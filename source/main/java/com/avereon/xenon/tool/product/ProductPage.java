package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.xenon.Program;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

abstract class ProductPage extends ProductToolPage {

	private ProductTool productTool;

	private List<ProductPane> sources;

	ProductPage( Program program, ProductTool productTool ) {
		this.productTool = productTool;
		sources = new CopyOnWriteArrayList<>();
		showUpdating();
	}

	protected void showUpdating() {
		getChildren().clear();
		getChildren().addAll( new Label( "Updating..." ) );
	}

	List<ProductPane> getSourcePanels() {
		return Collections.unmodifiableList( sources );
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, Map.of() );
	}

	void setProducts( List<ProductCard> cards, Map<String, ProductCard> productUpdates ) {
		// Add a product pane for each card
		sources.clear();
		sources.addAll( getTool()
			.createSourceList( cards )
			.stream()
			.map( ( source ) -> new ProductPane( getTool(), source, productUpdates.get( source.getProductKey() ) ) )
			.collect( Collectors.toList() ) );

		getChildren().clear();
		getChildren().addAll( sources );

		updateProductStates();
	}

	private ProductTool getTool() {
		return productTool;
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
