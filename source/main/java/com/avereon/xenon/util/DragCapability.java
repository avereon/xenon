package com.avereon.xenon.util;

import javafx.geometry.Point3D;
import javafx.scene.Node;

public class DragCapability {

	private Point3D origin;

	private Point3D anchor;

	private DragCapability( Node node ) {
		node.setOnMousePressed( e -> {
			origin = new Point3D( node.getLayoutX(), node.getLayoutY(), 0 );
			anchor = new Point3D( e.getSceneX(), e.getSceneY(), 0 );
		} );

		node.setOnMouseDragged( e -> {
			Point3D offset = new Point3D( e.getSceneX() - anchor.getX(), e.getSceneY() - anchor.getY(), 0 );
			node.setLayoutX( origin.getX() + offset.getX() );
			node.setLayoutY( origin.getY() + offset.getY() );
		} );
	}

	public static void add( Node node ) {
		new DragCapability( node );
	}

}
