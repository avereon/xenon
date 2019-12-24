package com.avereon.xenon.workpane;

import com.avereon.settings.Settings;
import com.avereon.util.Configurable;
import com.avereon.util.LogUtil;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkpaneView extends BorderPane implements Configurable {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private WorkpaneEdge topEdge;

	private WorkpaneEdge leftEdge;

	private WorkpaneEdge rightEdge;

	private WorkpaneEdge bottomEdge;

	private Workpane.Placement placement;

	private ToolPane tools;

	private Workpane parent;

	private Tool activeTool;

	private Settings settings;

	public WorkpaneView() {
		getStyleClass().add( "workpane-view" );
		setCenter( tools = new ToolPane() );
		setSnapToPixel( true );

		//tools.setTabClosingPolicy( TabPane.TabClosingPolicy.ALL_TABS );
		//tools.setTabDragPolicy( TabPane.TabDragPolicy.REORDER );

		// Add a focus listener to the tabs so when a tab is focused, the tool
		// is activated. This may happen even if the tab is not selected.
		tools.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
			ToolTab tab = tools.getSelectionModel().getSelectedItem();
			if( newValue && tab != null ) activateTool( tab.getTool() );
		} );

		// Add a selection listener to the tabs so when a tab is selected, the tool
		// is activated. This may happen even if the tab is not focused.
		tools.getSelectionModel().selectedItemProperty().addListener( ( observable, oldValue, newValue ) -> {
			if( tools.focusedProperty().getValue() && newValue != null ) activateTool( newValue.getTool() );
		} );

		// Add a listener to the tab list to store the order when the tabs change
		tools.getTabs().addListener( (ListChangeListener<? super ToolTab>)( change ) -> {
			for( ToolTab tab : tools.getTabs() ){
				Tool tool = tab.getTool();
				tool.fireToolEvent( new ToolEvent( this, ToolEvent.ORDERED, tool ) );
			}
		} );
	}

	// FIXME Replace with Node.getId() when settings are removed
	public String getViewId() {
		return settings == null ? null : settings.getName();
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
				return topEdge;
			}
			case LEFT: {
				return leftEdge;
			}
			case RIGHT: {
				return rightEdge;
			}
			case BOTTOM: {
				return bottomEdge;
			}
		}

		return null;
	}

	public void setEdge( Side direction, WorkpaneEdge edge ) {
		switch( direction ) {
			case TOP: {
				topEdge = edge;
				if( edge != null ) edge.bottomViews.add( this );
				if( settings != null ) settings.set( "t", edge == null ? null : edge.getEdgeId() );
				break;
			}
			case LEFT: {
				leftEdge = edge;
				if( edge != null ) edge.rightViews.add( this );
				if( settings != null ) settings.set( "l", edge == null ? null : edge.getEdgeId() );
				break;
			}
			case RIGHT: {
				rightEdge = edge;
				if( edge != null ) edge.leftViews.add( this );
				if( settings != null ) settings.set( "r", edge == null ? null : edge.getEdgeId() );
				break;
			}
			case BOTTOM: {
				bottomEdge = edge;
				if( edge != null ) edge.topViews.add( this );
				if( settings != null ) settings.set( "b", edge == null ? null : edge.getEdgeId() );
				break;
			}
		}
	}

	public Workpane.Placement getPlacement() {
		return placement;
	}

	public void setPlacement( Workpane.Placement placement ) {
		this.placement = placement;
		if( settings != null ) settings.set( "placement", placement == null ? null : placement.name().toLowerCase() );
	}

	@Override
	public void setSettings( Settings settings ) {
		if( settings == null ) {
			this.settings = null;
			return;
		} else if( this.settings != null ) {
			return;
		}

		this.settings = settings;

		// Restore state from settings
		String placementValue = settings.get( "placement" );
		if( placementValue != null ) setPlacement( Workpane.Placement.valueOf( placementValue.toUpperCase() ) );

		// Persist state to settings
		if( isActive() ) settings.set( "active", true );
		if( isDefault() ) settings.set( "default", true );
		if( isMaximized() ) settings.set( "maximized", true );
		settings.set( "placement", getPlacement() == null ? null : getPlacement().name().toLowerCase() );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + System.identityHashCode( this ) + ")";
	}

	public Workpane getWorkpane() {
		return parent;
	}

	double getCenter( Orientation orientation ) {
		switch( orientation ) {
			case VERTICAL: {
				return (topEdge.getPosition() + bottomEdge.getPosition()) / 2;
			}
			case HORIZONTAL: {
				return (leftEdge.getPosition() + rightEdge.getPosition()) / 2;
			}
		}

		return Double.NaN;
	}

	void setWorkpane( Workpane parent ) {
		this.parent = parent;
		// TODO Should workpanes have icons? If so, update them.
		//if( parent != null ) updateIcons();
	}

	private void activateTool( Tool tool ) {
		Workpane workpane = getWorkpane();
		//if( workpane.getActiveTool() == tool ) return;
		workpane.setActiveTool( tool );
	}

}
