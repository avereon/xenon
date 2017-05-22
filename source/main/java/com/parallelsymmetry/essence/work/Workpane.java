package com.parallelsymmetry.essence.work;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class Workpane extends Pane {

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
		setDefaultView( view );
		setActiveView( view );

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

		// Add the view to the workpane
		getChildren().add( view );
	}

	/**
	 * Returns an unmodifiable list of the views.
	 *
	 * @return
	 */
	public Set<Edge> getEdges() {
		Set<Edge> edges = new HashSet<>();
		for( Node node : getChildren() ) {
			if( node instanceof Edge ) edges.add( (Edge)node );
		}
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
		return getActiveView().getActiveTool();
	}

	public void setActiveTool( Tool tool ) {
		if( tool != null ) {
			ToolView view = tool.getToolView();
			if( view == null || !getViews().contains( view ) ) return;
		}

		doSetActiveTool( tool, true );
	}

	ToolView getActiveView() {
		return activeViewProperty.get();
	}

	void setActiveView( ToolView view ) {
		activeViewProperty.set( view );
	}

	ObjectProperty<ToolView> activeViewProperty() {
		return activeViewProperty;
	}

	ToolView getDefaultView() {
		return defaultViewProperty.get();
	}

	void setDefaultView( ToolView view ) {
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
		maximizedViewProperty.set( view );
		updateComponentTree( true );
	}

	ObjectProperty<ToolView> maximizedViewProperty() {
		return maximizedViewProperty;
	}

	public Tool getActiveWorktool() {
		return activeWorktoolProperty.get();
	}

	public void setActiveWorktool( Tool worktool ) {
		activeWorktoolProperty.set( worktool );
	}

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

	private void doSetActiveTool( Tool tool, boolean setView ) {
		startOperation();
		try {
			ToolView view = tool == null ? null : tool.getToolView();
			Tool activeTool = getActiveTool();

			if( activeTool != null ) {
				activeTool.callDeactivate();
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.TOOL_DEACTIVATED, this, activeTool.getToolView(), activeTool ) );
			}

			// Change the active tool.
			setActiveTool( tool );

			if( view != null ) {
				view.setActiveTool( tool );
				if( setView && view != getActiveView() ) doSetActiveView( view, false );
			}

			activeTool = getActiveTool();
			if( activeTool != null ) {
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.TOOL_ACTIVATED, this, activeTool.getToolView(), activeTool ) );
				activeTool.callActivate();
			}
		} finally {
			finishOperation( true );
		}
	}

	private void doSetActiveView( ToolView view, boolean setTool ) {
		startOperation();
		try {
			ToolView activeToolView = getActiveView();

			if( activeToolView != null ) {
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_DEACTIVATED, this, activeToolView, null ) );
			}

			// Change the active view.
			setActiveView( view );

			if( setTool ) doSetActiveTool( view.getActiveTool(), false );

			// Handle the new active view.
			activeToolView = getActiveView();
			if( activeToolView != null ) {
				queueEvent( new WorkpaneEvent( this, WorkpaneEvent.Type.VIEW_ACTIVATED, this, activeToolView, null ) );
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

	public boolean canSplit( ToolView target, Side direction ) {
		if( target == null ) return false;
		return getMaximizedView() == null;
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

	// NEXT Continue implementing Workpane class methods

	@Override
	protected void layoutChildren() {
		Bounds bounds = getLayoutBounds();
	}

	private double moveEdge( Edge edge, double delta ) {
		// NEXT Implement Workpane.moveEdge()
		return 0;
	}

	private boolean merge( Edge edge, Side side ) {
		// NEXT Implement Workpane.merge()
		return false;
	}

	private class Edge extends Control {

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

	}

	/**
	 * ToolView is not intended to be a public class.
	 */
	public class ToolView extends Pane {

		Edge northEdge;

		Edge southEdge;

		Edge westEdge;

		Edge eastEdge;

		private TabPane tools;

		private Workpane parent;

		private Tool activeTool;

		public ToolView() {
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
			Tool next = null;
			boolean isActiveTool = tool == activeTool;

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
