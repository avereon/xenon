package com.avereon.xenon.workpane;

import com.avereon.util.LogUtil;
import com.avereon.venza.javafx.FxUtil;
import javafx.collections.ListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ToolTabPaneSkin extends SkinBase<ToolTabPane> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Pane header;

	private Pane toolArea;

	public static final double SIDE_PERCENT = 0.3;

	public static final double MINIMUM_PIXELS = 50;

	protected ToolTabPaneSkin( ToolTabPane control ) {
		super( control );

		Rectangle clipRect = new Rectangle( control.getWidth(), control.getHeight() );
		registerChangeListener( control.widthProperty(), event -> clipRect.setWidth( control.getWidth() ) );
		registerChangeListener( control.heightProperty(), event -> clipRect.setHeight( control.getHeight() ) );
		control.setClip( clipRect );

		control.getTabs().forEach( tab -> tab.setToolPane( control ) );
		Set<Tool> tools = control.getTabs().stream().map( ToolTab::getTool ).peek( tool -> tool.setVisible( false ) ).collect( Collectors.toSet() );

		HBox tabContainer = new HBox();
		tabContainer.getStyleClass().add( "box" );
		tabContainer.getChildren().addAll( control.getTabs() );

		// Create a separate pane to capture drop target events in the header space
		Pane headerDrop = new Pane();
		headerDrop.getStyleClass().addAll( "tool-tab-drop" );

		// Create components
		header = new BorderPane( headerDrop, null, null, null, tabContainer );
		header.getStyleClass().addAll( "tool-pane-header-area" );

		toolArea = new ToolContentArea();
		toolArea.getChildren().addAll( tools );
		toolArea.getStyleClass().addAll( "tool-pane-content-area" );

		getChildren().setAll( header, toolArea );

		pseudoClassStateChanged( ToolTabPane.ACTIVE_PSEUDOCLASS_STATE, control.isActive() );

		control.getTabs().addListener( (ListChangeListener<ToolTab>)change -> {
			while( change.next() ) {
				change.getRemoved().stream().filter( Objects::nonNull ).forEach( tab -> {
					if( !control.getTabs().contains( tab ) ) tab.setToolPane( null );
					toolArea.getChildren().remove( tab.getTool() );
					// Tab is removed below
				} );

				change.getAddedSubList().stream().filter( Objects::nonNull ).forEach( tab -> {
					tab.setToolPane( control );
					// Tab is added below
					toolArea.getChildren().add( tab.getTool() );
				} );

				if( change.wasRemoved() ) tabContainer.getChildren().removeAll( change.getRemoved() );
				if( change.wasAdded() ) tabContainer.getChildren().addAll( change.getFrom(), change.getAddedSubList() );
			}

			control.requestLayout();
		} );

		control.getSelectionModel().selectedItemProperty().addListener( ( observable, oldValue, newValue ) -> {
			// Hide all the tools
			control.getTabs().stream().map( ToolTab::getTool ).forEach( tool -> tool.setVisible( false ) );

			// Show the selected tool
			ToolTab selectedTab = control.getSelectionModel().getSelectedItem();
			if( selectedTab != null ) selectedTab.getTool().setVisible( true );

			control.requestLayout();
		} );

		headerDrop.setOnMouseClicked( ( event ) -> {
			if( event.getClickCount() == 2 ) {
				WorkpaneView view = control.getWorkpaneView();
				view.getWorkpane().setMaximizedView( view.isMaximized() ? null : view );
			}
		} );

		headerDrop.setOnDragEntered( ( event ) -> {
			Bounds bounds = FxUtil.localToParent( headerDrop, control.getWorkpane() );
			control.getWorkpane().setDropHint( new WorkpaneDropHint( bounds ) );
		} );

		headerDrop.setOnDragOver( ( event ) -> {
			event.acceptTransferModes( TransferMode.MOVE, TransferMode.COPY );
		} );

		headerDrop.setOnDragExited( ( event ) -> {
			control.getWorkpane().setDropHint( null );
		} );

		headerDrop.setOnDragDropped( ( event ) -> {
			control.handleDrop( event, -1, null );
		} );

		toolArea.setOnDragEntered( ( event ) -> {
		} );

		toolArea.setOnDragOver( ( event ) -> {
			event.acceptTransferModes( TransferMode.COPY_OR_MOVE );

			Bounds dropBounds = getDropBounds( toolArea.getLayoutBounds(), getDropSide( event ) );
			Bounds dropHintBounds = FxUtil.localToParent( toolArea, control.getWorkpane(), dropBounds );
			control.getWorkpane().setDropHint( new WorkpaneDropHint( dropHintBounds ) );
		} );

		toolArea.setOnDragExited( ( event ) -> {
			control.getWorkpane().setDropHint( null );
		} );

		toolArea.setOnDragDropped( ( event ) -> {
			control.handleDrop( event, -2, getDropSide( event ) );
		} );

		ToolTab selectedTab = control.getSelectionModel().getSelectedItem();
		if( selectedTab != null ) selectedTab.getTool().setVisible( true );
	}

	private double getDropHintWidth( Bounds bounds ) {
		return Math.min( MINIMUM_PIXELS, SIDE_PERCENT * bounds.getWidth() );
	}

	private double getDropHintHeight( Bounds bounds ) {
		return Math.min( MINIMUM_PIXELS, SIDE_PERCENT * bounds.getHeight() );
	}

	private Side getDropSide( DragEvent event ) {
		Side position = null;

		Bounds bounds = ((Node)event.getSource()).getLayoutBounds();

		double dropWidth = getDropHintWidth( bounds );
		double dropHeight = getDropHintHeight( bounds );

		double northDistance = event.getY() - bounds.getMinY();
		double southDistance = bounds.getMinY() + bounds.getHeight() - event.getY();
		double eastDistance = bounds.getMinX() + bounds.getWidth() - event.getX();
		double westDistance = event.getX() - bounds.getMinX();

		// The following checks should be in this order: south, north, east, west
		if( southDistance > 0 && southDistance < dropHeight ) position = Side.BOTTOM;
		if( northDistance > 0 && northDistance < dropHeight ) position = Side.TOP;
		if( eastDistance > 0 && eastDistance < dropWidth ) position = Side.RIGHT;
		if( westDistance > 0 && westDistance < dropWidth ) position = Side.LEFT;

		return position;
	}

	private Bounds getDropBounds( Bounds bounds, Side side ) {
		if( side == null ) return bounds;

		double dropWidth = getDropHintWidth( bounds );
		double dropHeight = getDropHintHeight( bounds );

		switch( side ) {
			case LEFT: {
				return new BoundingBox( 0, 0, dropWidth, bounds.getHeight() );
			}
			case RIGHT: {
				return new BoundingBox( bounds.getWidth() - dropWidth, 0, dropWidth, bounds.getHeight() );
			}
			case TOP: {
				return new BoundingBox( 0, 0, bounds.getWidth(), dropHeight );
			}
			case BOTTOM: {
				return new BoundingBox( 0, bounds.getHeight() - dropHeight, bounds.getWidth(), dropHeight );
			}
		}

		return bounds;
	}

	@Override
	protected double computeMinWidth( double height, double topInset, double rightInset, double bottomInset, double leftInset ) {
		return 0;
	}

	@Override
	protected double computeMinHeight( double width, double topInset, double rightInset, double bottomInset, double leftInset ) {
		return 0;
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		double headerSize = snapSizeY( header.prefHeight( -1 ) );
		header.resizeRelocate( contentX, contentY, contentWidth, headerSize );

		double toolHeight = contentHeight - headerSize;
		toolArea.resizeRelocate( contentX, headerSize, contentWidth, toolHeight );
	}

	private class ToolContentArea extends Pane {

		ToolContentArea() {
			Rectangle clipRect = new Rectangle( this.getWidth(), getHeight() );
			registerChangeListener( widthProperty(), event -> clipRect.setWidth( getSkinnable().getWidth() ) );
			registerChangeListener( heightProperty(), event -> clipRect.setHeight( getSkinnable().getHeight() ) );
			setClip( clipRect );
		}

		@Override
		protected void layoutChildren() {
			getChildren().forEach( child -> child.resize( getWidth(), getHeight() ) );
		}

	}

}
