package com.avereon.xenon.tool.product;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

@Deprecated
abstract class ProductToolPage extends VBox {

	private ProductPageHeader header;

	ProductToolPage() {
		setId( "tool-product-page" );
		header = new ProductPageHeader();
	}

	protected abstract void showUpdating();

	protected abstract void updateState( boolean force );

	protected String getTitle() {
		return header.getTitle().getText();
	}

	protected void setTitle( String title ) {
		header.getTitle().setText( title );
	}

	ProductPageHeader getHeader() {
		return header;
	}

	ObservableList<Node> getButtonBox() {
		return header.getButtons().getChildren();
	}

}
