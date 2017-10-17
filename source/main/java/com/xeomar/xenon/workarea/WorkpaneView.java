package com.xeomar.xenon.workarea;

import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.util.Configurable;
import com.xeomar.xenon.worktool.Tool;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkpaneView extends BorderPane implements Configurable {

	WorkpaneEdge topEdge;

	WorkpaneEdge leftEdge;

	WorkpaneEdge rightEdge;

	WorkpaneEdge bottomEdge;

	private Workpane.Placement placement;

	private TabPane tools;

	private Workpane parent;

	private Tool activeTool;

	private Settings settings;

	public WorkpaneView() {
		getStyleClass().add( "workpane-view" );
		setCenter( tools = new TabPane() );
	}

	public TabPane getToolTabPane() {
		return tools;
	}

	/**
	 * Get an unmodifiable list of the tools in the view.
	 *
	 * @return A list of the tools in the view.
	 */
	public List<Tool> getTools() {
		List<Tool> toolList = new ArrayList<Tool>();

		for( Tab tab : tools.getTabs() ) {
			toolList.add( (Tool)tab.getContent() );
		}

		return Collections.unmodifiableList( toolList );
	}

	public Tool addTool( Tool tool ) {
		return addTool( tool, tools.getTabs().size() );
	}

	public Tool addTool( Tool tool, int index ) {
		if( tool.getToolView() != null ) tool.getToolView().removeTool( tool );

		Tab tab = new Tab( tool.getTitle(), tool );
		tab.textProperty().bind( tool.titleProperty() );
		tab.setOnCloseRequest( event -> {
			event.consume();
			tool.close();
		} );
		tool.setToolView( this );
		tools.getTabs().add( index, tab );

		tool.callAllocate();

		if( tools.getTabs().size() == 1 ) setActiveTool( tool );

		return tool;
	}

	public Tool removeTool( Tool tool ) {
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

	public void setActiveTool( Tool tool ) {
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

	public int getToolIndex( Tool tool ) {
		int index = 0;

		for( Tab tab : tools.getTabs() ) {
			if( tab.getContent() == tool ) return index;
			index++;
		}

		return -1;
	}

	public boolean isActive() {
		return parent != null && parent.getActiveView() == this;
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
				topEdge.bottomViews.add( this );
				if( settings != null ) settings.set( "t", edge.getEdgeId() );
				break;
			}
			case LEFT: {
				leftEdge = edge;
				leftEdge.rightViews.add( this );
				// NEXT FIXME The setting is not being persisted
				//System.out.println( "Setting left edge: " + edge.getEdgeId() );
				if( settings != null ) settings.set( "l", edge.getEdgeId() );
				break;
			}
			case RIGHT: {
				rightEdge = edge;
				rightEdge.leftViews.add( this );
				if( settings != null ) settings.set( "r", edge.getEdgeId() );
				break;
			}
			case BOTTOM: {
				bottomEdge = edge;
				bottomEdge.topViews.add( this );
				if( settings != null ) settings.set( "b", edge.getEdgeId() );
				break;
			}
		}
	}

	public Workpane.Placement getPlacement() {
		return placement;
	}

	public void setPlacement( Workpane.Placement placement ) {
		this.placement = placement;
	}

	public double getCenter( Orientation orientation ) {
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

	@Override
	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;
		this.settings = settings;
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

	void setWorkpane( Workpane parent ) {
		this.parent = parent;
		// TODO Should workpanes have icons? If so, update them.
		//if( parent != null ) updateIcons();
	}

}
