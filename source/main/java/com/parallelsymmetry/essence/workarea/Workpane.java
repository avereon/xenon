package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.worktool.CloseOperation;
import com.parallelsymmetry.essence.worktool.Tool;
import com.parallelsymmetry.essence.worktool.ToolEvent;
import com.parallelsymmetry.essence.worktool.ToolVetoException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class Workpane extends Pane {

	public enum Placement {
		DEFAULT,
		ACTIVE,
		LARGEST,
		SMART
	}

	public static final double DEFAULT_VIEW_SPLIT_RATIO = 0.20;

	public static final double DEFAULT_WALL_SPLIT_RATIO = 0.25;

	private static final Logger log = LoggerFactory.getLogger( Workpane.class );

	private Edge northEdge;

	private Edge southEdge;

	private Edge westEdge;

	private Edge eastEdge;

	private DoubleProperty edgeSize;

	private ObjectProperty<ToolView> activeViewProperty;

	private ObjectProperty<ToolView> defaultViewProperty;

	private ObjectProperty<ToolView> maximizedViewProperty;

	private ObjectProperty<Tool> activeWorktoolProperty;

	private AtomicInteger operation;

	private Queue<WorkpaneEvent> events;

	private Collection<WorkpaneListener> listeners;

	public Workpane() {
		edgeSize = new SimpleDoubleProperty();
		activeViewProperty = new SimpleObjectProperty<>();
		defaultViewProperty = new SimpleObjectProperty<>();
		maximizedViewProperty = new SimpleObjectProperty<>();
		activeWorktoolProperty = new SimpleObjectProperty<>();

		operation = new AtomicInteger();
		events = new LinkedList<WorkpaneEvent>();
		listeners = new CopyOnWriteArraySet<>();

		// Create the wall edges
		northEdge = new Edge( Orientation.HORIZONTAL, true );
		southEdge = new Edge( Orientation.HORIZONTAL, true );
		westEdge = new Edge( Orientation.VERTICAL, true );
		eastEdge = new Edge( Orientation.VERTICAL, true );

		// Set the workpane on the edges
		northEdge.setWorkpane( this );
		southEdge.setWorkpane( this );
		westEdge.setWorkpane( this );
		eastEdge.setWorkpane( this );

		// Set the edge positions
		northEdge.setPosition( 0 );
		southEdge.setPosition( 1 );
		westEdge.setPosition( 0 );
		eastEdge.setPosition( 1 );

		// Add the edges to the workpane
		getChildren().add( northEdge );
		getChildren().add( southEdge );
		getChildren().add( westEdge );
		getChildren().add( eastEdge );

		// Create the initial view
		ToolView view = new ToolView();

		// Add the view to the wall edges
		northEdge.southViews.add( view );
		southEdge.northViews.add( view );
		westEdge.eastViews.add( view );
		eastEdge.westViews.add( view );

		// Set the edges on the view
		view.northEdge = northEdge;
		view.southEdge = southEdge;
		view.westEdge = westEdge;
		view.eastEdge = eastEdge;

		// Add the initial view
		addView( view );
		setActiveView( view );
		setDefaultView( view );

		// TODO Set a better default background
		setBackground( new Background( new BackgroundFill( new Color( 0.2, 0.2, 0.2, 1.0 ), CornerRadii.EMPTY, Insets.EMPTY ) ) );
	}

	/**
	 * Returns an unmodifiable list of the edges.
	 *
	 * @return
	 */
	public Set<Edge> getEdges() {
		Set<Edge> edges = new HashSet<>();

		// Count the edges that are not walls
		for( Node node : getChildren() ) {
			if( node instanceof Edge ) edges.add( (Edge)node );
		}
		edges.remove( northEdge );
		edges.remove( southEdge );
		edges.remove( westEdge );
		edges.remove( eastEdge );

		return Collections.unmodifiableSet( edges );
	}

	/**
	 * Returns an unmodifiable list of the views.
	 *
	 * @return
	 */
	public Set<ToolView> getViews() {
		Set<ToolView> views = new HashSet<>();
		for( Node node : getChildren() ) {
			if( node instanceof ToolView ) views.add( (ToolView)node );
		}
		return Collections.unmodifiableSet( views );
	}

	/**
	 * Get an unmodifiable set of the tools.
	 *
	 * @return An unmodifiable set of the tools.
	 */
	public Set<Tool> getTools() {
		Set<Tool> tools = new HashSet<Tool>();
		for( ToolView view : getViews() ) {
			tools.addAll( view.getTools() );
		}
		return Collections.unmodifiableSet( tools );
	}

	public double getEdgeSize() {
		return edgeSize.get();
	}

	public void setEdgeSize( double size ) {
		edgeSize.set( size );
		layoutChildren();
	}

	public DoubleProperty edgeSize() {
		return edgeSize;
	}

	public Tool getActiveTool() {
		return activeWorktoolProperty.get();
	}

	public void setActiveTool( Tool tool ) {
		doSetActiveTool( tool, true );
	}

	ToolView getActiveView() {
		return activeViewProperty.get();
	}

	void setActiveView( ToolView view ) {
		doSetActiveView( view, true );
	}

	ObjectProperty<ToolView> activeViewProperty() {
		return activeViewProperty;
	}

	ToolView getDefaultView() {
		return defaultViewProperty.get();
	}

	void setDefaultView( ToolView view ) {
		if( getDefaultView() == view ) return;
		defaultViewProperty.set( view );
		updateComponentTree( true );
	}

	ObjectProperty<ToolView> defaultViewProperty() {
		return defaultViewProperty;
	}

	ToolView getMaximizedView() {
		return maximizedViewProperty.get();
	}

	void setMaximizedView( ToolView view ) {
		if( getMaximizedView() == view ) return;
		maximizedViewProperty.set( view );
		updateComponentTree( true );
	}

	ObjectProperty<ToolView> maximizedViewProperty() {
		return maximizedViewProperty;
	}

	//	public Tool getActiveWorktool() {
	//		return activeWorktoolProperty.get();
	//	}

	//	public void setActiveWorktool( Tool worktool ) {
	//		activeWorktoolProperty.set( worktool );
	//	}

	public ObjectProperty<Tool> activeWorktoolProperty() {
		return activeWorktoolProperty;
	}

	public ToolView getLargestView() {
		ToolView view = null;

		for( ToolView testView : getViews() ) {
			if( compareViewArea( testView, view ) > 0 ) view = testView;
		}

		return view;
	}

	/**
	 * Find a view with the following rules:
	 * <ol>
	 * <li>Use a singular large view</li>
	 * <li>Use the active view</li>
	 * <li>Use the default view</li>
	 * </ol>
	 *
	 * @return
	 */
	private ToolView getSmartView() {
		// Collect the view areas
		int index = 0;
		double maxArea = 0;
		ToolView largest = null;
		double[] areas = new double[ getViews().size() ];
		for( ToolView view : getViews() ) {
			Bounds size = view.getBoundsInLocal();
			double area = size.getWidth() * size.getHeight();
			if( area > maxArea ) {
				maxArea = area;
				largest = view;
			}
			areas[ index++ ] = area;
		}

		// Count the number of "large" views
		int count = 0;
		double threshold = maxArea / 2;
		for( double area : areas ) {
			if( area > threshold ) count++;
		}

		// If there is only one large view, use it, otherwise get the active view
		ToolView view = count == 1 ? largest : getActiveView();

		// If there was not definite large view and no active view just use the default view
		return view != null ? view : getDefaultView();
	}

	boolean isOperationActive() {
		return operation.get() > 0;
	}

	void startOperation() {
		operation.incrementAndGet();
	}

	void finishOperation( boolean changed ) {
		int value = operation.decrementAndGet();
		if( value < 0 ) log.error( "Operation flag is less than zero." );
		updateComponentTree( changed );
	}

	// TODO Rename to addWorkpaneListener
	public void addWorkPaneListener( WorkpaneListener listener ) {
		listeners.add( listener );
	}

	// TODO Rename to removeWorkpaneListener
	public void removeWorkPaneListener( WorkpaneListener listener ) {
		listeners.remove( listener );
	}

	void fireViewWillSplit( WorkpaneEvent event ) throws WorkpaneVetoException {
		WorkpaneVetoException exception = null;

		for( WorkpaneListener listener : listeners ) {
			try {
				listener.viewWillSplit( event );
			} catch( WorkpaneVetoException vetoException ) {
				if( exception == null ) exception = vetoException;
			}
		}

		if( exception != null ) throw exception;
	}

	void fireViewWillMerge( WorkpaneEvent event ) throws WorkpaneVetoException {
		WorkpaneVetoException exception = null;

		for( WorkpaneListener listener : listeners ) {
			try {
				listener.viewWillMerge( event );
			} catch( WorkpaneVetoException vetoException ) {
				if( exception == null ) exception = vetoException;
			}
		}

		if( exception != null ) throw exception;
	}

	protected void updateComponentTree( boolean changed ) {
		if( isOperationActive() ) return;
		if( changed ) events.offer( new WorkpaneEvent( this, WorkpaneEvent.Type.CHANGED, this ) );

		// CLEANUP ?
		//		synchronized( getTreeLock() ) {
		//			validateTree();
		//		}
		//		revalidate();
		//		repaint();

		dispatchEvents();
	}

	void queueEvent( WorkpaneEvent data ) {
		if( !isOperationActive() ) throw new RuntimeException( "Event should only be queued during active operations: " + data.getType() );
		events.offer( data );
	}

	void dispatchEvents() {
		for( WorkpaneEvent event : new LinkedList<WorkpaneEvent>( events ) ) {
			events.remove( event );

			for( WorkpaneListener listener : listeners ) {
				switch( event.getType() ) {
					case CHANGED: {
						listener.paneChanged( event );
						break;
					}
					case VIEW_ADDED: {
						listener.viewAdded( event );
						break;
					}
					case VIEW_REMOVED: {
						listener.viewRemoved( event );
						break;
					}
					case VIEW_MERGED: {
						listener.viewMerged( event );
						break;
					}
					case VIEW_SPLIT: {
						listener.viewSplit( event );
						break;
					}
					case VIEW_ACTIVATED: {
						listener.viewActivated( event );
						break;
					}
					case VIEW_DEACTIVATED: {
						listener.viewDeactivated( event );
						break;
					}
					case TOOL_ADDED: {
						listener.toolAdded( event );
						break;
					}
					case TOOL_REMOVED: {
						listener.toolRemoved( event );
						break;
					}
					case TOOL_ACTIVATED: {
						listener.toolActivated( event );
						break;
					}
					case TOOL_DEACTIVATED: {
						listener.toolDeactivated( event );
						break;
					}
				}
			}
		}
	}

	private void doSetActiveView( ToolView view, boolean setTool ) {
		if( view != null && (view == getActiveView() || !getViews().contains( view )) ) return;

		startOperation();
		try {
			ToolView activeToolView = getActiveView();

			if( activeToolView != null ) {
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_DEACTIVATED, this, activeToolView, null ) );
			}

			// Change the active view
			activeViewProperty().set( view );

			// Change the active tool
			if( setTool ) doSetActiveTool( view.getActiveTool(), false );

			// Handle the new active view
			activeToolView = getActiveView();
			if( activeToolView != null ) {
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_ACTIVATED, this, activeToolView, null ) );
			}
		} finally {
			finishOperation( true );
		}
	}

	private void doSetActiveTool( Tool tool, boolean setView ) {
		if( tool != null ) {
			ToolView view = tool.getToolView();
			if( view == null || !getViews().contains( view ) ) return;
		}

		Tool activeTool;
		startOperation();
		try {
			activeTool = getActiveTool();
			if( activeTool != null ) {
				activeTool.callDeactivate();
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.TOOL_DEACTIVATED, this, activeTool.getToolView(), activeTool ) );
			}

			// Change the active view
			ToolView view = tool == null ? null : tool.getToolView();
			if( view != null && getViews().contains( view ) ) {
				view.setActiveTool( tool );
				if( setView && view != getActiveView() ) doSetActiveView( view, false );
			}

			// Change the active tool
			activeWorktoolProperty().set( tool );

			activeTool = getActiveTool();
			if( activeTool != null ) {
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.TOOL_ACTIVATED, this, activeTool.getToolView(), activeTool ) );
				activeTool.callActivate();
			}
		} finally {
			finishOperation( true );
		}
	}

	private double compareViewArea( ToolView view1, ToolView view2 ) {
		Bounds size1 = view1.getBoundsInLocal();
		Bounds size2 = view2.getBoundsInLocal();
		double area1 = size1.getWidth() * size1.getHeight();
		double area2 = size2.getWidth() * size2.getHeight();
		return area1 - area2;
	}

	private ToolView addView( ToolView view ) {
		if( view == null ) return view;

		startOperation();
		try {
			view.setWorkPane( this );
			getChildren().add( view );
			queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_ADDED, this, view, null ) );
		} finally {
			finishOperation( true );
		}

		return view;
	}

	public ToolView removeView( ToolView view ) {
		if( view == null ) return view;

		try {
			startOperation();

			getChildren().remove( view );
			view.setWorkPane( null );

			view.northEdge.southViews.remove( view );
			view.southEdge.northViews.remove( view );
			view.westEdge.eastViews.remove( view );
			view.eastEdge.westViews.remove( view );
			view.northEdge = null;
			view.southEdge = null;
			view.westEdge = null;
			view.eastEdge = null;

			queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_REMOVED, this, view, null ) );
		} finally {
			finishOperation( true );
		}

		return view;
	}

	public Edge getWallEdge( Side direction ) {
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

	public Edge addEdge( Edge edge ) {
		if( edge == null ) return edge;

		edge.setWorkpane( this );
		getChildren().add( edge );

		return edge;
	}

	public Edge removeEdge( Edge edge ) {
		if( edge == null ) return edge;

		getChildren().remove( edge );
		edge.setWorkpane( null );

		return edge;
	}

	public boolean canSplit( ToolView target, Side direction ) {
		if( target == null ) return false;
		return getMaximizedView() == null;
	}

	/**
	 * Split the workpane using the space in the specified direction to make a new
	 * tool view along the entire edge of the workpane.
	 *
	 * @param direction
	 * @return
	 */
	public ToolView split( Side direction ) {
		return split( direction, DEFAULT_WALL_SPLIT_RATIO );
	}

	/**
	 * Split the workpane using the space in the specified direction to make a new
	 * tool view along the entire edge of the workpane. The new tool view is
	 * created using the specified percentage of the original space.
	 *
	 * @param direction
	 * @param percent
	 * @return
	 */
	public ToolView split( Side direction, double percent ) {
		ToolView result = null;
		startOperation();
		try {
			// Calculate the location of the split.
			switch( direction ) {
				case TOP: {
					result = splitNorth( percent );
					break;
				}
				case BOTTOM: {
					result = splitSouth( percent );
					break;
				}
				case LEFT: {
					result = splitWest( percent );
					break;
				}
				case RIGHT: {
					result = splitEast( percent );
					break;
				}
			}
			// TODO Does workpane maintain icons?
			//result.updateIcons();

			queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_SPLIT, this, null, null ) );
		} finally {
			finishOperation( true );
		}

		return result;
	}

	/**
	 * Split an existing tool view using the space in the specified direction to
	 * create a new tool view.
	 *
	 * @param view
	 * @param direction
	 * @return
	 */
	public ToolView split( ToolView view, Side direction ) {
		return split( view, direction, DEFAULT_VIEW_SPLIT_RATIO );
	}

	/**
	 * Split an existing tool view using the space in the specified direction to
	 * create a new tool view. The new tool view is created using the specified
	 * percentage of the original space.
	 *
	 * @param view
	 * @param direction
	 * @param percent
	 * @return
	 */
	public ToolView split( ToolView view, Side direction, double percent ) {
		ToolView result = null;
		startOperation();
		try {
			// Calculate the location of the split.
			switch( direction ) {
				case TOP: {
					result = splitNorth( view, percent );
					break;
				}
				case BOTTOM: {
					result = splitSouth( view, percent );
					break;
				}
				case LEFT: {
					result = splitWest( view, percent );
					break;
				}
				case RIGHT: {
					result = splitEast( view, percent );
					break;
				}
			}
			// TODO Does workpane maintain icons?
			//result.updateIcons();
			queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_SPLIT, this, view, null ) );
		} finally {
			finishOperation( true );
		}

		return result;
	}

	/**
	 * Split the workpane using the space to the north for a new tool view along
	 * the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private ToolView splitNorth( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.HORIZONTAL );
		newEdge.westEdge = westEdge;
		newEdge.eastEdge = eastEdge;
		newEdge.setPosition( percent );
		addEdge( newEdge );

		// Connect the new edge to the old and new views.
		for( ToolView view : northEdge.southViews ) {
			northEdge.southViews.remove( view );
			newEdge.southViews.add( view );
			view.northEdge = newEdge;
		}
		newEdge.northViews.add( newView );

		// Connect the new view to old and new edges.
		newView.northEdge = northEdge;
		newView.southEdge = newEdge;
		newView.westEdge = westEdge;
		newView.eastEdge = eastEdge;

		// Connect the old edges to the new view.
		northEdge.southViews.add( newView );
		westEdge.eastViews.add( newView );
		eastEdge.westViews.add( newView );

		// Connect the old edges to the new edge.
		for( Edge edge : getEdges() ) {
			if( edge.northEdge != northEdge ) continue;
			edge.northEdge = newEdge;
		}

		return newView;
	}

	/**
	 * Split the workpane using the space to the south for a new tool view along
	 * the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private ToolView splitSouth( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.HORIZONTAL );
		newEdge.westEdge = westEdge;
		newEdge.eastEdge = eastEdge;
		newEdge.setPosition( 1.0 - percent );
		addEdge( newEdge );

		// Connect the new edge to the old and new views.
		for( ToolView view : southEdge.northViews ) {
			southEdge.northViews.remove( view );
			newEdge.northViews.add( view );
			view.southEdge = newEdge;
		}
		newEdge.southViews.add( newView );

		// Connect the new view to old and new edges.
		newView.northEdge = newEdge;
		newView.southEdge = southEdge;
		newView.westEdge = westEdge;
		newView.eastEdge = eastEdge;

		// Connect the old edges to the new view.
		southEdge.northViews.add( newView );
		westEdge.eastViews.add( newView );
		eastEdge.westViews.add( newView );

		// Connect the old edges to the new edge.
		for( Edge edge : getEdges() ) {
			if( edge.southEdge != southEdge ) continue;
			edge.southEdge = newEdge;
		}

		return newView;
	}

	/**
	 * Split the workpane using the space to the west for a new tool view along
	 * the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private ToolView splitWest( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.VERTICAL );
		newEdge.northEdge = northEdge;
		newEdge.southEdge = southEdge;
		newEdge.setPosition( percent );
		addEdge( newEdge );

		// Connect the new edge to the old and new views.
		for( ToolView view : westEdge.eastViews ) {
			westEdge.eastViews.remove( view );
			newEdge.eastViews.add( view );
			view.westEdge = newEdge;
		}
		newEdge.westViews.add( newView );

		// Connect the new view to old and new edges.
		newView.northEdge = northEdge;
		newView.southEdge = southEdge;
		newView.westEdge = westEdge;
		newView.eastEdge = newEdge;

		// Connect the old edges to the new view.
		westEdge.eastViews.add( newView );
		northEdge.southViews.add( newView );
		southEdge.northViews.add( newView );

		// Connect the old edges to the new edge.
		for( Edge edge : getEdges() ) {
			if( edge.westEdge != westEdge ) continue;
			edge.westEdge = newEdge;
		}

		return newView;
	}

	/**
	 * Split the workpane using the space to the east for a new tool view along
	 * the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private ToolView splitEast( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.VERTICAL );
		newEdge.northEdge = northEdge;
		newEdge.southEdge = southEdge;
		newEdge.setPosition( 1.0 - percent );
		addEdge( newEdge );

		// Connect the new edge to the old and new views.
		for( ToolView view : eastEdge.westViews ) {
			eastEdge.westViews.remove( view );
			newEdge.westViews.add( view );
			view.eastEdge = newEdge;
		}
		newEdge.eastViews.add( newView );

		// Connect the new view to old and new edges.
		newView.northEdge = northEdge;
		newView.southEdge = southEdge;
		newView.westEdge = newEdge;
		newView.eastEdge = eastEdge;

		// Connect the old edges to the new view.
		eastEdge.westViews.add( newView );
		northEdge.southViews.add( newView );
		southEdge.northViews.add( newView );

		// Connect the old edges to the new edge.
		for( Edge edge : getEdges() ) {
			if( edge.eastEdge != eastEdge ) continue;
			edge.eastEdge = newEdge;
		}

		return newView;
	}

	/**
	 * Split an existing tool view using the space to the north for a new tool
	 * view.
	 *
	 * @param source
	 * @param percent
	 * @return
	 */
	private ToolView splitNorth( ToolView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "ToolView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireViewWillSplit( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source, null ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.HORIZONTAL );
		newEdge.westEdge = source.westEdge;
		newEdge.eastEdge = source.eastEdge;
		addEdge( newEdge );

		// Connect the new edge to the new and old views.
		newEdge.northViews.add( newView );
		newEdge.southViews.add( source );

		// Connect the new view to old and new edges.
		newView.northEdge = source.northEdge;
		newView.southEdge = newEdge;
		newView.eastEdge = source.eastEdge;
		newView.westEdge = source.westEdge;

		// Connect the old edges to the new view.
		source.northEdge.southViews.remove( source );
		source.northEdge.southViews.add( newView );
		source.westEdge.eastViews.add( newView );
		source.eastEdge.westViews.add( newView );

		// Connect the old view to the new edge.
		source.northEdge = newEdge;

		// Move the new edge to new position.
		newEdge.setPosition( newView.northEdge.getPosition() + ((source.southEdge.getPosition() - newView.northEdge.getPosition()) * percent) );

		//source.invalidate();

		return newView;
	}

	private ToolView splitSouth( ToolView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "ToolView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireViewWillSplit( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source, null ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.HORIZONTAL );
		newEdge.westEdge = source.westEdge;
		newEdge.eastEdge = source.eastEdge;
		addEdge( newEdge );

		// Connect the new edge to the new and old views.
		newEdge.northViews.add( source );
		newEdge.southViews.add( newView );

		// Connect the new view to old and new edges.
		newView.northEdge = newEdge;
		newView.southEdge = source.southEdge;
		newView.eastEdge = source.eastEdge;
		newView.westEdge = source.westEdge;

		// Connect the old edges to the new view.
		source.southEdge.northViews.remove( source );
		source.southEdge.northViews.add( newView );
		source.westEdge.eastViews.add( newView );
		source.eastEdge.westViews.add( newView );

		// Connect the old view to the new edge.
		source.southEdge = newEdge;

		// Move the new edge to new position.
		newEdge.setPosition( newView.southEdge.getPosition() - ((newView.southEdge.getPosition() - source.northEdge.getPosition()) * percent) );

		//source.invalidate();

		return newView;
	}

	private ToolView splitWest( ToolView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "ToolView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireViewWillSplit( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source, null ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.VERTICAL );
		newEdge.northEdge = source.northEdge;
		newEdge.southEdge = source.southEdge;
		addEdge( newEdge );

		// Connect the new edge to the new and old views.
		newEdge.westViews.add( newView );
		newEdge.eastViews.add( source );

		// Connect the new view to old and new edges.
		newView.eastEdge = newEdge;
		newView.northEdge = source.northEdge;
		newView.southEdge = source.southEdge;
		newView.westEdge = source.westEdge;

		// Connect the old edges to the new view.
		source.westEdge.eastViews.remove( source );
		source.westEdge.eastViews.add( newView );
		source.northEdge.southViews.add( newView );
		source.southEdge.northViews.add( newView );

		// Connect the old view to the new edge.
		source.westEdge = newEdge;

		// Move the new edge to new position.
		newEdge.setPosition( newView.westEdge.getPosition() + ((source.eastEdge.getPosition() - newView.westEdge.getPosition()) * percent) );

		//source.invalidate();

		return newView;
	}

	private ToolView splitEast( ToolView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "ToolView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireViewWillSplit( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source, null ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		// Create the new view.
		ToolView newView = new ToolView();
		addView( newView );

		// Create the new edge.
		Edge newEdge = new Edge( Orientation.VERTICAL );
		newEdge.northEdge = source.northEdge;
		newEdge.southEdge = source.southEdge;
		addEdge( newEdge );

		// Connect the new edge to the new and old views.
		newEdge.westViews.add( source );
		newEdge.eastViews.add( newView );

		// Connect the new view to old and new edges.
		newView.westEdge = newEdge;
		newView.northEdge = source.northEdge;
		newView.southEdge = source.southEdge;
		newView.eastEdge = source.eastEdge;

		// Connect the old edges to the new view.
		source.eastEdge.westViews.remove( source );
		source.eastEdge.westViews.add( newView );
		source.northEdge.southViews.add( newView );
		source.southEdge.northViews.add( newView );

		// Connect the old view to the new edge.
		source.eastEdge = newEdge;

		// Move the new edge to new position.
		newEdge.setPosition( newView.eastEdge.getPosition() - ((newView.eastEdge.getPosition() - source.westEdge.getPosition()) * percent) );

		//source.invalidate();

		return newView;
	}

	private static Side getReverseDirection( Side direction ) {
		switch( direction ) {
			case TOP: {
				return Side.BOTTOM;
			}
			case BOTTOM: {
				return Side.TOP;
			}
			case LEFT: {
				return Side.RIGHT;
			}
			case RIGHT: {
				return Side.LEFT;
			}
		}

		return null;
	}

	private static Side getLeftDirection( Side direction ) {
		switch( direction ) {
			case TOP: {
				return Side.LEFT;
			}
			case BOTTOM: {
				return Side.RIGHT;
			}
			case LEFT: {
				return Side.BOTTOM;
			}
			case RIGHT: {
				return Side.TOP;
			}
		}

		return null;
	}

	private static Side getRightDirection( Side direction ) {
		switch( direction ) {
			case TOP: {
				return Side.RIGHT;
			}
			case BOTTOM: {
				return Side.LEFT;
			}
			case LEFT: {
				return Side.TOP;
			}
			case RIGHT: {
				return Side.BOTTOM;
			}
		}

		return null;
	}

	private static Orientation getPerpendicularDirectionOrientation( Side direction ) {
		switch( direction ) {
			case TOP:
			case BOTTOM: {
				return Orientation.HORIZONTAL;
			}
			case LEFT:
			case RIGHT: {
				return Orientation.VERTICAL;
			}
		}

		return null;
	}

	/**
	 * Performs an automatic pull merge. The direction is automatically determined
	 * by a weighted algorithm.
	 *
	 * @param target
	 * @return
	 */
	public boolean pullMerge( ToolView target ) {
		Side direction = getPullMergeDirection( target, true );
		if( direction == null ) return false;
		return pullMerge( target, direction );
	}

	/**
	 * Performs a pull merge in the specified direction.
	 *
	 * @param target
	 * @param direction
	 * @return
	 */
	public boolean pullMerge( ToolView target, Side direction ) {
		// Check the parameters.
		if( target == null ) return false;

		boolean result = false;
		try {
			startOperation();
			result = merge( target.getEdge( getReverseDirection( direction ) ), direction );
			if( result ) queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_MERGED, this, target, null ) );
		} finally {
			finishOperation( result );
		}

		return result;
	}

	/**
	 * Performs a push merge in the specified direction.
	 *
	 * @param source
	 * @param direction
	 * @return
	 */
	public boolean pushMerge( ToolView source, Side direction ) {
		// Check the parameters.
		if( source == null ) return false;

		boolean result = false;
		try {
			startOperation();
			result = merge( source.getEdge( direction ), direction );
			if( result ) queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_MERGED, this, source, null ) );
		} finally {
			finishOperation( result );
		}

		return result;
	}

	public boolean canPushMerge( ToolView source, Side direction, boolean auto ) {
		return canMerge( source.getEdge( direction ), direction, auto );
	}

	public boolean canPullMerge( ToolView target, Side direction, boolean auto ) {
		return canMerge( target.getEdge( getReverseDirection( direction ) ), direction, auto );
	}

	/**
	 * Returns whether views on the source (opposite of direction) side of the
	 * edge can be merged into the space occupied by the views on the target
	 * (towards direction) side of the edge. The method returns false if any of
	 * the following conditions exist:
	 * <ul>
	 * <li>If the edge is an end edge.</li>
	 * <li>If any of the target views is the default view.</li>
	 * <li>If the target views do not share a common back edge.</li>
	 * <li>If the auto flag is set to true and any of the target views have tools.
	 * </li>
	 * </ul>
	 *
	 * @param edge The edge across which views are to be merged.
	 * @param direction The direction of the merge.
	 * @param auto Check if views can automatically be merged.
	 * @return
	 */
	private boolean canMerge( Edge edge, Side direction, boolean auto ) {
		if( edge == null ) return false;

		// Check for end edge.
		if( edge.isWall() ) return false;

		Set<ToolView> targets = null;
		switch( direction ) {
			case TOP: {
				targets = edge.northViews;
				break;
			}
			case BOTTOM: {
				targets = edge.southViews;
				break;
			}
			case LEFT: {
				targets = edge.westViews;
				break;
			}
			case RIGHT: {
				targets = edge.eastViews;
				break;
			}
		}

		Edge commonBackEdge = null;
		for( ToolView target : targets ) {
			// Check for the default view in targets.
			if( target == getDefaultView() ) return false;

			// If auto, check the tool counts.
			if( auto && target.getTools().size() > 0 ) return false;

			// Check targets for common back edge.
			if( commonBackEdge == null ) commonBackEdge = target.getEdge( direction );
			if( target.getEdge( direction ) != commonBackEdge ) return false;
		}

		return true;
	}

	private Side getPullMergeDirection( ToolView target, boolean auto ) {
		List<MergeDirection> directions = new ArrayList<MergeDirection>( 4 );

		directions.add( new MergeDirection( target, Side.TOP ) );
		directions.add( new MergeDirection( target, Side.BOTTOM ) );
		directions.add( new MergeDirection( target, Side.LEFT ) );
		directions.add( new MergeDirection( target, Side.RIGHT ) );

		Collections.sort( directions );

		for( MergeDirection direction : directions ) {
			if( canPullMerge( target, direction.direction, auto ) ) return direction.direction;
		}

		int weight = directions.get( 0 ).getWeight();

		return weight == 0 ? null : directions.get( 0 ).getDirection();
	}

	public Tool addTool( Tool tool ) {
		return addTool( tool, true );
	}

	public Tool addTool( Tool tool, boolean select ) {
		ToolView view = null;

		switch( tool.getPlacement() ) {
			case DEFAULT: {
				view = getDefaultView();
				break;
			}
			case ACTIVE: {
				view = getActiveView();
				break;
			}
			case LARGEST: {
				view = getLargestView();
				break;
			}
			case SMART: {
				view = getSmartView();
				break;
			}
		}

		return addTool( tool, view, select );
	}

	public Tool addTool( Tool tool, ToolView view ) {
		return addTool( tool, view, true );
	}

	public Tool addTool( Tool tool, ToolView view, boolean select ) {
		return addTool( tool, view, view.getTools().size(), select );
	}

	public Tool addTool( Tool tool, ToolView view, int index, boolean select ) {
		if( tool.getToolView() != null || getViews().contains( tool.getToolView() ) ) return tool;

		try {
			startOperation();
			view.addTool( tool, index );
			queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.TOOL_ADDED, this, view, tool ) );
			if( select ) setActiveTool( tool );
		} finally {
			finishOperation( true );
		}

		return tool;
	}

	public Tool removeTool( Tool tool ) {
		return removeTool( tool, true );
	}

	public Tool removeTool( Tool tool, boolean automerge ) {
		ToolView view = tool.getToolView();
		if( view == null ) return tool;

		try {
			startOperation();
			view.removeTool( tool );

			queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.TOOL_REMOVED, this, view, tool ) );

			// Try to auto merge the view.
			if( automerge ) pullMerge( view );
		} finally {
			finishOperation( true );
		}

		return tool;
	}

	public Tool closeTool( Tool tool ) {
		return closeTool( tool, true );
	}

	public Tool closeTool( Tool tool, boolean autoMerge ) {
		if( tool == null ) return null;

		startOperation();
		try {
			// Notify view listeners of attempt to close.
			try {
				tool.fireToolClosingEvent( new ToolEvent( this, ToolEvent.Type.TOOL_CLOSING, tool ) );
			} catch( ToolVetoException exception ) {
				return tool;
			}

			// Check the tool close operation.
			if( tool.getCloseOperation() == CloseOperation.NOTHING ) return tool;

			removeTool( tool, autoMerge );

			// Notify view listeners of view closure.
			tool.fireToolClosedEvent( new ToolEvent( this, ToolEvent.Type.TOOL_CLOSED, tool ) );
		} finally {
			finishOperation( true );
		}

		return tool;
	}

	@Override
	protected void layoutChildren() {
		Bounds bounds = getLayoutBounds();

		//System.out.println( "Layout pane: w=" + bounds.getWidth() + " h=" + bounds.getHeight() );

		ToolView maximizedView = getMaximizedView();
		if( maximizedView == null ) {
			for( Node node : getChildren() ) {
				if( node instanceof ToolView ) {
					layoutView( bounds, (ToolView)node );
				} else if( node instanceof Edge ) {
					layoutEdge( bounds, (Edge)node );
				}
			}
		} else {
			for( Node node : getChildren() ) {
				if( node == maximizedView ) {
					layoutMaximized( bounds, (ToolView)node );
				} else {
					node.setVisible( false );
				}
			}
		}
	}

	private void layoutView( Bounds bounds, ToolView view ) {
		if( bounds.getWidth() == 0 | bounds.getHeight() == 0 ) return;

		Insets insets = getInsets();
		bounds = new BoundingBox( 0, 0, bounds.getWidth() - insets.getLeft() - insets.getRight(), bounds.getHeight() - insets.getTop() - insets.getBottom() );

		double edgeSize = getEdgeSize();
		double edgeHalf = edgeSize / 2;
		double edgeRest = edgeSize - edgeHalf;

		double x1 = view.westEdge.getPosition();
		double y1 = view.northEdge.getPosition();
		double x2 = view.eastEdge.getPosition();
		double y2 = view.southEdge.getPosition();

		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;

		x = Math.floor( x1 * bounds.getWidth() );
		y = Math.floor( y1 * bounds.getHeight() );
		w = Math.floor( x2 * bounds.getWidth() ) - x;
		h = Math.floor( y2 * bounds.getHeight() ) - y;

		// Leave space for the edges.
		double north = 0;
		double south = 0;
		double east = 0;
		double west = 0;

		if( !view.northEdge.isWall() ) north = edgeRest;
		if( !view.southEdge.isWall() ) south = edgeHalf;
		if( !view.westEdge.isWall() ) west = edgeRest;
		if( !view.eastEdge.isWall() ) east = edgeHalf;

		x += west + insets.getLeft();
		y += north + insets.getTop();
		w -= (west + east);
		h -= (north + south);

		System.out.println( "Layout view: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		layoutInArea( view, x, y, w, h, 0, HPos.LEFT, VPos.TOP );
		view.setVisible( true );
	}

	private void layoutEdge( Bounds bounds, Edge edge ) {
		if( bounds.getWidth() == 0 | bounds.getHeight() == 0 ) return;

		Insets insets = getInsets();
		bounds = new BoundingBox( 0, 0, bounds.getWidth() - insets.getLeft() - insets.getRight(), bounds.getHeight() - insets.getTop() - insets.getBottom() );

		double edgeSize = edge.isWall() ? 0 : getEdgeSize();
		double edgeHalf = edgeSize / 2;
		double edgeRest = edgeSize - edgeHalf;
		double position = edge.getPosition();

		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;

		if( edge.getOrientation() == Orientation.VERTICAL ) {
			x = (int)Math.floor( position * bounds.getWidth() - edgeHalf );
			y = (int)Math.floor( edge.northEdge == null ? 0 : edge.northEdge.getPosition() * bounds.getHeight() );
			w = edgeSize;
			h = (int)Math.floor( edge.southEdge == null ? 1 : edge.southEdge.getPosition() * bounds.getHeight() ) - y;

			double north = edge.northEdge == null ? 0 : edge.northEdge.isWall() ? 0 : edgeRest;
			double south = edge.southEdge == null ? 0 : edge.southEdge.isWall() ? 0 : edgeHalf;

			y += north;
			h -= (north + south);
		} else {
			x = (int)Math.floor( edge.westEdge == null ? 0 : edge.westEdge.getPosition() * bounds.getWidth() );
			y = (int)Math.floor( position * bounds.getHeight() - edgeHalf );
			w = (int)Math.floor( edge.eastEdge == null ? 1 : edge.eastEdge.getPosition() * bounds.getWidth() ) - x;
			h = edgeSize;

			double west = edge.westEdge == null ? 0 : edge.westEdge.isWall() ? 0 : edgeRest;
			double east = edge.eastEdge == null ? 0 : edge.eastEdge.isWall() ? 0 : edgeHalf;

			x += west;
			w -= (west + east);
		}

		x += insets.getLeft();
		y += insets.getTop();

		//System.out.println( "Layout edge: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		layoutInArea( edge, x, y, w, h, 0, HPos.CENTER, VPos.CENTER );

		edge.setVisible( true );
	}

	private void layoutMaximized( Bounds bounds, ToolView view ) {
		Insets insets = getInsets();

		double x = insets.getLeft();
		double y = insets.getTop();
		double w = bounds.getWidth() - insets.getLeft() - insets.getRight();
		double h = bounds.getHeight() - insets.getTop() - insets.getBottom();

		//System.out.println( "Layout view max: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		layoutInArea( view, x, y, w, h, 0, HPos.CENTER, VPos.CENTER );
		view.setVisible( true );
	}

	/**
	 * Move the specified edge the specified offset in pixels.
	 *
	 * @param edge
	 * @param offset
	 * @return
	 */
	public double moveEdge( Edge edge, double offset ) {
		if( offset == 0 ) return 0;

		double result = 0;
		startOperation();
		try {
			switch( edge.getOrientation() ) {
				case HORIZONTAL: {
					result = moveVertical( edge, offset );
					break;
				}
				case VERTICAL: {
					result = moveHorizontal( edge, offset );
					break;
				}
			}
		} finally {
			finishOperation( result != 0 );
		}

		return result;
	}

	/**
	 * Move the edge vertically because its orientation is horizontal. This method
	 * may be called from other edges that need to move as part of the bump and
	 * slide effect.
	 */
	private double moveVertical( Edge edge, double offset ) {
		if( offset == 0 || edge.isWall() ) return 0;

		double delta = 0;

		// Check for room to move.
		if( offset < 0 ) {
			delta = checkMoveNorth( edge, offset );
		} else if( offset > 0 ) {
			delta = checkMoveSouth( edge, offset );
		}

		// Move the edge.
		Insets insets = getInsets();
		Bounds bounds = getBoundsInLocal();
		edge.setPosition( edge.getPosition() + (delta / (bounds.getHeight() - insets.getTop() - insets.getBottom())) );

		//		// Resize the north views.
		//		for( ToolView view : edge.northViews ) {
		//			if( view.westEdge != null && view.westEdge.southEdge == edge ) {
		//				view.westEdge.invalidate();
		//			}
		//			if( view.eastEdge != null && view.eastEdge.southEdge == edge ) {
		//				view.eastEdge.invalidate();
		//			}
		//			view.invalidate();
		//		}
		//
		//		// Resize the south views.
		//		for( ToolView view : edge.southViews ) {
		//			if( view.westEdge != null && view.westEdge.northEdge == edge ) {
		//				view.westEdge.invalidate();
		//			}
		//			if( view.eastEdge != null && view.eastEdge.northEdge == edge ) {
		//				view.eastEdge.invalidate();
		//			}
		//			view.invalidate();
		//		}
		//
		//		edge.invalidate();

		return delta;
	}

	/**
	 * Move the edge horizontally because its orientation is vertical. This method
	 * may be called from other edges that need to move as part of the bump and
	 * slide effect.
	 */
	private double moveHorizontal( Edge edge, double offset ) {
		if( offset == 0 || edge.isWall() ) return 0;

		double delta = offset;

		// Check for room to move.
		if( offset < 0 ) {
			delta = checkMoveWest( edge, offset );
		} else if( offset > 0 ) {
			delta = checkMoveEast( edge, offset );
		}

		// Move the edge.
		Insets insets = getInsets();
		Bounds bounds = getBoundsInLocal();
		edge.setPosition( edge.getPosition() + (delta / (bounds.getWidth() - insets.getLeft() - insets.getRight())) );

		//		// Resize the west views and edges.
		//		for( ToolView view : edge.westViews ) {
		//			if( view.northEdge != null && view.northEdge.eastEdge == edge ) {
		//				view.northEdge.invalidate();
		//			}
		//			if( view.southEdge != null && view.southEdge.eastEdge == edge ) {
		//				view.southEdge.invalidate();
		//			}
		//			view.invalidate();
		//		}
		//
		//		// Resize the east views and edges.
		//		for( ToolView view : edge.eastViews ) {
		//			if( view.northEdge != null && view.northEdge.westEdge == edge ) {
		//				view.northEdge.invalidate();
		//			}
		//			if( view.southEdge != null && view.southEdge.westEdge == edge ) {
		//				view.southEdge.invalidate();
		//			}
		//			view.invalidate();
		//		}
		//
		//		edge.invalidate();

		return delta;
	}

	private double checkMoveNorth( Edge edge, double offset ) {
		double delta = offset;
		//Dimension viewSize = null;
		Edge blockingEdge = null;

		// Check the north views.
		for( ToolView view : edge.northViews ) {
			double height = view.getHeight();
			if( height < -delta ) {
				blockingEdge = view.northEdge;
				delta = -height;
			}
		}

		// If could move not the entire distance, try and move the next edge over.
		if( offset - delta < 0 ) {
			if( !blockingEdge.isWall() ) {
				double result = moveVertical( blockingEdge, offset - delta );
				delta += result;
			}
		}

		return delta;
	}

	private double checkMoveSouth( Edge edge, double offset ) {
		double delta = offset;
		Edge blockingEdge = null;

		// Check the south views.
		for( ToolView view : edge.southViews ) {
			double height = view.getHeight();
			if( height < delta ) {
				blockingEdge = view.southEdge;
				delta = height;
			}
		}

		// If could move not the entire distance, try and move the next edge over.
		if( offset - delta > 0 ) {
			if( !blockingEdge.isWall() ) {
				double result = moveVertical( blockingEdge, offset - delta );
				delta += result;
			}
		}

		return delta;
	}

	private double checkMoveWest( Edge edge, double offset ) {
		double delta = offset;
		Edge blockingEdge = null;

		// Check the west views.
		for( ToolView view : edge.westViews ) {
			double width = view.getWidth();
			if( width < -delta ) {
				blockingEdge = view.westEdge;
				delta = -width;
			}
		}

		// If could move not the entire distance, try and move the next edge over.
		if( offset - delta < 0 ) {
			if( !blockingEdge.isWall() ) {
				double result = moveHorizontal( blockingEdge, offset - delta );
				delta += result;
			}
		}

		return delta;
	}

	private double checkMoveEast( Edge edge, double offset ) {
		double delta = offset;
		Edge blockingEdge = null;

		// Check the east views.
		for( ToolView view : edge.eastViews ) {
			double width = view.getWidth();
			if( width < delta ) {
				blockingEdge = view.eastEdge;
				delta = width;
			}
		}

		// If could move not the entire distance, try and move the next edge over.
		if( offset - delta > 0 ) {
			if( !blockingEdge.isWall() ) {
				double result = moveHorizontal( blockingEdge, offset - delta );
				delta += result;
			}
		}

		return delta;
	}

	private boolean merge( Edge edge, Side direction ) {
		if( !canMerge( edge, direction, false ) ) return false;

		Set<ToolView> sources = edge.getViews( getReverseDirection( direction ) );
		Set<ToolView> targets = edge.getViews( direction );

		// Notify the listeners the views will merge.
		try {
			for( ToolView source : sources ) {
				fireViewWillMerge( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_WILL_MERGE, this, source, null ) );
			}
		} catch( WorkpaneVetoException exception ) {
			return false;
		}

		// Get needed objects.
		Edge farEdge = targets.iterator().next().getEdge( direction );

		// Extend the source views and edges.
		for( ToolView source : sources ) {
			source.setEdge( direction, farEdge );

			if( source.getEdge( getLeftDirection( direction ) ).getEdge( direction ) == edge ) {
				source.getEdge( getLeftDirection( direction ) ).setEdge( direction, farEdge );
			}
			if( source.getEdge( getRightDirection( direction ) ).getEdge( direction ) == edge ) {
				source.getEdge( getRightDirection( direction ) ).setEdge( direction, farEdge );
			}
			farEdge.getViews( getReverseDirection( direction ) ).add( source );
		}

		// Process the target views and edges.
		for( ToolView target : targets ) {
			ToolView closestSource = getClosest( sources, target, getPerpendicularDirectionOrientation( direction ) );

			// Check for default view.
			if( target.isDefault() ) setDefaultView( closestSource );

			// Check for active view.
			if( target.isActive() ) setActiveView( closestSource );

			// Check for tools.
			for( Tool tool : target.getTools() ) {
				removeTool( tool, false );
				addTool( tool, closestSource );
			}

			// Clean up target edges.
			cleanupTargetEdge( target, direction );
			cleanupTargetEdge( target, getReverseDirection( direction ) );
			cleanupTargetEdge( target, getLeftDirection( direction ) );
			cleanupTargetEdge( target, getRightDirection( direction ) );

			// Remove the target view.
			removeView( target );
		}

		// Remove the edge.
		edge.setEdge( direction, null );
		edge.setEdge( getReverseDirection( direction ), null );
		edge.setEdge( getLeftDirection( direction ), null );
		edge.setEdge( getRightDirection( direction ), null );
		edge.getWorkpane().removeEdge( edge );

		return true;
	}

	private ToolView getClosest( Set<ToolView> views, ToolView target, Orientation orientation ) {
		ToolView result = null;
		double distance = Double.MAX_VALUE;
		double resultDistance = Double.MAX_VALUE;
		double targetCenter = target.getCenter( orientation );

		for( ToolView view : views ) {
			distance = Math.abs( targetCenter - view.getCenter( orientation ) );
			if( distance < resultDistance ) {
				result = view;
				resultDistance = distance;
			}
		}

		return result;
	}

	private void cleanupTargetEdge( ToolView target, Side direction ) {
		Edge edge = target.getEdge( direction );

		// Remove the target from the edge.
		edge.getViews( getReverseDirection( direction ) ).remove( target );

		// If there are no more associated views, remove the edge.
		if( !edge.isWall() && edge.getViews( direction ).size() == 0 && edge.getViews( getReverseDirection( direction ) ).size() == 0 ) removeEdge( edge );
	}

	private class EdgeSkin extends SkinBase<Edge> {

		/**
		 * Constructor for all SkinBase instances.
		 *
		 * @param control The control for which this Skin should attach to.
		 */
		protected EdgeSkin( Edge control ) {
			super( control );
		}

	}

	public class Edge extends Control {

		Orientation orientation;

		Edge northEdge;

		Edge southEdge;

		Edge westEdge;

		Edge eastEdge;

		Set<ToolView> northViews;

		Set<ToolView> southViews;

		Set<ToolView> westViews;

		Set<ToolView> eastViews;

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

		private Set<ToolView> viewsA;

		private Set<ToolView> viewsB;

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
			viewsA = new CopyOnWriteArraySet<ToolView>();
			viewsB = new CopyOnWriteArraySet<ToolView>();
			northViews = viewsA;
			southViews = viewsB;
			westViews = viewsA;
			eastViews = viewsB;

			// Set the control cursor
			setCursor( orientation == Orientation.VERTICAL ? Cursor.V_RESIZE : Cursor.H_RESIZE );

			// Set the default background
			setBackground( new Background( new BackgroundFill( Color.RED, CornerRadii.EMPTY, Insets.EMPTY ) ) );

			// Register the mouse handlers
			onMousePressedProperty().set( this::mousePressed );
			onMouseReleasedProperty().set( this::mouseReleased );
			onMouseDraggedProperty().set( this::mouseDragged );
		}

		@Override
		protected Skin<Edge> createDefaultSkin() {
			return new EdgeSkin( this );
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

		public Set<ToolView> getViews( Side direction ) {
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder( getClass().getSimpleName() );

			builder.append( " orientation=" );
			builder.append( getOrientation() );
			builder.append( " position=" );
			builder.append( getPosition() );
			builder.append( " wall=" );
			builder.append( isWall() );

			return builder.toString();
		}

	}

	private static class MergeDirection implements Comparable<MergeDirection> {

		Side direction;

		int weight;

		public MergeDirection( ToolView target, Side direction ) {
			this.direction = direction;
			this.weight = getMergeWeight( target, direction );
			log.trace( "Direction: " + direction + "  Weight: " + weight );
		}

		public Side getDirection() {
			return direction;
		}

		public int getWeight() {
			return weight;
		}

		@Override
		public int compareTo( MergeDirection that ) {
			return this.getCompareValue() - that.getCompareValue();
		}

		private int getCompareValue() {
			return weight == Integer.MAX_VALUE ? weight : weight + getDirectionValue( direction );
		}

		private int getDirectionValue( Side side ) {
			switch( side ) {
				case TOP: {
					return 1;
				}
				case BOTTOM: {
					return 2;
				}
				case LEFT: {
					return 3;
				}
				case RIGHT: {
					return 4;
				}
			}

			return 0;
		}

		private int getMergeWeight( ToolView target, Side side ) {
			Edge edge = null;
			Set<ToolView> sourceViews = null;
			Set<ToolView> targetViews = null;

			switch( side ) {
				case TOP: {
					edge = target.northEdge;
					sourceViews = edge.southViews;
					targetViews = edge.northViews;
					break;
				}
				case BOTTOM: {
					edge = target.southEdge;
					sourceViews = edge.northViews;
					targetViews = edge.southViews;
					break;
				}
				case LEFT: {
					edge = target.westEdge;
					sourceViews = edge.eastViews;
					targetViews = edge.westViews;
					break;
				}
				case RIGHT: {
					edge = target.eastEdge;
					sourceViews = edge.westViews;
					targetViews = edge.eastViews;
					break;
				}
			}

			int result = 10 * (sourceViews.size() + targetViews.size() - 1);
			if( edge.isWall() ) result = Integer.MAX_VALUE;

			return result;
		}

	}

}
