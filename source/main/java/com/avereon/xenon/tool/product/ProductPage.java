package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

abstract class ProductPage extends ProductToolPage {

	private ProductTool productTool;

	private List<ProductPane> sources;

	private Labeled message;

	private String refreshMessage;

	private String missingMessage;

	ProductPage( Program program, ProductTool productTool, String productType ) {
		this.productTool = productTool;
		sources = new CopyOnWriteArrayList<>();
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + productType ) );

		this.refreshMessage = program.getResourceBundle().getString( BundleKey.TOOL, "product-" + productType +"-refresh" );
		this.missingMessage = program.getResourceBundle().getString( BundleKey.TOOL, "product-" + productType +"-missing" );

		message = new Label();
		message.setPrefWidth( Double.MAX_VALUE );
		message.getStyleClass().addAll( "tool-product-message" );

		showUpdating();
	}

	@Override
	protected void showUpdating() {
		showMessage( refreshMessage );
	}

	private void showMissing() {
		showMessage( missingMessage );
	}

	private void showMessage( String messageText ) {
		message.setText( messageText );
		getChildren().clear();
		getChildren().addAll( message );
	}

	List<ProductPane> getSourcePanels() {
		return Collections.unmodifiableList( sources );
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, Map.of() );
	}

	void setProducts( List<ProductCard> cards, Map<String, ProductCard> productUpdates ) {
		if( cards.size() == 0 ) {
			showMissing();
		} else {
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
