package com.avereon.xenon.workspace;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The StageMover is a Node intended to be used to drag a Stage around the
 * screen. Simply create the {@link StageMover} for the Stage to move, then add
 * the mover to that stage's screen graph.
 */
public class StageMover extends Pane implements javafx.event.EventHandler<MouseEvent> {

	private final Stage stage;

	private double anchorX, anchorY;

	StageMover( Stage stage ) {
		this.stage = stage;
		addEventFilter( MouseEvent.MOUSE_PRESSED, this );
		addEventFilter( MouseEvent.MOUSE_DRAGGED, this );
	}

	@Override
	public void handle( MouseEvent event ) {
		// FIXME Double-click should toggle the maximized flag
		// FIXME When maximized, dragging should switch back to normal
		if( event.getEventType() == MouseEvent.MOUSE_PRESSED ) {
			anchorX = stage.getX() - event.getScreenX();
			anchorY = stage.getY() - event.getScreenY();
			event.consume();
		} else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED ) {
			stage.setX( anchorX + event.getScreenX() );
			stage.setY( anchorY + event.getScreenY() );
			event.consume();
		}
	}

}
