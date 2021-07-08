package com.avereon.xenon.workpane;

import com.avereon.zerra.javafx.FxUtil;
import javafx.collections.ListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import lombok.CustomLog;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public class ToolTabPaneSkin extends SkinBase<ToolTabPane> {

	private final Pane header;

	private final HBox tabContainer;

	private final Pane headerDrop;

	private final Pane toolArea;

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

		tabContainer = new HBox();
		tabContainer.getStyleClass().add( "box" );
		tabContainer.getChildren().addAll( control.getTabs() );

		// Create a separate pane to capture drop target events in the header space
		headerDrop = new Pane();
		headerDrop.getStyleClass().addAll( "tool-tab-drop" );

		// Create components
		header = new BorderPane( headerDrop, null, null, null, tabContainer );
		header.getStyleClass().addAll( "tool-pane-header-area" );

		toolArea = new ToolContentArea();
		toolArea.getChildren().addAll( tools );
		toolArea.getStyleClass().addAll( "tool-pane-content-area" );

		getChildren().setAll( header, toolArea );

		pseudoClassStateChanged( ToolTabPane.ACTIVE_PSEUDOCLASS_STATE, control.isActive() );

		control.getTabs().addListener( this::doUpdateTabs );
		control.getSelectionModel().selectedItemProperty().addListener( ( p, o, n ) -> doUpdateTabSelection() );

		headerDrop.setOnMouseClicked( this::doHeaderDropMouseClicked );
		headerDrop.setOnDragEntered( this::doDragOver );
		headerDrop.setOnDragOver( this::doDragOver );
		headerDrop.setOnDragExited( this::doDragExited );
		headerDrop.setOnDragDropped( this::doDragDropped );

		toolArea.setOnDragEntered( this::doDragOver );
		toolArea.setOnDragOver( this::doDragOver );
		toolArea.setOnDragExited( this::doDragExited );
		toolArea.setOnDragDropped( this::doDragDropped );

		ToolTab selectedTab = control.getSelectionModel().getSelectedItem();
		if( selectedTab != null ) selectedTab.getTool().setVisible( true );
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

	private void doUpdateTabs( ListChangeListener.Change<? extends ToolTab> change ) {
		 while( change.next() ) {
			change.getRemoved().stream().filter( Objects::nonNull ).forEach( tab -> {
				if( !getSkinnable().getTabs().contains( tab ) ) tab.setToolPane( null );
				toolArea.getChildren().remove( tab.getTool() );
				tabContainer.getChildren().remove( tab );
			} );

			change.getAddedSubList().stream().filter( Objects::nonNull ).forEach( tab -> {
				tab.setToolPane( getSkinnable() );
				toolArea.getChildren().add( tab.getTool() );
				tabContainer.getChildren().add( tab );
			} );
		}

		getSkinnable().requestLayout();
	}

	private void doUpdateTabSelection() {
		// Hide all the tools
		getSkinnable().getTabs().stream().map( ToolTab::getTool ).forEach( tool -> tool.setVisible( false ) );

		// Show the selected tool
		ToolTab selectedTab = getSkinnable().getSelectionModel().getSelectedItem();
		if( selectedTab != null ) selectedTab.getTool().setVisible( true );

		getSkinnable().requestLayout();
	}

	private void doHeaderDropMouseClicked( javafx.scene.input.MouseEvent event ) {
		if( event.getClickCount() == 2 ) {
			WorkpaneView view = getSkinnable().getWorkpaneView();
			view.getWorkpane().setMaximizedView( view.isMaximized() ? null : view );
		}
	}

	private void doDragOver( DragEvent event ) {
		if( !ToolTabSkin.canHandleDrop( event ) ) return;

		WorkpaneDropHint hint = null;
		if( event.getSource() == headerDrop ) {
			hint = new WorkpaneDropHint( FxUtil.localToParent( headerDrop, getSkinnable().getWorkpane() ) );
		} else if( event.getSource() == toolArea ) {
			Bounds dropBounds = getDropBounds( toolArea.getLayoutBounds(), getDropSide( event ) );
			hint = new WorkpaneDropHint( FxUtil.localToParent( toolArea, getSkinnable().getWorkpane(), dropBounds ) );
		}
		getSkinnable().getWorkpane().setDropHint( hint );
		FxUtil.setTransferMode( event );
	}

	private void doDragExited( DragEvent event ) {
		if( !ToolTabSkin.canHandleDrop( event ) ) return;
		if( getSkinnable().getWorkpane() != null ) getSkinnable().getWorkpane().setDropHint( null );
	}

	private void doDragDropped( DragEvent event ) {
		if( !ToolTabSkin.canHandleDrop( event ) ) return;
		DropEvent.Area area = null;
		if( event.getSource() == headerDrop ) {
			area = DropEvent.Area.HEADER;
		} else if( event.getSource() == toolArea ) {
			area = DropEvent.Area.TOOL_AREA;
		}
		getSkinnable().handleDrop( event, area, 0, getDropSide( event ) );
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
