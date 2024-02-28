package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductList extends VBox {

	private final ProductsSettingsPanel parent;

	private final DisplayMode displayMode;

	private final List<ProductTile> sources;

	private final Labeled message;

	private final String refreshMessage;

	private final String missingMessage;

	public ProductList( ProductsSettingsPanel parent, DisplayMode displayMode ) {
		this.parent = parent;
		this.displayMode = displayMode;
		this.sources = new CopyOnWriteArrayList<>();

		getStyleClass().addAll( "tool-product-list" );

		String mode = displayMode.name().toLowerCase();
		this.refreshMessage = Rb.text( RbKey.TOOL, "product-" + mode + "-refresh" );
		this.missingMessage = Rb.text( RbKey.TOOL, "product-" + mode + "-missing" );

		this.message = new Label();
		this.message.setPrefWidth( Double.MAX_VALUE );
		this.message.getStyleClass().addAll( "tool-product-message" );

		getChildren().add( message );

		showUpdating();
	}

	protected void showUpdating() {
		showMessage( refreshMessage );
	}

	private void showMissing() {
		showMessage( missingMessage );
	}

	private void showMessage( String messageText ) {
		getChildren().clear();
		message.setText( messageText );
		getChildren().addAll( message );
	}

	private void hideMessage() {
		getChildren().remove( message );
	}

	private void updateProductStates() {
		for( Node node : getChildren() ) {
			if( node instanceof ProductTile pane ) pane.updateProductState();
		}
	}

	List<ProductTile> getSourcePanels() {
		return Collections.unmodifiableList( sources );
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, Map.of() );
	}

	void setProducts( List<ProductCard> cards, Map<String, ProductCard> productUpdates ) {
		if( cards.isEmpty() ) {
			showMissing();
		} else {
			// Add a product pane for each card
			sources.clear();
			sources.addAll(
				parent.createSourceList( cards )
				.stream()
				.map( ( source ) -> new ProductTile( parent.getProduct(), parent, source, productUpdates.get( source.getProductKey() ), displayMode ) )
				.toList() );

			getChildren().clear();
			getChildren().addAll( sources );
			updateProductStates();
		}
	}
}
