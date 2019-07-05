package com.xeomar.xenon.workarea;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.util.FxUtil;
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
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ToolTabSkin extends SkinBase<ToolTab> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

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

		//boolean selected = tab.getToolPane().getSelectionModel().getSelectedItem() == tab;
		pseudoClassStateChanged( ToolTab.SELECTED_PSEUDOCLASS_STATE, tab.isSelected() );

		label.setOnMousePressed( ( event ) -> {
			select( tab );
			if( event.getClickCount() == 2 ) {
				WorkpaneView view = tool.getToolView();
				view.getWorkpane().setMaximizedView( view.isMaximized() ? null : view );
			}
		} );

		tab.setOnDragDetected( ( event ) -> {
			Dragboard board = tab.startDragAndDrop( TransferMode.MOVE, TransferMode.COPY );

			ClipboardContent content = new ClipboardContent();
			content.putUrl( tool.getResource().getUri().toString() );
			content.putString( tool.getResource().getUri().toString() );
			board.setContent( content );

			// WORKAROUND Setting a drag view causes DnD to break
			//Image image = tab.snapshot( null, null );
			//board.setDragView( image, 0.5 * image.getWidth(), 0.5 * image.getHeight() );

			log.warn( "Drag start: " + tool.getResource().getUri() );
		} );

		tab.setOnDragDone( ( event ) -> {
			log.warn( "Drag done: " + tool.getResource().getUri() );
			//if( !event.isDropCompleted() ) getSkinnable().getToolPane().getWorkpane().setDropHint( null );
		} );

		tab.setOnDragEntered( ( event ) -> {
			log.warn( "Drag enter tab: " + event.getDragboard().getUrl() );
			Bounds bounds = FxUtil.localToParent( tab, getSkinnable().getToolPane().getWorkpane() );
			getSkinnable().getToolPane().getWorkpane().setDropHint( new WorkpaneDropHint( bounds ) );
			event.consume();
		} );

		tab.setOnDragOver( ( event ) -> {
			//log.warn( "Drag over tab: " + event.getDragboard().getUrl() );
			event.acceptTransferModes( TransferMode.MOVE, TransferMode.COPY );
			event.consume();
		} );

		tab.setOnDragExited( ( event ) -> {
			log.warn( "Drag exit tab: " + event.getDragboard().getUrl() );
			getSkinnable().getToolPane().getWorkpane().setDropHint( null );
			event.consume();
		} );

		tab.setOnDragDropped( ( event ) -> {
			log.warn( "Drag dropped on tab: " + event.getDragboard().getUrl() + ": " + event.getAcceptedTransferMode() );
			int index = tab.getToolPane().getTabs().indexOf( tab );
			tab.getToolPane().handleDrop( event, index, null );
		} );

		close.setOnMouseClicked( ( event ) -> tab.getOnCloseRequest().handle( event ) );
		tab.setOnCloseRequest( event -> {
			event.consume();
			tool.close();
		} );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

	private void select( ToolTab tab ) {
		tab.getToolPane().getSelectionModel().select( tab );
		tab.getToolPane().requestFocus();
	}

}
