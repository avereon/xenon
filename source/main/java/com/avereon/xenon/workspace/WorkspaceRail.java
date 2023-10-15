package com.avereon.xenon.workspace;

import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class WorkspaceRail extends BorderPane {

	public WorkspaceRail( Stage stage, Side side ) {
		Pane tl = new Pane();
		Pane c = new Pane();
		Pane br = new Pane();

		setCenter( c );

		if( side.isHorizontal() ) {
			setLeft( tl );
			setRight( br );

			// tl to resize left side
			// br to resize right side
		} else {
			setTop( tl );
			setBottom( br );
		}

		switch( side ) {
			case TOP: {
				tl.getStyleClass().addAll( "workspace-rail-nw" );
				c.getStyleClass().addAll( "workspace-rail-n" );
				br.getStyleClass().addAll( "workspace-rail-ne" );
				break;
			}
			case RIGHT: {
				tl.getStyleClass().addAll( "workspace-rail-en" );
				c.getStyleClass().addAll( "workspace-rail-e" );
				br.getStyleClass().addAll( "workspace-rail-es" );
				break;
			}
			case BOTTOM: {
				tl.getStyleClass().addAll( "workspace-rail-sw" );
				c.getStyleClass().addAll( "workspace-rail-s" );
				br.getStyleClass().addAll( "workspace-rail-se" );
				break;
			}
			case LEFT: {
				tl.getStyleClass().addAll( "workspace-rail-wn" );
				c.getStyleClass().addAll( "workspace-rail-w" );
				br.getStyleClass().addAll( "workspace-rail-ws" );
				break;
			}
		}
	}

}
