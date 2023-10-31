package com.avereon.xenon.workspace;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

public class StageClickAndDrag {

	private double originalX;

	private double originalY;

	private double originalW;

	private double originalH;

	private double anchorX;

	private double anchorY;

	private double anchorW;

	private double anchorH;

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
		// Need to recalculate dependent values
		offsetX = anchorX - originalX;
		offsetW = anchorW - anchorX;
	}

	private void handlePressed( MouseEvent event ) {
		Window stage = getWindow( event );
		originalX = stage.getX();
		originalY = stage.getY();
		originalW = stage.getWidth();
		originalH = stage.getHeight();
		anchorX = event.getScreenX();
		anchorY = event.getScreenY();
		anchorW = originalX + originalW;
		anchorH = originalY + originalH;
		offsetX = anchorX - originalX;
		offsetY = anchorY - stage.getY();
		offsetW = anchorW - anchorX;
		offsetH = anchorH - anchorY;
		skipMouseDragged = false;
	}

	private void handleDragged( MouseEvent event ) {
		if( skipMouseDragged ) return;

		Window stage = getWindow( event );

		double screenX = event.getScreenX() - offsetX;
		double screenY = event.getScreenY() - offsetY;
		double screenW = originalW + (event.getScreenX() - anchorW) + offsetW;
		double screenH = originalH + (event.getScreenY() - anchorH) + offsetH;

		dragHandler.handleDrag( this, event, stage, screenX, screenY, screenW, screenH, anchorW, anchorH );
	}

	public interface DragHandler {

		void handleDrag( StageClickAndDrag handler, MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH );

	}

}
