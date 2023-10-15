package com.avereon.xenon.workspace;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

public class StageClickAndDrag implements EventHandler<MouseEvent> {

	//private final Stage stage;

	private double anchorX;

	private double anchorY;

	private boolean skipMouseDragged;

	private final DragHandler handler;

	StageClickAndDrag( Node node, DragHandler handler ) {
		this.handler = handler;
		node.addEventFilter( MouseEvent.MOUSE_PRESSED, this );
		node.addEventFilter( MouseEvent.MOUSE_DRAGGED, this );
	}

	public Window getWindow( MouseEvent event ) {
		return event.getPickResult().getIntersectedNode().getScene().getWindow();

	}

	@Override
	public void handle( MouseEvent event ) {
		Window stage = event.getPickResult().getIntersectedNode().getScene().getWindow();

		if( event.getEventType() == MouseEvent.MOUSE_PRESSED ) {
			anchorX = stage.getX() - event.getScreenX();
			anchorY = stage.getY() - event.getScreenY();
			skipMouseDragged = false;

//			// TODO Move to title bar drag handler
//			boolean shouldToggleMaximize = !event.isDragDetect() && event.getClickCount() == 2;
//			if( shouldToggleMaximize ) toggleMaximize();
		} else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED ) {
			if( skipMouseDragged ) return;
			stage.setX( anchorX + event.getScreenX() );
			stage.setY( anchorY + event.getScreenY() );

//			// TODO Move to title bar drag handler
//			if( stage.isMaximized() ) unMaximize( event.getX() );
		}

		//handler.handle( mouseEvent );
	}

	public interface DragHandler {

		void handle( MouseEvent event, Stage window, double screenX, double screenY );

	}

}
