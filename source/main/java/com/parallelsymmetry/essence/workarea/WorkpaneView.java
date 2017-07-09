package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.worktool.Tool;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkpaneView extends BorderPane {

	WorkpaneEdge northEdge;

	WorkpaneEdge southEdge;

	WorkpaneEdge westEdge;

	WorkpaneEdge eastEdge;

	private TabPane tools;

	private Workpane parent;

	private Tool activeTool;

	WorkpaneView() {
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

		tool.setToolView( this );
		tools.getTabs().add( index, new Tab( tool.getTitle(), tool ) );

		Tab tab = new Tab( tool.getTitle(), tool );
		tab.textProperty().bind( tool.titleProperty() );

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
			activeTool.callDisplay();
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
				return northEdge;
			}
			case BOTTOM: {
				return southEdge;
			}
			case LEFT: {
				return westEdge;
			}
			case RIGHT: {
				return eastEdge;
			}
		}

		return null;
	}

	public void setEdge( Side direction, WorkpaneEdge edge ) {
		switch( direction ) {
			case TOP: {
				northEdge = edge;
				northEdge.southViews.add( this );
				break;
			}
			case BOTTOM: {
				southEdge = edge;
				southEdge.northViews.add( this );
				break;
			}
			case LEFT: {
				westEdge = edge;
				westEdge.eastViews.add( this );
				break;
			}
			case RIGHT: {
				eastEdge = edge;
				eastEdge.westViews.add( this );
				break;
			}
		}
	}

	public double getCenter( Orientation orientation ) {
		switch( orientation ) {
			case HORIZONTAL: {
				return (westEdge.getPosition() + eastEdge.getPosition()) / 2;
			}
			case VERTICAL: {
				return (northEdge.getPosition() + southEdge.getPosition()) / 2;
			}
		}

		return Double.NaN;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( super.toString() );
		builder.append( "(" );
		builder.append( System.identityHashCode( this ) );
		builder.append( ")" );

		return builder.toString();
	}

	public Workpane getWorkPane() {
		return parent;
	}

	void setWorkPane( Workpane parent ) {
		this.parent = parent;
		// TODO Should workpanes have icons? If so, update them.
		//if( parent != null ) updateIcons();
	}

}
