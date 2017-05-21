package com.parallelsymmetry.essence.work;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workpane extends Pane {

	private ObjectProperty<View> activeViewProperty;

	private ObjectProperty<View> defaultViewProperty;

	private ObjectProperty<View> maximizedViewProperty;

	private ObjectProperty<Worktool> activeWorktoolProperty;

	public Workpane() {
		activeViewProperty = new SimpleObjectProperty<>();
		activeWorktoolProperty = new SimpleObjectProperty<>();
	}

	View getActiveView() {
		return activeViewProperty.get();
	}

	void setActiveView( View view ) {
		activeViewProperty.set( view );
	}

	ObjectProperty<View> activeViewProperty() {
		return activeViewProperty;
	}

	View getDefaultView() {
		return defaultViewProperty.get();
	}

	void setDefaultView( View view ) {
		defaultViewProperty.set( view );
	}

	ObjectProperty<View> defaultViewProperty() {
		return defaultViewProperty;
	}

	View getMaximizedView() {
		return maximizedViewProperty.get();
	}

	void setMaximizedView( View view ) {
		maximizedViewProperty.set( view );
	}

	ObjectProperty<View> maximizedViewProperty() {
		return maximizedViewProperty;
	}

	public Worktool getActiveWorktool() {
		return activeWorktoolProperty.get();
	}

	public void setActiveWorktool( Worktool worktool ) {
		activeWorktoolProperty.set( worktool );
	}

	public ObjectProperty<Worktool> activeWorktoolProperty() {
		return activeWorktoolProperty;
	}

	@Override
	protected void layoutChildren() {
		Bounds bounds = getLayoutBounds();
	}

	private double moveEdge( Edge edge, double delta ) {
		// NEXT Implement Workpane.moveEdge()

		return 0;
	}

	private class Edge extends Control {

		Orientation orientation;

		Edge northEdge;

		Edge southEdge;

		Edge westEdge;

		Edge eastEdge;

		Set<View> northViews;

		Set<View> southViews;

		Set<View> westViews;

		Set<View> eastViews;

		/**
		 * <p>Represents the location where the divider should ideally be
		 * positioned, between 0.0 and 1.0 (inclusive) when the position is
		 * relative. 0.0 represents the left- or top-most point, and 1.0 represents
		 * the right- or bottom-most point (depending on the orientation property).
		 * <p>
		 * <p>As the user drags the edge around this property will be updated to
		 * always represent its current location.
		 */
		private DoubleProperty position;

		private boolean absolute;

		private boolean wall;

		private Set<View> viewsA;

		private Set<View> viewsB;

		private Workpane parent;

		private Point2D anchor;

		public Edge( Orientation orientation ) {
			this( orientation, false );
		}

		public Edge( Orientation orientation, boolean wall ) {
			this.position = new SimpleDoubleProperty( this, "position", 0 );
			this.orientation = orientation;
			this.wall = wall;

			// Create the view lists.
			viewsA = new CopyOnWriteArraySet<View>();
			viewsB = new CopyOnWriteArraySet<View>();
			northViews = viewsA;
			southViews = viewsB;
			westViews = viewsA;
			eastViews = viewsB;

			// Set the control cursor
			setCursor( orientation == Orientation.VERTICAL ? Cursor.V_RESIZE : Cursor.H_RESIZE );

			// Set the default background
			setBackground( new Background( new BackgroundFill( Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY ) ) );

			// Register the mouse handlers
			onMousePressedProperty().set( this::mousePressed );
			onMouseReleasedProperty().set( this::mouseReleased );
			onMouseDraggedProperty().set( this::mouseDragged );
		}

		private void mousePressed( MouseEvent event ) {
			// If the mouse button is not the primary button don't process the event.
			if( event.getButton() != MouseButton.PRIMARY ) return;

			// TODO Should the workpane dividers request the input focus?
			// Request the focus
			parent.requestFocus();

			// Calculate the anchor point
			anchor = localToParent( event.getX(), event.getY() );
		}

		private void mouseReleased( MouseEvent event ) {
			// If the mouse button is not the primary button, don't process the event.
			if( event.getButton() != MouseButton.PRIMARY ) return;
			anchor = null;
		}

		private void mouseDragged( MouseEvent event ) {
			// If there is not an anchor point, don't process the event.
			if( anchor == null ) return;

			double delta = 0;
			double movement = 0;
			Point2D point = localToParent( event.getX(), event.getY() );

			switch( orientation ) {
				case VERTICAL: {
					delta = point.getX() - anchor.getX();
					movement = parent.moveEdge( this, delta );
					if( movement != delta ) point.add( movement - delta, 0 );
					break;
				}
				case HORIZONTAL: {
					delta = point.getY() - anchor.getY();
					movement = parent.moveEdge( this, delta );
					if( movement != delta ) point.add( 0, movement - delta );
					break;
				}
			}

			anchor = point;
		}

		public final boolean isWall() {
			return wall;
		}

		public final Orientation getOrientation() {
			return orientation;
		}

		public final double getPosition() {
			return position == null ? 0.5F : position.get();
		}

		public final void setPosition( double value ) {
			positionProperty().set( value );
		}

		public final DoubleProperty positionProperty() {
			return position;
		}

		Workpane getWorkpane() {
			return parent;
		}

		void setWorkpane( Workpane parent ) {
			this.parent = parent;
		}

		public Edge getEdge( Side direction ) {

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

		public void setEdge( Side direction, Edge edge ) {
			switch( direction ) {
				case TOP: {
					northEdge = edge;
					break;
				}
				case BOTTOM: {
					southEdge = edge;
					break;
				}
				case LEFT: {
					westEdge = edge;
					break;
				}
				case RIGHT: {
					eastEdge = edge;
					break;
				}
			}
		}

		public Set<View> getViews( Side direction ) {
			switch( direction ) {
				case TOP: {
					return northViews;
				}
				case BOTTOM: {
					return southViews;
				}
				case LEFT: {
					return westViews;
				}
				case RIGHT: {
					return eastViews;
				}
			}

			return null;
		}

	}

	/**
	 * View is not intended to be a public class.
	 */
	class View extends Pane {

		Edge northEdge;

		Edge southEdge;

		Edge westEdge;

		Edge eastEdge;

		private TabPane tools;

		private Workpane parent;

		private Worktool activeTool;

		public View() {
			setOpacity( 0 );

			tools = new TabPane();
			getChildren().add( tools );

			// TODO Escape had a special border to highlight the active view, do I want the same?
		}

		/**
		 * Get an unmodifiable list of the tools in the view.
		 *
		 * @return A list of the tools in the view.
		 */
		public List<Worktool> getTools() {
			List<Worktool> toolList = new ArrayList<Worktool>();

			for( Tab tab : tools.getTabs() ) {
				toolList.add( (Worktool)tab.getContent() );
			}

			return Collections.unmodifiableList( toolList );
		}

		public Worktool addTool( Worktool tool ) {
			return addTool( tool, tools.getTabs().size() );
		}

		public Worktool addTool( Worktool tool, int index ) {
			if( tool.getToolView() != null ) tool.getToolView().removeTool( tool );

			tool.setToolView( this );
			tools.getTabs().add( index, new Tab( tool.getTitle(), tool ) );

			Tab tab = new Tab( tool.getTitle(), tool );
			tab.textProperty().bind( tool.titleProperty() );

			tool.callAllocate();

			if( tools.getTabs().size() == 1 ) setActiveTool( tool );

			return tool;
		}

		public Worktool removeTool( Worktool tool ) {
			Worktool next = null;
			boolean isActiveTool = tool == activeTool;

			if( isActiveTool ) {
				// Determine the next tool for the view.
				if( tools.getTabs().size() > 1 ) {
					int index = getToolIndex( tool );
					if( index < tools.getTabs().size() - 1 ) {
						next = (Worktool)tools.getTabs().get( index + 1 ).getContent();
					} else if( index >= 1 ) {
						next = (Worktool)tools.getTabs().get( index - 1 ).getContent();
					}
				}

				// If the tool is the active tool set the active tool to null.
				parent.setActiveWorktool( null );
			}

			// If the tool is currently displayed, call conceal.
			if( tool.isDisplayed() ) tool.callConceal();

			tool.callDeallocate();

			// Remove the tool.
			tools.getTabs().remove( getToolIndex( tool ) );
			tool.setToolView( null );
			if( activeTool == tool ) activeTool = null;

			// Set the active tool.
			if( isActiveTool ) parent.setActiveWorktool( next );

			return tool;
		}

		public Worktool getActiveTool() {
			return activeTool;
		}

		public void setActiveTool( Worktool tool ) {
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

		public int getToolIndex( Worktool tool ) {
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

		public Edge getEdge( Side direction ) {

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

		public void setEdge( Side direction, Edge edge ) {
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

}
