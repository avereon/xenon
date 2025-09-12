package com.avereon.xenon.workpane;

import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.util.IdGenerator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import lombok.CustomLog;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class WorkpaneView extends BorderPane implements WritableIdentity {

	@Getter
	private final ToolTabPane toolTabPane;

	private ObjectProperty<WorkpaneEdge> topEdge;

	private ObjectProperty<WorkpaneEdge> leftEdge;

	private ObjectProperty<WorkpaneEdge> rightEdge;

	private ObjectProperty<WorkpaneEdge> bottomEdge;

	private ObjectProperty<Workpane.Placement> placement;

	private Workpane parent;

	@Getter
	private Tool activeTool;

	public WorkpaneView() {
		getStyleClass().add( "workpane-view" );

		setUid( IdGenerator.getId() );
		setCenter( toolTabPane = new ToolTabPane() );
		setSnapToPixel( true );

		//tools.setTabClosingPolicy( TabPane.TabClosingPolicy.ALL_TABS );
		//tools.setTabDragPolicy( TabPane.TabDragPolicy.REORDER );

		// Add a focus listener to the tabs, so when a tab is focused, the tool
		// is activated. This may happen even if the tab is not selected.
		//		tools.activeProperty().addListener( ( observable, oldValue, newValue ) -> {
		//			ToolTab tab = tools.getSelectionModel().getSelectedItem();
		//			if( newValue && tab != null ) activateTool( tab.getTool() );
		//		} );

		// Add a selection listener to the tabs, so when a tab is selected, the tool
		// is activated. This may happen even if the tab is not focused.
		toolTabPane.getSelectionModel().selectedItemProperty().addListener( ( _, _, newValue ) -> {
			if( toolTabPane.focusedProperty().getValue() && newValue != null ) activateTool( newValue.getTool() );
		} );

		// Add a listener to the tab list to store the order when the tabs change
		toolTabPane.getTabs().addListener( (ListChangeListener<? super ToolTab>)( _ ) -> toolsReordered() );
	}

	private void toolsReordered() {
		toolTabPane.getTabs().stream().map( ToolTab::getTool ).forEach( this::fireToolReordered );
	}

	private void fireToolReordered( Tool tool ) {
		tool.setOrder( getTools().indexOf( tool ) );
		tool.fireEvent( new ToolEvent( this, ToolEvent.REORDERED, getWorkpane(), tool ) );
	}

	@Override
	public String getUid() {
		return getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setUid( String id ) {
		getProperties().put( Identity.KEY, id );
	}

	public WorkpaneEdge getTopEdge() {
		return topEdge == null ? null : topEdgeProperty().get();
	}

	public void setTopEdge( WorkpaneEdge topEdge ) {
		((ObjectProperty<WorkpaneEdge>)topEdgeProperty()).set( topEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> topEdgeProperty() {
		if( topEdge == null ) topEdge = new SimpleObjectProperty<>();
		return topEdge;
	}

	public WorkpaneEdge getLeftEdge() {
		return leftEdge == null ? null : leftEdgeProperty().get();
	}

	public void setLeftEdge( WorkpaneEdge leftEdge ) {
		((ObjectProperty<WorkpaneEdge>)leftEdgeProperty()).set( leftEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> leftEdgeProperty() {
		if( leftEdge == null ) leftEdge = new SimpleObjectProperty<>();
		return leftEdge;
	}

	public WorkpaneEdge getRightEdge() {
		return rightEdge == null ? null : rightEdgeProperty().get();
	}

	public void setRightEdge( WorkpaneEdge rightEdge ) {
		((ObjectProperty<WorkpaneEdge>)rightEdgeProperty()).set( rightEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> rightEdgeProperty() {
		if( rightEdge == null ) rightEdge = new SimpleObjectProperty<>();
		return rightEdge;
	}

	public WorkpaneEdge getBottomEdge() {
		return bottomEdge == null ? null : bottomEdgeProperty().get();
	}

	public void setBottomEdge( WorkpaneEdge bottomEdge ) {
		((ObjectProperty<WorkpaneEdge>)bottomEdgeProperty()).set( bottomEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> bottomEdgeProperty() {
		if( bottomEdge == null ) bottomEdge = new SimpleObjectProperty<>();
		return bottomEdge;
	}

	/**
	 * Get an unmodifiable list of the tools in the view.
	 *
	 * @return A list of the tools in the view.
	 */
	public List<Tool> getTools() {
		List<ToolTab> tabs = new ArrayList<>( toolTabPane.getTabs() );
		return tabs.parallelStream().map( b -> (Tool)b.getContent() ).collect( Collectors.toList() );
	}

	@SuppressWarnings( "UnusedReturnValue" )
	Tool addTool( Tool tool, int index ) {
		if( tool.getToolView() != null ) tool.getToolView().removeTool( tool );
		toolTabPane.getTabs().add( index, new ToolTab( tool ) );
		tool.setToolView( this );
		tool.callAllocate();

		if( toolTabPane.getTabs().size() == 1 ) setActiveTool( tool );

		return tool;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	Tool removeTool( Tool tool ) {
		boolean isActiveTool = tool == activeTool;

		Tool next = null;
		if( isActiveTool ) {
			// If the tool was the active tool, set the active tool to null
			if( parent != null ) parent.setActiveTool( null );

			next = determineNextTool( tool );
		}

		// If the tool is currently displayed, call conceal.
		if( tool.isDisplayed() ) tool.callConceal();
		tool.callDeallocate();

		// Remove the tool.
		toolTabPane.getTabs().remove( tool.getOrder() );
		tool.setToolView( null );
		if( activeTool == tool ) activeTool = null;

		// Set the active tool.
		if( isActiveTool && parent != null ) parent.setActiveTool( next );

		return tool;
	}

	public void setActiveTool( Tool tool ) {
		if( tool == activeTool ) return;

		if( activeTool != null ) {
			if( activeTool.isDisplayed() ) activeTool.callConceal();
		}

		activeTool = tool;

		if( activeTool != null ) {
			toolTabPane.getSelectionModel().select( tool.getOrder() );
			if( !activeTool.isDisplayed() ) activeTool.callDisplay();
		}
	}

	private Tool determineNextTool( Tool tool ) {
		Tool next = null;
		if( toolTabPane.getTabs().size() > 1 ) {
			int index = tool.getOrder();
			if( index < toolTabPane.getTabs().size() - 1 ) {
				next = (Tool)toolTabPane.getTabs().get( index + 1 ).getContent();
			} else if( index >= 1 ) {
				next = (Tool)toolTabPane.getTabs().get( index - 1 ).getContent();
			}
		}
		return next;
	}

	public boolean isActive() {
		return parent != null && parent.getActiveView() == this;
	}

	void setActive( boolean active ) {
		toolTabPane.setActive( active );
	}

	public boolean isDefault() {
		return parent != null && parent.getDefaultView() == this;
	}

	public boolean isMaximized() {
		return parent != null && parent.getMaximizedView() == this;
	}

	public WorkpaneEdge getEdge( Side direction ) {
		return switch( direction ) {
			case TOP -> getTopEdge();
			case LEFT -> getLeftEdge();
			case RIGHT -> getRightEdge();
			case BOTTOM -> getBottomEdge();
		};
	}

	public void setEdge( Side direction, WorkpaneEdge edge ) {
		switch( direction ) {
			case TOP: {
				setTopEdge( edge );
				if( edge != null ) edge.bottomViews.add( this );
				break;
			}
			case LEFT: {
				setLeftEdge( edge );
				if( edge != null ) edge.rightViews.add( this );
				break;
			}
			case RIGHT: {
				setRightEdge( edge );
				if( edge != null ) edge.leftViews.add( this );
				break;
			}
			case BOTTOM: {
				setBottomEdge( edge );
				if( edge != null ) edge.topViews.add( this );
				break;
			}
		}
	}

	public Workpane.Placement getPlacement() {
		return placement == null ? null : placementProperty().get();
	}

	public void setPlacement( Workpane.Placement placement ) {
		((ObjectProperty<Workpane.Placement>)placementProperty()).set( placement );
	}

	public ReadOnlyObjectProperty<Workpane.Placement> placementProperty() {
		if( placement == null ) placement = new SimpleObjectProperty<>();
		return placement;
	}

	@Override
	@SuppressWarnings( "StringBufferReplaceableByString" )
	public String toString() {
		Bounds bounds = getLayoutBounds();

		StringBuilder builder = new StringBuilder();
		builder.append( "<" );
		builder.append( getClass().getSimpleName() );
		builder.append( " id=" );
		builder.append( getUid() );
		builder.append( " bounds=" );
		builder.append( bounds.getMinX() ).append( "," ).append( bounds.getMinX() );
		builder.append( " " );
		builder.append( bounds.getWidth() ).append( "x" ).append( bounds.getHeight() );
		builder.append( ">" );

		return builder.toString();
	}

	public Workpane getWorkpane() {
		return parent;
	}

	double getCenter( Orientation orientation ) {
		return switch( orientation ) {
			case VERTICAL -> (getTopEdge().getPosition() + getBottomEdge().getPosition()) / 2;
			case HORIZONTAL -> (getLeftEdge().getPosition() + getRightEdge().getPosition()) / 2;
		};
	}

	void setWorkpane( Workpane parent ) {
		this.parent = parent;
	}

	void activateTool( Tool tool ) {
		getWorkpane().setActiveTool( tool );
	}

}
