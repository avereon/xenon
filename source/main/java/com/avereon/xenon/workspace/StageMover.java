package com.avereon.xenon.workspace;

import com.avereon.zerra.javafx.Fx;
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
public class StageMover {

	private static final double DRAG_DISTANCE_THRESHOLD = 10;

	private StageMover( Node node ) {
		node.addEventFilter( MouseEvent.MOUSE_PRESSED, this::handlePress );

		// FIXME Why did I split this into two classes?
		new StageDragContext( node, this::handleDrag );
	}

	public static <T extends Node> T of(T node) {
		new StageMover( node );
		node.getStyleClass().add( "stage-mover" );
		return node;
	}

	private void handlePress( MouseEvent event ) {
		boolean shouldToggleMaximize = !event.isDragDetect() && event.getClickCount() == 2;
		if( shouldToggleMaximize ) toggleMaximize( Fx.getStage( event ) );
	}

	private void handleDrag( StageDragContext.DragData data ) {
		Stage stage = (Stage)data.window();
		double dragThreshold = DRAG_DISTANCE_THRESHOLD * stage.getOutputScaleX();

		if( stage.isMaximized() && data.dragDistance() > dragThreshold ) unMaximize( data.handler(), stage, data.event() );

		stage.setX( data.windowX() );
		stage.setY( data.windowY() );
	}

	private void toggleMaximize( Stage stage ) {
		boolean isMaximized = !stage.isMaximized();
		stage.setMaximized( isMaximized );
	}

	private void unMaximize( StageDragContext handler, Stage stage, MouseEvent event ) {
		// Get the screen width
		List<Screen> screens = Screen.getScreensForRectangle( stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight() );
		if( screens.isEmpty() ) return;

		// Calculate what percent of the screen width the mouse pointer is located
		double percent = event.getX() / screens.getFirst().getVisualBounds().getWidth();

		// Normalize the window
		toggleMaximize( stage );

		// Update the offsetX from the stage width to calculate the anchor
		handler.setOffsetX( percent * stage.getWidth() );
	}

}
