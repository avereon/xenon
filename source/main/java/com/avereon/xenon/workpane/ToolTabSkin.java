package com.avereon.xenon.workpane;

import com.avereon.util.Log;
import com.avereon.zerra.javafx.FxUtil;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

import java.lang.System.Logger;

public class ToolTabSkin extends SkinBase<ToolTab> {

	private static final Logger log = Log.get();

	private BorderPane tabLayout;

	private Label label;

	private Button close;

	ToolTabSkin( ToolTab tab ) {
		super( tab );

		Tool tool = tab.getTool();

		label = new Label() {

			@Override
			protected double computeMinWidth( double height ) {
				return computePrefWidth( height );
			}

			@Override
			protected double computeMinHeight( double width ) {
				return computePrefHeight( width );
			}
		};
		label.getStyleClass().setAll( "tool-tab-label" );
		label.graphicProperty().bind( tool.graphicProperty() );
		label.textProperty().bind( tool.titleProperty() );

		close = new Button();
		close.graphicProperty().bind( tool.closeGraphicProperty() );
		close.getStyleClass().setAll( "tool-tab-close" );

		tabLayout = new BorderPane();
		tabLayout.setCenter( label );
		tabLayout.setRight( close );

		getChildren().setAll( tabLayout );

		pseudoClassStateChanged( ToolTab.SELECTED_PSEUDOCLASS_STATE, tab.isSelected() );

		label.setOnMousePressed( ( event ) -> {
			tab.getToolTabPane().getWorkpane().setActiveTool( tab.getTool() );
			if( event.getClickCount() == 2 ) {
				WorkpaneView view = tool.getToolView();
				view.getWorkpane().setMaximizedView( view.isMaximized() ? null : view );
			}
		} );

		tab.setOnDragDetected( ( event ) -> {
			log.log( Log.DEBUG,  "Drag start: " + tool.getAsset().getUri() );

			TransferMode[] modes = tab.getToolTabPane().getWorkpane().getOnToolDrop().getSupportedModes();
			Dragboard board = tab.startDragAndDrop( modes );

			ClipboardContent content = new ClipboardContent();
			content.putUrl( tool.getAsset().getUri().toString() );
			content.putString( tool.getAsset().getUri().toString() );
			board.setContent( content );

			Image image = tab.snapshot( null, null );
			board.setDragView( image, 0.5 * image.getWidth(), 0.5 * image.getHeight() );
		} );

		tab.setOnDragEntered( ( event ) -> {
			log.log( Log.DEBUG,  "Drag enter tab: " + event.getDragboard().getUrl() );
			Bounds bounds = FxUtil.localToParent( tab, tool.getWorkpane() );
			tool.getWorkpane().setDropHint( new WorkpaneDropHint( bounds ) );
			event.acceptTransferModes( TransferMode.MOVE, TransferMode.COPY );
		} );

		tab.setOnDragOver( tab.getOnDragEntered() );

		tab.setOnDragExited( ( event ) -> {
			log.log( Log.DEBUG,  "Drag exit tab: " + event.getDragboard().getUrl() );
			tool.getWorkpane().setDropHint( null );
		} );

		tab.setOnDragDropped( ( event ) -> {
			log.log( Log.DEBUG,  "Drag dropped on tab: " + event.getDragboard().getUrl() + ": " + event.getAcceptedTransferMode() );
			int index = tab.getToolTabPane().getTabs().indexOf( tab );
			tab.getToolTabPane().handleDrop( event, DropEvent.Area.TAB, index, null );
		} );

		tab.setOnDragDone( ( event ) -> {
			log.log( Log.DEBUG,  "Drag done: " + tool.getAsset().getUri() );
		} );

		tab.setOnCloseRequest( event -> tool.close() );
		close.setOnMouseClicked( ( event ) -> tab.getOnCloseRequest().handle( event ) );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

}
