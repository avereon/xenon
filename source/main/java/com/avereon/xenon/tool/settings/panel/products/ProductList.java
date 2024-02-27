package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductList extends VBox {

	private List<ProductPane> sources;

	private final Labeled message;

	private final String refreshMessage;

	private final String missingMessage;

	public ProductList( DisplayMode displayMode ) {
		String mode = displayMode.name().toLowerCase();
		this.sources = new CopyOnWriteArrayList<>();

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
			if( node instanceof ProductPane pane ) pane.updateProductState();
		}
	}

}
