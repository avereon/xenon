package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import lombok.CustomLog;

@CustomLog
public class StageClickAndDrag {

	private double originalX;

	private double originalY;

	private double originalW;

	private double originalH;

	private double anchorX;

	private double anchorY;

	private double windowX2;

	private double windowY2;

	private double offsetX;

	private double offsetY;

	private double offsetW;

	private double offsetH;

	private boolean skipMouseDragged;

	private final DragHandler dragHandler;

	StageClickAndDrag( Node node, DragHandler dragHandler ) {
		this.dragHandler = dragHandler;
		node.addEventFilter( MouseEvent.MOUSE_PRESSED, this::handlePressed );
		node.addEventFilter( MouseEvent.MOUSE_DRAGGED, this::handleDragged );
	}

	public static Window getWindow( MouseEvent event ) {
		return ((Node)event.getSource()).getScene().getWindow();
	}

	public void setAnchorX( double x ) {
		this.anchorX = x;

		// Recalculate dependent values
		//offsetX = anchorX - originalX;
		//offsetW = windowX2 - anchorX;
	}

	private void handlePressed( MouseEvent event ) {
		Window stage = getWindow( event );

		// The anchor location in screen coordinates
		anchorX = event.getScreenX();
		anchorY = event.getScreenY();

		originalX = stage.getX();
		originalY = stage.getY();
		originalW = stage.getWidth();
		originalH = stage.getHeight();

		// These are the window width and height locations in screen coordinates
		windowX2 = originalX + originalW;
		windowY2 = originalY + originalH;

		offsetX = anchorX - originalX;
		offsetY = anchorY - originalY;
		offsetW = windowX2 - anchorX;
		offsetH = windowY2 - anchorY;

		skipMouseDragged = false;

		// On Cinnamon the width and height are the original window width and height, not the maximized width and height
		// On Windows the width and height are the maximized window width and height, not the original width and height
		log.atConfig().log( "x={0} y={1} w={2} h={3}", originalX, originalY, originalW, originalH );
	}

	private void handleDragged( MouseEvent event ) {
		if( skipMouseDragged ) return;

		double windowX = event.getScreenX() - offsetX;
		double windowY = event.getScreenY() - offsetY;
		double windowW = originalW + (event.getScreenX() - anchorX);
		double windowH = originalH + (event.getScreenY() - anchorY);

		Window stage = getWindow( event );
		dragHandler.handleDrag( this, event, stage, windowX, windowY, windowW, windowH, windowX2, windowY2 );
	}

	public interface DragHandler {

		void handleDrag( StageClickAndDrag handler, MouseEvent event, Window window, double windowX, double windowY, double windowW, double windowH, double anchorW, double anchorH );

	}

}
