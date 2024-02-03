package com.avereon.xenon.workspace;

import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lombok.CustomLog;

@CustomLog
public class WorkspaceRail extends BorderPane {

	public WorkspaceRail( Side side ) {
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
				new StageDragContext( tl, this::onTlResize );
				new StageDragContext( c, this::onTopResize );
				new StageDragContext( br, this::onTrResize );
				break;
			}
			case RIGHT: {
				tl.getStyleClass().addAll( "workspace-rail-en" );
				c.getStyleClass().addAll( "workspace-rail-e" );
				br.getStyleClass().addAll( "workspace-rail-es" );
				new StageDragContext( tl, this::onTrResize );
				new StageDragContext( c, this::onRightResize );
				new StageDragContext( br, this::onBrResize );
				break;
			}
			case BOTTOM: {
				tl.getStyleClass().addAll( "workspace-rail-sw" );
				c.getStyleClass().addAll( "workspace-rail-s" );
				br.getStyleClass().addAll( "workspace-rail-se" );
				new StageDragContext( tl, this::onBlResize );
				new StageDragContext( c, this::onBottomResize );
				new StageDragContext( br, this::onBrResize );
				break;
			}
			case LEFT: {
				tl.getStyleClass().addAll( "workspace-rail-wn" );
				c.getStyleClass().addAll( "workspace-rail-w" );
				br.getStyleClass().addAll( "workspace-rail-ws" );
				new StageDragContext( tl, this::onTlResize );
				new StageDragContext( c, this::onLeftResize );
				new StageDragContext( br, this::onBlResize );
				break;
			}
		}
	}

	private void onTopResize( StageDragContext.DragData data ) {
		data.window().setY( data.windowY() );
		data.window().setHeight( data.anchorH() - data.windowY() );
	}

	private void onRightResize( StageDragContext.DragData data ) {
		data.window().setWidth( data.windowW() );
	}

	private void onBottomResize( StageDragContext.DragData data ) {
		data.window().setHeight( data.windowH() );
	}

	private void onLeftResize( StageDragContext.DragData data ) {
		data.window().setX( data.windowX() );
		data.window().setWidth( data.anchorW() - data.windowX() );
	}

	private void onTlResize( StageDragContext.DragData data ) {
		data.window().setX( data.windowX() );
		data.window().setY( data.windowY() );
		data.window().setWidth( data.anchorW() - data.windowX() );
		data.window().setHeight( data.anchorH() - data.windowY() );
	}

	private void onTrResize( StageDragContext.DragData data ) {
		data.window().setY( data.windowY() );
		data.window().setWidth( data.windowW() );
		data.window().setHeight( data.anchorH() - data.windowY() );
	}

	private void onBlResize( StageDragContext.DragData data ) {
		data.window().setX( data.windowX() );
		data.window().setWidth( data.anchorW() - data.windowX() );
		data.window().setHeight( data.windowH() );
	}

	private void onBrResize( StageDragContext.DragData data ) {
		data.window().setWidth( data.windowW() );
		data.window().setHeight( data.windowH() );
	}

}
