package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.Arrays;

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

	public void setLeft( Node... nodes ) {
		leftStatusBarItems.getChildren().clear();
		addLeft( nodes );
	}

	public void addLeft( Node... nodes ) {
		Arrays.stream( nodes ).filter( n -> !leftStatusBarItems.getChildren().contains( n ) ).forEach( n -> leftStatusBarItems.getChildren().add( n ) );
	}

	public void removeLeft( Node node ) {
		leftStatusBarItems.getChildren().remove( node );
	}

	public void setRight( Node... nodes ) {
		rightStatusBarItems.getChildren().clear();
		addRight( nodes );
	}

	public void addRight( Node... nodes ) {
		Arrays.stream( nodes ).filter( n -> !rightStatusBarItems.getChildren().contains( n ) ).forEach( n -> rightStatusBarItems.getChildren().add( n ) );
	}

	public void removeRight( Node node ) {
		rightStatusBarItems.getChildren().remove( node );
	}

}
