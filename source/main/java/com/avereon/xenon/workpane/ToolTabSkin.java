package com.avereon.xenon.workpane;

import com.avereon.util.Log;
import com.avereon.venza.javafx.FxUtil;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
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
			// WORKAROUND Copy transfer mode not working correctly on Linux and MacOS
			Dragboard board = tab.startDragAndDrop( TransferMode.MOVE );

			ClipboardContent content = new ClipboardContent();
			content.putUrl( tool.getAsset().getUri().toString() );
			content.putString( tool.getAsset().getUri().toString() );
			board.setContent( content );

			// WORKAROUND Setting a drag view causes DnD to break
			//Image image = tab.snapshot( null, null );
			//board.setDragView( image, 0.5 * image.getWidth(), 0.5 * image.getHeight() );

			log.log( Log.DEBUG,  "Drag start: " + tool.getAsset().getUri() );
		} );

		tab.setOnDragDone( ( event ) -> {
			log.log( Log.DEBUG,  "Drag done: " + tool.getAsset().getUri() );
			//if( !event.isDropCompleted() ) getSkinnable().getToolPane().getWorkpane().setDropHint( null );
		} );

		tab.setOnDragEntered( ( event ) -> {
			log.log( Log.DEBUG,  "Drag enter tab: " + event.getDragboard().getUrl() );
			Bounds bounds = FxUtil.localToParent( tab, getSkinnable().getToolTabPane().getWorkpane() );
			getSkinnable().getToolTabPane().getWorkpane().setDropHint( new WorkpaneDropHint( bounds ) );
		} );

		tab.setOnDragOver( ( event ) -> {
			event.acceptTransferModes( TransferMode.MOVE, TransferMode.COPY );
		} );

		tab.setOnDragExited( ( event ) -> {
			log.log( Log.DEBUG,  "Drag exit tab: " + event.getDragboard().getUrl() );
			getSkinnable().getToolTabPane().getWorkpane().setDropHint( null );
		} );

		tab.setOnDragDropped( ( event ) -> {
			log.log( Log.DEBUG,  "Drag dropped on tab: " + event.getDragboard().getUrl() + ": " + event.getAcceptedTransferMode() );
			int index = tab.getToolTabPane().getTabs().indexOf( tab );
			tab.getToolTabPane().handleDrop( event, index, null );
		} );

		close.setOnMouseClicked( ( event ) -> tab.getOnCloseRequest().handle( event ) );
		tab.setOnCloseRequest( event -> tool.close() );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

}
