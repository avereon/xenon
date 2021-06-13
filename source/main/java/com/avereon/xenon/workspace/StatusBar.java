package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatusBar extends BorderPane {

	private final HBox leftStatusBarItems;

	private final HBox rightStatusBarItems;

	private final HBox leftToolStatusItems;

	private final HBox rightToolStatusItems;

	public StatusBar() {
		getStyleClass().addAll( "status-bar" );

		leftStatusBarItems = new HBox();
		leftStatusBarItems.getStyleClass().addAll( "box" );

		leftToolStatusItems = new HBox();

		rightToolStatusItems = new HBox();

		rightStatusBarItems = new HBox();
		rightStatusBarItems.getStyleClass().addAll( "box" );

		setLeft( leftStatusBarItems );
		setCenter( new BorderPane( null, null, rightToolStatusItems, null, leftToolStatusItems ) );
		setRight( rightStatusBarItems );
	}

	public void setLeftToolItems( Node... nodes ) {
		leftToolStatusItems.getChildren().clear();
		leftToolStatusItems.getChildren().addAll( nodes );
	}

	public void setRightToolItems( Node... nodes ) {
		rightToolStatusItems.getChildren().clear();
		rightToolStatusItems.getChildren().addAll( nodes );
	}

	public void addLeftItems( Node... nodes ) {
		Arrays.stream( nodes ).filter( n -> !leftStatusBarItems.getChildren().contains( n ) ).forEach( n -> leftStatusBarItems.getChildren().add( n ) );
	}

	public void removeLeftItems( Node... node ) {
		leftStatusBarItems.getChildren().removeAll( node );
	}

	public void addRightItems( Node... nodes ) {
		List<Node> items = Arrays.asList( nodes );
		Collections.reverse( items );
		items.stream().filter( n -> !rightStatusBarItems.getChildren().contains( n ) ).forEach( n -> rightStatusBarItems.getChildren().add( 0, n ) );
	}

	public void removeRightItems( Node node ) {
		rightStatusBarItems.getChildren().remove( node );
	}

}
