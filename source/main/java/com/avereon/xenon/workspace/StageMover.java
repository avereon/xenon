package com.avereon.xenon.workspace;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.util.List;

/**
 * The StageMover is a Node intended to be used to drag a Stage around the
 * screen. Simply create the {@link StageMover} for the Stage to move, then add
 * the mover to that stage's screen graph.
 */
@CustomLog
public class StageMover extends Pane implements javafx.event.EventHandler<MouseEvent> {

	private final Stage stage;

	private double anchorX;

	private double anchorY;

	private boolean skipMouseDragged;

	StageMover( Stage stage ) {
		this.stage = stage;
		addEventFilter( MouseEvent.MOUSE_PRESSED, this );
		addEventFilter( MouseEvent.MOUSE_DRAGGED, this );
	}

	@Override
	public void handle( MouseEvent event ) {
		if( event.getEventType() == MouseEvent.MOUSE_PRESSED ) {
			boolean shouldToggleMaximize = !event.isDragDetect() && event.getClickCount() == 2;

			if( shouldToggleMaximize ) toggleMaximize();

			anchorX = stage.getX() - event.getScreenX();
			anchorY = stage.getY() - event.getScreenY();
			skipMouseDragged = false;
		} else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED ) {
			if( skipMouseDragged ) return;

			if( stage.isMaximized() ) unMaximize( event.getX() );

			stage.setX( anchorX + event.getScreenX() );
			stage.setY( anchorY + event.getScreenY() );
		}
	}

	private void toggleMaximize() {
		if( stage.isMaximized() ) {
			stage.setMaximized( false );
		} else {
			stage.setMaximized( true );
			skipMouseDragged = true;
		}
	}

	private void unMaximize( double eventX ) {
		// Get the screen width
		List<Screen> screens = Screen.getScreensForRectangle( stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight() );
		if( screens.isEmpty() ) return;

		// Calculate what percent of the screen/stage width the mouse pointer is located
		double percent = eventX / screens.get( 0 ).getVisualBounds().getWidth();

		// Calculate the xOffset from the stage width to calculate the anchor
		double xOffset = stage.getX() + percent * stage.getWidth();

		// Update the anchorX based on percent offset
		anchorX = stage.getX() - xOffset;

		stage.setMaximized( false );
	}

}
