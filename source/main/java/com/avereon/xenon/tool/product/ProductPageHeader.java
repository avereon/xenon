package com.avereon.xenon.tool.product;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ProductPageHeader extends BorderPane {

	private Label title;

	private HBox buttons;

	public ProductPageHeader() {
		setId( "tool-product-page-header" );

		title = new Label();
		title.setId("tool-product-page-title");

		buttons = new HBox();
		buttons.setId( "tool-product-page-header-buttons" );

		setLeft( title );
		setRight( buttons );
	}

	public Label getTitle() {
		return title;
	}

	public HBox getButtons() {
		return buttons;
	}

}
