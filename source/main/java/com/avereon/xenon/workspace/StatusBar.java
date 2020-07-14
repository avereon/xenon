package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class StatusBar extends BorderPane {

	private HBox leftStatusBarItems;

	private HBox rightStatusBarItems;

	public StatusBar() {
		getStyleClass().addAll( "status-bar" );

		leftStatusBarItems = new HBox();
		leftStatusBarItems.getStyleClass().addAll( "box" );

		rightStatusBarItems = new HBox();
		rightStatusBarItems.getStyleClass().addAll( "box" );

		setLeft( leftStatusBarItems );
		setRight( rightStatusBarItems );
	}

	public void addLeft( Node node ) {
		leftStatusBarItems.getChildren().add( node );
	}

	public void removeLeft( Node node ) {
		leftStatusBarItems.getChildren().remove( node );
	}

	public void addRight( Node node ) {
		rightStatusBarItems.getChildren().add( 0, node );
	}

	public void removeRight( Node node ) {
		rightStatusBarItems.getChildren().remove( node );
	}

}
