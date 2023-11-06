package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.CustomLog;

import java.util.List;

/**
 * The StageMover is a mouse event handler to drag a Stage around the screen.
 * Simply create the {@link StageMover} on the node to use for dragging, then
 * add that node to the stage screen graph.
 */
@CustomLog
public class StageMover {

	//private boolean skipMouseDragged;

	public StageMover( Node node ) {
		new StageClickAndDrag( node, this::handleDrag );
		node.addEventFilter( MouseEvent.MOUSE_PRESSED, this::handleClick );
		//node.addEventFilter( MouseEvent.MOUSE_DRAGGED, this::handleDrag );
	}

	private void handleClick( MouseEvent event ) {
		boolean shouldToggleMaximize = !event.isDragDetect() && event.getClickCount() == 2;
		if( shouldToggleMaximize ) toggleMaximize( getStage( event ) );
	}

	private void handleDrag( StageClickAndDrag handler, MouseEvent event, Window window, double windowX, double windowY, double windowW, double windowH, double anchorW, double anchorH ) {
		//if( skipMouseDragged ) return;

		Stage stage = getStage( event );
		if( stage.isMaximized() ) unMaximize( handler, stage, event );
		//skipMouseDragged = false;

		window.setX( windowX );
		window.setY( windowY );
	}

	public static Stage getStage( MouseEvent event ) {
		return (Stage)StageClickAndDrag.getWindow( event );
	}

	private void toggleMaximize( Stage stage ) {
		boolean isMaximized = !stage.isMaximized();
		stage.setMaximized( isMaximized );
		//if( isMaximized ) skipMouseDragged = true;
	}

	private void unMaximize( StageClickAndDrag handler, Stage stage, MouseEvent event ) {
		// Get the screen width
		List<Screen> screens = Screen.getScreensForRectangle( stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight() );
		if( screens.isEmpty() ) return;

		// Calculate what percent of the screen width the mouse pointer is located
		double percent = event.getX() / screens.get( 0 ).getVisualBounds().getWidth();

		// Normalize the window
		toggleMaximize( stage );

		// Update the offsetX from the stage width to calculate the anchor
		handler.setOffsetX( percent * stage.getWidth() );
	}

}
