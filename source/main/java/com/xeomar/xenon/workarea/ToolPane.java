package com.xeomar.xenon.workarea;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

class ToolPane extends TabPane {

	ToolPane() {
		this( (Tab[])null );
	}

	private ToolPane( Tab... tabs ) {
		super( tabs );

		// NEXT Add mouse listeners to handle drag and drop
		// Tab D&D support: https://bugs.openjdk.java.net/browse/JDK-8092098

		setOnMousePressed( event -> {
			//System.out.println( "Mouse pressed: event=" + event );

			// NEXT Try to figure out what tab was pressed...
			System.out.println( "Tab: " + getTab( event ) );

		} );

		//		setOnMouseDragged( event -> {
		//			//System.out.println("Mouse dragged: source=" + event.getSource() + " target=" + event.getTarget() );
		//		} );
		//
		//		setOnMouseReleased( event -> {
		//			System.out.println( "Mouse released: event=" + event );
		//		});

		// TODO Make this a user setting
		setTabClosingPolicy( TabPane.TabClosingPolicy.ALL_TABS );
	}

	private Tab getTab( MouseEvent event ) {
		Node node = (Node)event.getTarget();
		while( node != null ) {
			//System.out.println( "node: " + node );
			//TabHeaderSkin skin = event.getSource();

//			if( node.contains( event.getX(), event.getY() ) ) {
//				System.out.println( "Found node by coordinates: " + node );
//			}

			if( node.getStyleClass().contains( "tab" ) ) {
				System.out.println( "Found node by style: " + node );
			}

			node = node.getParent();
		}
		return null;
	}

}
