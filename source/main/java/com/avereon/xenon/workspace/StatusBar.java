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

	public void setLeftItems( Node... nodes ) {
		leftStatusBarItems.getChildren().clear();
		addLeftItems( nodes );
	}

	public void addLeftItems( Node... nodes ) {
		Arrays.stream( nodes ).filter( n -> !leftStatusBarItems.getChildren().contains( n ) ).forEach( n -> leftStatusBarItems.getChildren().add( n ) );
	}

	public void removeLeftItems( Node... node ) {
		leftStatusBarItems.getChildren().removeAll( node );
	}

	public void setRightItems( Node... nodes ) {
		rightStatusBarItems.getChildren().clear();
		addRightItems( nodes );
	}

	public void addRightItems( Node... nodes ) {
		Arrays.stream( nodes ).filter( n -> !rightStatusBarItems.getChildren().contains( n ) ).forEach( n -> rightStatusBarItems.getChildren().add( n ) );
	}

	public void removeRightItems( Node node ) {
		rightStatusBarItems.getChildren().remove( node );
	}

}
