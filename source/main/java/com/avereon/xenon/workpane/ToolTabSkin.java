package com.avereon.xenon.workpane;

import com.avereon.log.LazyEval;
import com.avereon.zarra.javafx.FxUtil;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import lombok.CustomLog;

@CustomLog
public class ToolTabSkin extends SkinBase<ToolTab> {

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

		content.setOnMousePressed( this::doSetActiveTool );
		tab.setOnDragDetected( this::doStartDrag );
		tab.setOnDragEntered( this::doDragOver );
		tab.setOnDragOver( this::doDragOver );
		tab.setOnDragExited( this::doDragExited );
		tab.setOnDragDropped( this::doDragDropped );
		tab.setOnDragDone( this::doDragDone );
		tab.setOnCloseRequest( event -> tool.close() );
		close.setOnMouseClicked( ( event ) -> tab.getOnCloseRequest().handle( event ) );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		// This is a slight optimization over the default implementation
		layoutInArea( tabLayout, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER );
	}

	static boolean canHandleDrop( DragEvent event ) {
		if( event.getDragboard().hasUrl() ) return true;
		if( event.getDragboard().hasFiles() ) return true;
		return event.getGestureSource() instanceof ToolTab;
	}

	private void doSetActiveTool( MouseEvent event ) {
		ToolTab tab = getSkinnable();
		Tool tool = tab.getTool();
		if( isShowContextMenu( tool ) ) tool.getContextMenu().show( tab, Side.BOTTOM, 0, 0 );
		tab.getToolTabPane().getWorkpane().setActiveTool( tool );
		if( event.getClickCount() == 2 ) doSetMaximizedView( tool );
	}

	private void doStartDrag( javafx.scene.input.MouseEvent event ) {
		ToolTab tab = getSkinnable();
		Tool tool = tab.getTool();

		log.atDebug().log( "Drag start: %s", LazyEval.of( () -> tool.getAsset().getUri() ) );

		TransferMode[] modes = tab.getToolTabPane().getWorkpane().getOnToolDrop().getSupportedModes( tool );
		Dragboard board = tab.startDragAndDrop( modes );

		ClipboardContent content = new ClipboardContent();
		content.putString( tool.getAsset().getUri().toString() );
		content.putUrl( tool.getAsset().getUri().toString() );
		board.setContent( content );

		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setTransform( tab.getLocalToSceneTransform() );
		Image image = tab.snapshot( parameters, null );
		board.setDragView( image, 0.5 * image.getWidth(), 0.5 * image.getHeight() );
	}

	private void doDragOver( DragEvent event ) {
		if( !canHandleDrop( event ) ) return;

		ToolTab tab = getSkinnable();
		Tool tool = tab.getTool();

		log.atDebug().log( "Drag over tab: %s", LazyEval.of( () -> event.getDragboard().getUrl() ) );
		Bounds bounds = FxUtil.localToParent( tab, tool.getWorkpane() );
		tool.getWorkpane().setDropHint( new WorkpaneDropHint( bounds ) );
		FxUtil.setTransferMode( event );
	}

	private void doDragExited( DragEvent event ) {
		if( !canHandleDrop( event ) ) return;

		ToolTab tab = getSkinnable();
		Tool tool = tab.getTool();

		log.atDebug().log( "Drag exit tab: %s", LazyEval.of( () -> event.getDragboard().getUrl() ) );
		if( tool.getWorkpane() != null ) tool.getWorkpane().setDropHint( null );
	}

	private void doDragDropped( DragEvent event ) {
		if( !canHandleDrop( event ) ) return;

		ToolTab tab = getSkinnable();

		log.atDebug().log( "Drag dropped on tab: %s: %s", LazyEval.of( () -> event.getDragboard().getUrl() ), LazyEval.of( event::getAcceptedTransferMode ) );
		int index = tab.getToolTabPane().getTabs().indexOf( tab );
		tab.getToolTabPane().handleDrop( event, DropEvent.Area.TAB, index, null );
	}

	private void doDragDone( javafx.scene.input.DragEvent event ) {
		if( !canHandleDrop( event ) ) return;

		ToolTab tab = getSkinnable();
		Tool tool = tab.getTool();
		log.atDebug().log( "Drag done: %s", LazyEval.of( () -> tool.getAsset().getUri() ) );
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
