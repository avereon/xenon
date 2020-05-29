package com.avereon.xenon.workpane;

import com.avereon.util.IdGenerator;
import com.avereon.util.Log;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkpaneView extends BorderPane {

	private static final System.Logger log = Log.get();

	private ObjectProperty<WorkpaneEdge> topEdge;

	private ObjectProperty<WorkpaneEdge> leftEdge;

	private ObjectProperty<WorkpaneEdge> rightEdge;

	private ObjectProperty<WorkpaneEdge> bottomEdge;

	private ObjectProperty<Workpane.Placement> placement;

	private ToolTabPane tools;

	private Workpane parent;

	private Tool activeTool;

	private String viewId;

	public WorkpaneView() {
		getStyleClass().add( "workpane-view" );

		setViewId( IdGenerator.getId() );
		setCenter( tools = new ToolTabPane() );
		setSnapToPixel( true );

		//tools.setTabClosingPolicy( TabPane.TabClosingPolicy.ALL_TABS );
		//tools.setTabDragPolicy( TabPane.TabDragPolicy.REORDER );

		// Add a focus listener to the tabs so when a tab is focused, the tool
		// is activated. This may happen even if the tab is not selected.
		//		tools.activeProperty().addListener( ( observable, oldValue, newValue ) -> {
		//			ToolTab tab = tools.getSelectionModel().getSelectedItem();
		//			if( newValue && tab != null ) activateTool( tab.getTool() );
		//		} );

		// Add a selection listener to the tabs so when a tab is selected, the tool
		// is activated. This may happen even if the tab is not focused.
		tools.getSelectionModel().selectedItemProperty().addListener( ( observable, oldValue, newValue ) -> {
			if( tools.focusedProperty().getValue() && newValue != null ) activateTool( newValue.getTool() );
		} );

		// Add a listener to the tab list to store the order when the tabs change
		tools.getTabs().addListener( (ListChangeListener<? super ToolTab>)( change ) -> {
			for( ToolTab tab : tools.getTabs() ) {
				Tool tool = tab.getTool();
				tool.fireEvent( new ToolEvent( null, ToolEvent.ORDERED, tool.getWorkpane(), tool ) );
			}
		} );
	}

	public String getViewId() {
		return viewId;
	}

	public void setViewId( String id ) {
		this.viewId = id;
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
		List<Tool> toolList = new ArrayList<>();

		for( ToolTab tab : tools.getTabs() ) {
			toolList.add( (Tool)tab.getContent() );
		}

		return Collections.unmodifiableList( toolList );
	}

	Tool addTool( Tool tool ) {
		return addTool( tool, tools.getTabs().size() );
	}

	Tool addTool( Tool tool, int index ) {
		if( tool.getToolView() != null ) tool.getToolView().removeTool( tool );
		tool.setToolView( this );
		tool.callAllocate();

		tools.getTabs().add( index, new ToolTab( tool ) );

		if( tools.getTabs().size() == 1 ) setActiveTool( tool );

		return tool;
	}

	Tool removeTool( Tool tool ) {
		boolean isActiveTool = tool == activeTool;

		Tool next = null;
		if( isActiveTool ) {
			// Determine the next tool for the view.
			if( tools.getTabs().size() > 1 ) {
				int index = getToolIndex( tool );
				if( index < tools.getTabs().size() - 1 ) {
					next = (Tool)tools.getTabs().get( index + 1 ).getContent();
				} else if( index >= 1 ) {
					next = (Tool)tools.getTabs().get( index - 1 ).getContent();
				}
			}

			// If the tool is the active tool set the active tool to null.
			if( parent != null ) parent.setActiveTool( null );
		}

		// If the tool is currently displayed, call conceal.
		if( tool.isDisplayed() ) tool.callConceal();
		tool.callDeallocate();

		// Remove the tool.
		tools.getTabs().remove( getToolIndex( tool ) );
		tool.setToolView( null );
		if( activeTool == tool ) activeTool = null;

		// Set the active tool.
		if( isActiveTool && parent != null ) parent.setActiveTool( next );

		return tool;
	}

	public Tool getActiveTool() {
		return activeTool;
	}

	void setActiveTool( Tool tool ) {
		if( tool == activeTool ) return;

		if( activeTool != null ) {
			if( activeTool.isDisplayed() ) activeTool.callConceal();
		}

		activeTool = tool;

		if( activeTool != null ) {
			tools.getSelectionModel().select( getToolIndex( tool ) );
			if( !activeTool.isDisplayed() ) activeTool.callDisplay();
		}
	}

	private int getToolIndex( Tool tool ) {
		int index = 0;

		for( ToolTab tab : tools.getTabs() ) {
			if( tab.getTool() == tool ) return index;
			index++;
		}

		return -1;
	}

	public boolean isActive() {
		return parent != null && parent.getActiveView() == this;
	}

	void setActive( boolean active ) {
		tools.setActive( active );
	}

	public boolean isDefault() {
		return parent != null && parent.getDefaultView() == this;
	}

	public boolean isMaximized() {
		return parent != null && parent.getMaximizedView() == this;
	}

	public WorkpaneEdge getEdge( Side direction ) {

		switch( direction ) {
			case TOP: {
				return getTopEdge();
			}
			case LEFT: {
				return getLeftEdge();
			}
			case RIGHT: {
				return getRightEdge();
			}
			case BOTTOM: {
				return getBottomEdge();
			}
		}

		return null;
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

	//	@Deprecated
	//	public void setSettings( Settings settings ) {
	//		if( settings == null ) {
	//			this.settings = null;
	//			return;
	//		} else if( this.settings != null ) {
	//			return;
	//		}

	//		this.settings = settings;

	// Restore state from settings
	//setViewId( settings.getName() );
	//if( settings.exists( "placement" ) ) setPlacement( Workpane.Placement.valueOf( settings.get( "placement" ).toUpperCase() ) );

	// Persist state to settings
	//		if( isActive() ) settings.set( "active", true );
	//		if( isDefault() ) settings.set( "default", true );
	//		if( isMaximized() ) settings.set( "maximized", true );
	//		settings.set( "placement", getPlacement() == null ? null : getPlacement().name().toLowerCase() );
	//	}

	//	@Deprecated
	//	public Settings getSettings() {
	//		return settings;
	//	}

	//	@Override
	//	public String toString() {
	//		return super.toString() + "(" + System.identityHashCode( this ) + ")";
	//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		Bounds bounds = getLayoutBounds();
		builder.append( "<" );
		builder.append( getClass().getSimpleName() );
		builder.append( " id=" );
		builder.append( getViewId() );
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
		switch( orientation ) {
			case VERTICAL: {
				return (getTopEdge().getPosition() + getBottomEdge().getPosition()) / 2;
			}
			case HORIZONTAL: {
				return (getLeftEdge().getPosition() + getRightEdge().getPosition()) / 2;
			}
		}

		return Double.NaN;
	}

	void setWorkpane( Workpane parent ) {
		this.parent = parent;
		// TODO Should workpanes have icons? If so, update them.
		//if( parent != null ) updateIcons();
	}

	void activateTool( Tool tool ) {
		Workpane workpane = getWorkpane();
		//if( workpane.getActiveTool() == tool ) return;
		workpane.setActiveTool( tool );
	}

}
