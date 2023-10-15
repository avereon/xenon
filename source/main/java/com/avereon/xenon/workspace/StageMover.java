package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.util.List;

/**
 * The StageMover is a mouse event handler to drag a Stage around the screen.
 * Simply create the {@link StageMover} on the node to use for dragging, then
 * add that node to the stage screen graph.
 */
@CustomLog
public class StageMover implements javafx.event.EventHandler<MouseEvent> {

	private double anchorX;

	private double anchorY;

	private boolean skipMouseDragged;

	public StageMover( Node node ) {
		node.addEventFilter( MouseEvent.MOUSE_PRESSED, this );
		node.addEventFilter( MouseEvent.MOUSE_DRAGGED, this );
	}

	@Override
	public void handle( MouseEvent event ) {
		Stage stage = getStage( event );
		if( event.getEventType() == MouseEvent.MOUSE_PRESSED ) {
			boolean shouldToggleMaximize = !event.isDragDetect() && event.getClickCount() == 2;

			if( shouldToggleMaximize ) toggleMaximize( stage );

			anchorX = stage.getX() - event.getScreenX();
			anchorY = stage.getY() - event.getScreenY();
			skipMouseDragged = false;
		} else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED ) {
			if( skipMouseDragged ) return;

			if( stage.isMaximized() ) unMaximize( stage, event.getX() );

			stage.setX( anchorX + event.getScreenX() );
			stage.setY( anchorY + event.getScreenY() );
		}
	}

	public Stage getStage( MouseEvent event ) {
		return (Stage)((Node)event.getSource()).getScene().getWindow();
	}

	private void toggleMaximize( Stage stage ) {
		if( stage.isMaximized() ) {
			stage.setMaximized( false );
		} else {
			stage.setMaximized( true );
			skipMouseDragged = true;
		}
	}

	private void unMaximize( Stage stage, double eventX ) {
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
