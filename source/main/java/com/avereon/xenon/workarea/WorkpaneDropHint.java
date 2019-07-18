package com.avereon.xenon.workarea;

import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;

public class WorkpaneDropHint extends Rectangle {

	public WorkpaneDropHint( Bounds bounds) {
		super( bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight() );
		getStyleClass().add( "work-pane-drop-hint");
		setMouseTransparent( true );
	}

}
