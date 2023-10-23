package com.avereon.xenon.workspace;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

public class StageClickAndDrag implements EventHandler<MouseEvent> {

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

	private final DragHandler handler;

	StageClickAndDrag( Node node, DragHandler handler ) {
		this.handler = handler;
		node.addEventFilter( MouseEvent.MOUSE_PRESSED, this );
		node.addEventFilter( MouseEvent.MOUSE_DRAGGED, this );
	}

	public static Window getWindow( MouseEvent event ) {
		return ((Node)event.getSource()).getScene().getWindow();
	}

	@Override
	public void handle( MouseEvent event ) {
		Window stage = getWindow( event );

		if( event.getEventType() == MouseEvent.MOUSE_PRESSED ) {
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
		} else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED ) {
			if( skipMouseDragged ) return;
			double screenX = event.getScreenX() - offsetX;
			double screenY = event.getScreenY() - offsetY;

			double screenW = originalW + (event.getScreenX() - anchorW) + offsetW;
			double screenH = originalH + (event.getScreenY() - anchorH) + offsetH;
			handler.handle( event, stage, screenX, screenY, screenW, screenH, anchorW, anchorH );
		}
	}

	public interface DragHandler {

		void handle( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH );

	}

}
