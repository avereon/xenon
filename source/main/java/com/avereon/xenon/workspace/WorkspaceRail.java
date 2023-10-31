package com.avereon.xenon.workspace;

import javafx.geometry.Side;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.CustomLog;

@CustomLog
public class WorkspaceRail extends BorderPane {

	public WorkspaceRail( Stage stage, Side side ) {
		Pane tl = new Pane();
		Pane c = new Pane();
		Pane br = new Pane();

		setCenter( c );

		if( side.isHorizontal() ) {
			setLeft( tl );
			setRight( br );
		} else {
			setTop( tl );
			setBottom( br );
		}

		switch( side ) {
			case TOP: {
				tl.getStyleClass().addAll( "workspace-rail-nw" );
				c.getStyleClass().addAll( "workspace-rail-n" );
				br.getStyleClass().addAll( "workspace-rail-ne" );
				new StageClickAndDrag( tl, this::handleTlResize );
				new StageClickAndDrag( c, this::handleTopResize );
				new StageClickAndDrag( br, this::handleTrResize );
				break;
			}
			case RIGHT: {
				tl.getStyleClass().addAll( "workspace-rail-en" );
				c.getStyleClass().addAll( "workspace-rail-e" );
				br.getStyleClass().addAll( "workspace-rail-es" );
				new StageClickAndDrag( tl, this::handleTrResize );
				new StageClickAndDrag( c, this::handleRightResize );
				new StageClickAndDrag( br, this::handleBrResize );
				break;
			}
			case BOTTOM: {
				tl.getStyleClass().addAll( "workspace-rail-sw" );
				c.getStyleClass().addAll( "workspace-rail-s" );
				br.getStyleClass().addAll( "workspace-rail-se" );
				new StageClickAndDrag( tl, this::handleBlResize );
				new StageClickAndDrag( c, this::handleBottomResize );
				new StageClickAndDrag( br, this::handleBrResize );
				break;
			}
			case LEFT: {
				tl.getStyleClass().addAll( "workspace-rail-wn" );
				c.getStyleClass().addAll( "workspace-rail-w" );
				br.getStyleClass().addAll( "workspace-rail-ws" );
				new StageClickAndDrag( tl, this::handleTlResize );
				new StageClickAndDrag( c, this::handleLeftResize );
				new StageClickAndDrag( br, this::handleBlResize );
				break;
			}
		}
	}

	private void handleTopResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setY( screenY );
		window.setHeight( anchorH - screenY );
	}

	private void handleRightResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setWidth( screenW );
	}

	private void handleBottomResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setHeight( screenH );
	}

	private void handleLeftResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setX( screenX );
		window.setWidth( anchorW - screenX );
	}

	private void handleTlResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setX( screenX );
		window.setY( screenY );
		window.setWidth( anchorW - screenX );
		window.setHeight( anchorH - screenY );
	}

	private void handleTrResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setY( screenY );
		window.setWidth( screenW );
		window.setHeight( anchorH - screenY );
	}

	private void handleBlResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		window.setX( screenX );
		//window.setY( screenY );
		window.setWidth( anchorW - screenX );
		window.setHeight( screenH );
	}

	private void handleBrResize( MouseEvent event, Window window, double screenX, double screenY, double screenW, double screenH, double anchorW, double anchorH ) {
		//window.setX( screenX );
		//window.setY( screenY );
		window.setWidth( screenW );
		window.setHeight( screenH );
	}

}