package com.avereon.xenon.workpane;

import com.avereon.util.Log;
import com.avereon.zerra.javafx.FxUtil;
import javafx.beans.binding.Bindings;
import javafx.geometry.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;

import java.lang.System.Logger;

public class ToolTabSkin extends SkinBase<ToolTab> {

	private static final Logger log = Log.get();

	private final BorderPane tabLayout;

	ToolTabSkin( ToolTab tab ) {
		super( tab );

		Tool tool = tab.getTool();

		Label icon = new Label();
		icon.getStyleClass().setAll( "tool-tab-icon" );
		icon.graphicProperty().bind( tool.graphicProperty() );

		Label title = new TabLabel();
		title.getStyleClass().setAll( "tool-tab-label" );
		title.textProperty().bind( tool.titleProperty() );

		Label context = new Label();
		context.getStyleClass().setAll( "tool-tab-context-icon" );
		context.graphicProperty().bind( tool.contextGraphicProperty() );
		context.visibleProperty().bind( Bindings.isNotNull( tool.contextGraphicProperty() ) );

		Button close = new Button();
		close.getStyleClass().setAll( "tool-tab-close" );
		close.graphicProperty().bind( tool.closeGraphicProperty() );

		BorderPane content = new BorderPane( title, null, context, null, icon );
		getChildren().setAll( tabLayout = new BorderPane( content, null, close, null, null ) );

		pseudoClassStateChanged( ToolTab.SELECTED_PSEUDOCLASS_STATE, tab.isSelected() );

		content.setOnMousePressed( ( event ) -> doSetActiveTool( event, tab, tool ) );
		tab.setOnDragDetected( ( event ) -> doStartDrag( event, tab, tool ) );
		tab.setOnDragEntered( ( event ) -> doDragEntered( event, tab, tool ) );
		tab.setOnDragOver( tab.getOnDragEntered() );
		tab.setOnDragExited( ( event ) -> doDragExited( event, tool ) );
		tab.setOnDragDropped( ( event ) -> doDragDropped( event, tab ) );
		tab.setOnDragDone( ( event ) -> doDragDone( event, tool ) );
		tab.setOnCloseRequest( event -> tool.close() );
		close.setOnMouseClicked( ( event ) -> tab.getOnCloseRequest().handle( event ) );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

	private void doDragDone( javafx.scene.input.DragEvent event, Tool tool ) {
		log.log( Log.DEBUG, "Drag done: " + tool.getAsset().getUri() );
	}

	private void doDragDropped( DragEvent event, ToolTab tab ) {
		log.log( Log.DEBUG, "Drag dropped on tab: " + event.getDragboard().getUrl() + ": " + event.getAcceptedTransferMode() );
		int index = tab.getToolTabPane().getTabs().indexOf( tab );
		tab.getToolTabPane().handleDrop( event, DropEvent.Area.TAB, index, null );
	}

	private void doDragExited( DragEvent event, Tool tool ) {
		log.log( Log.DEBUG, "Drag exit tab: " + event.getDragboard().getUrl() );
		if( tool.getWorkpane() != null ) tool.getWorkpane().setDropHint( null );
	}

	private void doDragEntered( DragEvent event, ToolTab tab, Tool tool ) {
		log.log( Log.DEBUG, "Drag enter tab: " + event.getDragboard().getUrl() );
		Bounds bounds = FxUtil.localToParent( tab, tool.getWorkpane() );
		tool.getWorkpane().setDropHint( new WorkpaneDropHint( bounds ) );
		FxUtil.setTransferMode( event );
	}

	private void doStartDrag( javafx.scene.input.MouseEvent event, ToolTab tab, Tool tool ) {
		log.log( Log.DEBUG, "Drag start: " + tool.getAsset().getUri() );

		TransferMode[] modes = tab.getToolTabPane().getWorkpane().getOnToolDrop().getSupportedModes( tool );
		Dragboard board = tab.startDragAndDrop( modes );

		ClipboardContent content = new ClipboardContent();
		content.putUrl( tool.getAsset().getUri().toString() );
		content.putString( tool.getAsset().getUri().toString() );
		board.setContent( content );

		Image image = tab.snapshot( null, null );
		board.setDragView( image, 0.5 * image.getWidth(), 0.5 * image.getHeight() );
	}

	private void doSetActiveTool( MouseEvent event, ToolTab tab, Tool tool ) {
		if( isShowContextMenu( tool ) ) tool.getContextMenu().show( tab, Side.BOTTOM, 0, 0 );
		tab.getToolTabPane().getWorkpane().setActiveTool( tool );
		if( event.getClickCount() == 2 ) doSetMaximizedView( tool );
	}

	private boolean isShowContextMenu( Tool tool ) {
		return tool.isActive() && !tool.getContextMenu().getItems().isEmpty();
	}

	private void doSetMaximizedView( Tool tool ) {
		WorkpaneView view = tool.getToolView();
		view.getWorkpane().setMaximizedView( view.isMaximized() ? null : view );
	}

	private static class TabLabel extends Label {

		// NOTE Just binding the preferred sizes to the min sizes does not work

		@Override
		protected double computeMinWidth( double height ) {
			return computePrefWidth( height );
		}

		@Override
		protected double computeMinHeight( double width ) {
			return computePrefHeight( width );
		}

	}

}
