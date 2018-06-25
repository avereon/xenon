package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.UiFactory;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

abstract class ProductToolPage extends BorderPane {

	private Label title;

	private HBox buttonBox;

	private VBox nodes;

	public ProductToolPage() {
		setId( "tool-product-panel" );

		title = new Label( "" );
		title.setId( "tool-product-page-title" );

		// Can be styled with -fx-spacing
		buttonBox = new HBox( UiFactory.PAD );

		BorderPane header = new BorderPane( null, null, buttonBox, null, title );
		header.prefWidthProperty().bind( this.widthProperty() );
		header.getStyleClass().add( "tool-product-page-header" );

		setTop( header );
	}

	protected void setTitle( String title ) {
		this.title.setText( title );
	}

	protected ObservableList<Node> getButtonBox() {
		return buttonBox.getChildren();
	}

	protected abstract void updateState();

}
