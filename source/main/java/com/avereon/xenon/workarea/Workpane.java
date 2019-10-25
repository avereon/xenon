package com.avereon.xenon.workarea;

import com.avereon.settings.Settings;
import com.avereon.util.Configurable;
import com.avereon.util.IdGenerator;
import com.avereon.util.LogUtil;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.UiFactory;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Workpane extends Control implements Configurable {

	public enum Placement {
		DEFAULT,
		ACTIVE,
		LARGEST,
		SMART,
		DOCK_TOP,
		DOCK_LEFT,
		DOCK_RIGHT,
		DOCK_BOTTOM
	}

	public enum DockMode {
		LANDSCAPE,
		PORTRAIT
	}

	static final DockMode DEFAULT_DOCK_MODE = DockMode.LANDSCAPE;

	static final double DEFAULT_VIEW_SPLIT_RATIO = 0.20;

	static final double DEFAULT_WALL_SPLIT_RATIO = 0.20;

	private static final double DEFAULT_EDGE_SIZE = 5;

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private WorkpaneEdge topWall;

	private WorkpaneEdge leftWall;

	private WorkpaneEdge rightWall;

	private WorkpaneEdge bottomWall;

	private WorkpaneDropHint dragHint;

	private DoubleProperty edgeSize;

	private WorkpaneLayout layout;

	private ObjectProperty<WorkpaneView> activeViewProperty;

	private ObjectProperty<WorkpaneView> defaultViewProperty;

	private ObjectProperty<WorkpaneView> maximizedViewProperty;

	private ObjectProperty<Tool> activeToolProperty;

	private ObjectProperty<DockMode> dockModeProperty;

	private DoubleProperty topDockSize;

	private DoubleProperty leftDockSize;

	private DoubleProperty rightDockSize;

	private DoubleProperty bottomDockSize;

	private AtomicInteger operation;

	private Queue<WorkpaneEvent> events;

	private Collection<WorkpaneListener> listeners;

	@Deprecated
	private Settings settings;

	public Workpane() {
		layout = new WorkpaneLayout( this );

		getStyleClass().add( "workpane" );

		edgeSize = new SimpleDoubleProperty( DEFAULT_EDGE_SIZE );
		activeViewProperty = new SimpleObjectProperty<>();
		defaultViewProperty = new SimpleObjectProperty<>();
		maximizedViewProperty = new SimpleObjectProperty<>();
		activeToolProperty = new SimpleObjectProperty<>();
		dockModeProperty = new SimpleObjectProperty<>();

		topDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );
		leftDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );
		rightDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );
		bottomDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );

		//leftDockSize.addListener( (observable, oldValue, newValue) -> System.out.println( "New left dock size: " + newValue ) );

		operation = new AtomicInteger();
		events = new LinkedList<>();
		listeners = new CopyOnWriteArraySet<>();

		// Create the wall edges
		topWall = new WorkpaneEdge( Orientation.HORIZONTAL, Side.TOP );
		leftWall = new WorkpaneEdge( Orientation.VERTICAL, Side.LEFT );
		rightWall = new WorkpaneEdge( Orientation.VERTICAL, Side.RIGHT );
		bottomWall = new WorkpaneEdge( Orientation.HORIZONTAL, Side.BOTTOM );

		// Set the workpane on the edges
		topWall.setWorkpane( this );
		leftWall.setWorkpane( this );
		rightWall.setWorkpane( this );
		bottomWall.setWorkpane( this );

		// Set the edge positions
		topWall.setPosition( 0 );
		leftWall.setPosition( 0 );
		rightWall.setPosition( 1 );
		bottomWall.setPosition( 1 );

		// Add the edges to the workpane
		getChildren().add( topWall );
		getChildren().add( leftWall );
		getChildren().add( rightWall );
		getChildren().add( bottomWall );

		// Create the initial view
		WorkpaneView view = new WorkpaneView();

		// Add the view to the wall edges
		topWall.bottomViews.add( view );
		leftWall.rightViews.add( view );
		rightWall.leftViews.add( view );
		bottomWall.topViews.add( view );

		// Set the edges on the view
		view.setEdge( Side.TOP, topWall );
		view.setEdge( Side.LEFT, leftWall );
		view.setEdge( Side.RIGHT, rightWall );
		view.setEdge( Side.BOTTOM, bottomWall );

		// Add the initial view
		addView( view );
		setActiveView( view );
		setDefaultView( view );

		setDockMode( DEFAULT_DOCK_MODE );

		// TODO Set a better default background
		setBackground( new Background( new BackgroundFill( new Color( 0.2, 0.2, 0.2, 1.0 ), CornerRadii.EMPTY, Insets.EMPTY ) ) );
	}

	/**
	 * Returns an unmodifiable list of the edges.
	 *
	 * @return
	 */
	public Set<WorkpaneEdge> getEdges() {
		Set<WorkpaneEdge> edges = new HashSet<>();

		// Count the edges that are not walls
		getChildren().filtered( ( c ) -> c instanceof WorkpaneEdge ).forEach( ( c ) -> edges.add( (WorkpaneEdge)c ) );
		edges.remove( topWall );
		edges.remove( bottomWall );
		edges.remove( leftWall );
		edges.remove( rightWall );

		return Collections.unmodifiableSet( edges );
	}

	/**
	 * Returns an unmodifiable list of the views.
	 *
	 * @return
	 */
	public Set<WorkpaneView> getViews() {
		Set<WorkpaneView> views = new HashSet<>();
		for( Node node : getChildren() ) {
			if( node instanceof WorkpaneView ) views.add( (WorkpaneView)node );
		}
		return Collections.unmodifiableSet( views );
	}

	/**
	 * Get an unmodifiable set of the tools.
	 *
	 * @return An unmodifiable set of the tools.
	 */
	public Set<Tool> getTools() {
		Set<Tool> tools = new HashSet<>();
		getViews().forEach( ( view ) -> tools.addAll( view.getTools() ) );
		return Collections.unmodifiableSet( tools );
	}

	public Set<Tool> getTools( Class<? extends Tool> type ) {
		return getTools().stream().filter( type::isInstance ).collect( Collectors.toUnmodifiableSet() );
	}

	public boolean hasTool( Class<? extends Tool> type ) {
		return getTools( type ).size() > 0;
	}

	public double getEdgeSize() {
		return edgeSize.get();
	}

	public void setEdgeSize( double size ) {
		edgeSize.set( size );
		updateComponentTree( true );
	}

	public DoubleProperty edgeSize() {
		return edgeSize;
	}

	public Tool getActiveTool() {
		return activeToolProperty.get();
	}

	public void setActiveTool( Tool tool ) {
		doSetActiveTool( tool, true );
	}

	public ReadOnlyObjectProperty<Tool> activeToolProperty() {
		return activeToolProperty;
	}

	public WorkpaneView getActiveView() {
		return activeViewProperty.get();
	}

	public void setActiveView( WorkpaneView view ) {
		doSetActiveView( view, true );
	}

	public ReadOnlyObjectProperty<WorkpaneView> activeViewProperty() {
		return activeViewProperty;
	}

	public WorkpaneView getDefaultView() {
		return defaultViewProperty.get();
	}

	public void setDefaultView( WorkpaneView view ) {
		WorkpaneView defaultView = getDefaultView();
		if( defaultView == view ) return;

		if( defaultView != null ) {
			if( defaultView.getSettings() != null ) defaultView.getSettings().set( "default", null );
		}

		defaultViewProperty.set( view );
		defaultView = view;

		if( defaultView != null ) {
			if( defaultView.getSettings() != null ) defaultView.getSettings().set( "default", true );
		}

		updateComponentTree( true );
	}

	public ReadOnlyObjectProperty<WorkpaneView> defaultViewProperty() {
		return defaultViewProperty;
	}

	public WorkpaneView getMaximizedView() {
		return maximizedViewProperty.get();
	}

	public void setMaximizedView( WorkpaneView view ) {
		WorkpaneView maximizedView = getMaximizedView();
		if( maximizedView == view ) return;

		if( maximizedView != null ) {
			if( maximizedView.getSettings() != null ) maximizedView.getSettings().set( "maximized", null );
		}

		maximizedViewProperty.set( view );
		maximizedView = view;

		if( maximizedView != null ) {
			if( maximizedView.getSettings() != null ) maximizedView.getSettings().set( "maximized", true );
		}

		updateComponentTree( true );
	}

	public ReadOnlyObjectProperty<WorkpaneView> maximizedViewProperty() {
		return maximizedViewProperty;
	}

	public WorkpaneView getLargestView() {
		WorkpaneView view = null;

		for( WorkpaneView testView : getViews() ) {
			if( compareViewArea( testView, view ) > 0 ) view = testView;
		}

		return view;
	}

	/**
	 * Find a view with the following rules: <ol> <li>Use a single large view (has double the area of any other view)</li> <li>Use the active view</li> <li>Use
	 * the default view</li> </ol>
	 *
	 * @return
	 */
	public WorkpaneView getSmartView() {
		// Collect the view areas
		int index = 0;
		double maxArea = 0;
		WorkpaneView largest = null;
		double[] areas = new double[ getViews().size() ];
		for( WorkpaneView view : getViews() ) {
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
		WorkpaneView view = count == 1 ? largest : getActiveView();

		// If there was not definite large view and no active view just use the default view
		return view != null ? view : getDefaultView();
	}

	public DockMode getDockMode() {
		return dockModeProperty.get();
	}

	public void setDockMode( DockMode mode ) {
		if( getDockMode() == mode ) return;

		// TODO Rearrange the dock views according to the new mode

		dockModeProperty.set( mode );
	}

	public ReadOnlyObjectProperty<DockMode> dockModeProperty() {
		return dockModeProperty;
	}

	public double getTopDockSize() {
		return topDockSize.get();
	}

	public DoubleProperty topDockSizeProperty() {
		return topDockSize;
	}

	public void setTopDockSize( double topDockSize ) {
		this.topDockSize.set( topDockSize );
	}

	public double getLeftDockSize() {
		return leftDockSize.get();
	}

	public DoubleProperty leftDockSizeProperty() {
		return leftDockSize;
	}

	public void setLeftDockSize( double leftDockSize ) {
		this.leftDockSize.set( leftDockSize );
	}

	public double getRightDockSize() {
		return rightDockSize.get();
	}

	public DoubleProperty rightDockSizeProperty() {
		return rightDockSize;
	}

	public void setRightDockSize( double rightDockSize ) {
		this.rightDockSize.set( rightDockSize );
	}

	public double getBottomDockSize() {
		return bottomDockSize.get();
	}

	public DoubleProperty bottomDockSizeProperty() {
		return bottomDockSize;
	}

	public void setBottomDockSize( double bottomDockSize ) {
		this.bottomDockSize.set( bottomDockSize );
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

	public void addWorkpaneListener( WorkpaneListener listener ) {
		listeners.add( listener );
	}

	public void removeWorkpaneListener( WorkpaneListener listener ) {
		listeners.remove( listener );
	}

	@Override
	protected double computeMinWidth( double height ) {
		return getInsets().getLeft() + getInsets().getRight();
	}

	@Override
	protected double computeMinHeight( double width ) {
		return getInsets().getTop() + getInsets().getBottom();
	}

	@Override
	protected double computeMaxWidth( double height ) {
		return Double.MAX_VALUE;
	}

	@Override
	protected double computeMaxHeight( double width ) {
		return Double.MAX_VALUE;
	}

	void setDropHint( WorkpaneDropHint hint ) {
		if( dragHint != null ) getChildren().remove( dragHint );
		if( hint != null ) getChildren().add( dragHint = hint );
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new WorkpaneSkin( this );
	}

	private boolean isOperationActive() {
		return operation.get() > 0;
	}

	private void startOperation() {
		operation.incrementAndGet();
	}

	private void finishOperation( boolean changed ) {
		int value = operation.decrementAndGet();
		if( value < 0 ) log.error( "Workpane operation flag is less than zero." );
		updateComponentTree( changed );
	}

	private void fireWorkpaneEvent( WorkpaneEvent event ) throws WorkpaneVetoException {
		WorkpaneVetoException exception = null;

		for( WorkpaneListener listener : listeners ) {
			try {
				listener.handle( event );
			} catch( WorkpaneVetoException vetoException ) {
				if( exception == null ) exception = vetoException;
			}
		}

		if( exception != null ) throw exception;
	}

	void queueEvent( WorkpaneEvent data ) {
		if( !isOperationActive() ) throw new RuntimeException( "Event should only be queued during active operations: " + data.getType() );
		events.offer( data );
	}

	private void updateComponentTree( boolean changed ) {
		if( isOperationActive() ) return;

		if( changed ) events.offer( new WorkpaneEvent( this, WorkpaneEvent.Type.CHANGED, this ) );

		layoutChildren();
		dispatchEvents();
	}

	private void dispatchEvents() {
		for( WorkpaneEvent event : new LinkedList<>( events ) ) {
			events.remove( event );
			for( WorkpaneListener listener : listeners ) {
				try {
					listener.handle( event );
				} catch( WorkpaneVetoException exception ) {
					log.error( "Error dispatching workpane event", exception );
				}
			}
		}
	}

	private void doSetActiveView( WorkpaneView view, boolean setTool ) {
		if( view != null && (view == getActiveView() || !getViews().contains( view )) ) return;

		startOperation();
		try {
			WorkpaneView activeToolView = getActiveView();
			if( activeToolView != null ) {
				activeToolView.setActive( false );
				if( activeToolView.getSettings() != null ) activeToolView.getSettings().set( "active", null );
				queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_DEACTIVATED, this, activeToolView ) );
			}

			// Change the active view
			activeViewProperty.set( view );

			// Handle the new active view
			activeToolView = getActiveView();
			if( activeToolView != null ) {
				activeToolView.setActive( true );
				if( activeToolView.getSettings() != null ) activeToolView.getSettings().set( "active", true );
				if( setTool ) doSetActiveTool( activeToolView.getActiveTool(), false );
				queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_ACTIVATED, this, activeToolView ) );
			}
		} finally {
			finishOperation( true );
		}
	}

	private void doSetActiveTool( Tool tool, boolean setView ) {
		// Make sure the tool is contained by this workpane
		if( tool != null && tool.getWorkpane() != this ) return;

		Tool activeTool;
		startOperation();
		try {
			activeTool = getActiveTool();
			if( activeTool != null ) {
				activeTool.callDeactivate();
				if( activeTool.getSettings() != null ) activeTool.getSettings().set( "active", null );
			}

			// Change the active view
			WorkpaneView view = tool == null ? null : tool.getToolView();
			if( view != null && getViews().contains( view ) ) {
				view.setActiveTool( tool );
				if( setView && view != getActiveView() ) doSetActiveView( view, false );
			}

			// Change the active tool
			activeToolProperty.set( tool );
			if( view != null ) view.setActiveTool( tool );

			activeTool = getActiveTool();
			if( activeTool != null ) {
				activeTool.callActivate();
				if( activeTool.getSettings() != null ) activeTool.getSettings().set( "active", true );
			}
		} finally {
			finishOperation( true );
		}
	}

	private double compareViewArea( WorkpaneView view1, WorkpaneView view2 ) {
		Bounds size1 = view1.getBoundsInLocal();
		Bounds size2 = view2.getBoundsInLocal();
		double area1 = size1.getWidth() * size1.getHeight();
		double area2 = size2.getWidth() * size2.getHeight();
		return area1 - area2;
	}

	/**
	 * For use when restoring the state of the workpane.
	 */
	public void restoreNodes( Set<Node> nodes ) {
		startOperation();
		try {
			// Remove existing views
			for( WorkpaneView view : getViews() ) {
				removeView( view );
			}

			// Add edges and views
			for( Node node : nodes ) {
				if( node instanceof WorkpaneEdge ) {
					addEdge( (WorkpaneEdge)node );
				} else if( node instanceof WorkpaneView ) {
					WorkpaneView view = (WorkpaneView)node;

					addView( view );

					Settings settings = view.getSettings();
					if( settings != null ) {
						boolean isActive = settings.get( "active", Boolean.class, false );
						boolean isDefault = settings.get( "default", Boolean.class, false );
						boolean isMaximized = settings.get( "maximized", Boolean.class, false );

						if( isActive ) setActiveView( view );
						if( isDefault ) setDefaultView( view );
						if( isMaximized ) setMaximizedView( view );
					}
				}
			}
		} finally {
			finishOperation( false );
		}
	}

	private WorkpaneView addView( WorkpaneView view ) {
		if( view == null ) return null;

		startOperation();
		try {
			view.setWorkpane( this );
			getChildren().add( view );
			queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_ADDED, this, view ) );
		} finally {
			finishOperation( true );
		}

		return view;
	}

	private WorkpaneView removeView( WorkpaneView view ) {
		if( view == null ) return null;

		try {
			startOperation();

			if( view.getSettings() != null ) view.getSettings().delete();
			view.setSettings( null );

			getChildren().remove( view );
			view.setWorkpane( null );

			view.getEdge( Side.TOP ).bottomViews.remove( view );
			view.getEdge( Side.BOTTOM ).topViews.remove( view );
			view.getEdge( Side.LEFT ).rightViews.remove( view );
			view.getEdge( Side.RIGHT ).leftViews.remove( view );
			view.setEdge( Side.TOP, null );
			view.setEdge( Side.BOTTOM, null );
			view.setEdge( Side.LEFT, null );
			view.setEdge( Side.RIGHT, null );

			queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_REMOVED, this, view ) );
		} finally {
			finishOperation( true );
		}

		return view;
	}

	public WorkpaneEdge getWallEdge( Side direction ) {
		switch( direction ) {
			case TOP: {
				return topWall;
			}
			case BOTTOM: {
				return bottomWall;
			}
			case LEFT: {
				return leftWall;
			}
			case RIGHT: {
				return rightWall;
			}
		}

		return null;
	}

	Side getWallEdgeSide( WorkpaneEdge edge ) {
		if( !edge.isWall() ) return null;
		if( edge == getWallEdge( Side.TOP ) ) return Side.TOP;
		if( edge == getWallEdge( Side.LEFT ) ) return Side.LEFT;
		if( edge == getWallEdge( Side.RIGHT ) ) return Side.RIGHT;
		if( edge == getWallEdge( Side.BOTTOM ) ) return Side.BOTTOM;
		return null;
	}

	private WorkpaneEdge addEdge( WorkpaneEdge edge ) {
		if( edge == null ) return null;

		edge.setWorkpane( this );
		getChildren().add( edge );
		queueEvent( new WorkpaneEdgeEvent( this, WorkpaneEvent.Type.EDGE_ADDED, this, edge, edge.getPosition() ) );
		edge
			.positionProperty()
			.addListener( ( observable, oldValue, newValue ) -> queueEvent( new WorkpaneEdgeEvent( this,
				WorkpaneEvent.Type.EDGE_MOVED,
				this,
				edge,
				newValue.doubleValue()
			) ) );

		return edge;
	}

	private WorkpaneEdge removeEdge( WorkpaneEdge edge ) {
		if( edge == null ) return null;

		if( edge.getSettings() != null ) edge.getSettings().delete();
		edge.setSettings( null );

		getChildren().remove( edge );
		edge.setWorkpane( null );

		queueEvent( new WorkpaneEdgeEvent( this, WorkpaneEvent.Type.EDGE_REMOVED, this, edge, edge.getPosition() ) );

		return edge;
	}

	/**
	 * Move the specified edge the specified offset in pixels.
	 *
	 * @param edge
	 * @param offset
	 * @return
	 */
	double moveEdge( WorkpaneEdge edge, double offset ) {
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

	public boolean canSplit( WorkpaneView target, Side direction ) {
		return target != null && getMaximizedView() == null;
	}

	/**
	 * Split the workpane using the space in the specified direction to make a new tool view along the entire edge of the workpane.
	 *
	 * @param direction
	 * @return
	 */
	public WorkpaneView split( Side direction ) {
		return split( direction, DEFAULT_WALL_SPLIT_RATIO );
	}

	/**
	 * Split the workpane using the space in the specified direction to make a new tool view along the entire edge of the workpane. The new tool view is created
	 * using the specified percentage of the original space.
	 *
	 * @param direction
	 * @param percent
	 * @return
	 */
	public WorkpaneView split( Side direction, double percent ) {
		WorkpaneView result = null;
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

			queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_SPLIT, this, null ) );
		} finally {
			finishOperation( true );
		}

		return result;
	}

	/**
	 * Split an existing tool view using the space in the specified direction to create a new tool view.
	 *
	 * @param view
	 * @param direction
	 * @return
	 */
	public WorkpaneView split( WorkpaneView view, Side direction ) {
		return split( view, direction, DEFAULT_VIEW_SPLIT_RATIO );
	}

	/**
	 * Split an existing tool view using the space in the specified direction to create a new tool view. The new tool view is created using the specified
	 * percentage of the original space.
	 *
	 * @param view
	 * @param direction
	 * @param percent
	 * @return
	 */
	public WorkpaneView split( WorkpaneView view, Side direction, double percent ) {
		WorkpaneView result = null;
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
			queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_SPLIT, this, view ) );
		} finally {
			finishOperation( true );
		}

		return result;
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

		return Side.TOP;
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

		return Side.TOP;
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

		return Side.TOP;
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
	 * Performs an automatic pull merge. The direction is automatically determined by a weighted algorithm.
	 *
	 * @param target
	 * @return If the merge was successful
	 */
	public boolean pullMerge( WorkpaneView target ) {
		Side direction = getPullMergeDirection( target, true );
		return direction != null && pullMerge( target, direction );
	}

	/**
	 * Performs a pull merge in the specified direction.
	 *
	 * @param target
	 * @param direction
	 * @return If the merge was successful
	 */
	public boolean pullMerge( WorkpaneView target, Side direction ) {
		// Check the parameters.
		if( target == null ) return false;

		boolean result = false;
		try {
			startOperation();
			result = merge( target.getEdge( getReverseDirection( direction ) ), direction );
			if( result ) queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_MERGED, this, target ) );
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
	 * @return If the merge was successful
	 */
	public boolean pushMerge( WorkpaneView source, Side direction ) {
		// Check the parameters.
		if( source == null ) return false;

		boolean result = false;
		try {
			startOperation();
			result = merge( source.getEdge( direction ), direction );
			if( result ) queueEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_MERGED, this, source ) );
		} finally {
			finishOperation( result );
		}

		return result;
	}

	public boolean canPushMerge( WorkpaneView source, Side direction, boolean auto ) {
		return canMerge( source.getEdge( direction ), direction, auto );
	}

	public boolean canPullMerge( WorkpaneView target, Side direction, boolean auto ) {
		return canMerge( target.getEdge( getReverseDirection( direction ) ), direction, auto );
	}

	/**
	 * Returns whether views on the source (opposite of direction) side of the edge can be merged into the space occupied by the views on the target (towards
	 * direction) side of the edge. The method returns false if any of the following
	 * conditions exist: <ul> <li>If the edge is an end edge.</li> <li>If any of the target views is the default view.</li> <li>If the target views do not share a
	 * common back edge.</li> <li>If the auto flag is set to true and any of the
	 * target views have tools. </li> </ul>
	 *
	 * @param edge The edge across which views are to be merged.
	 * @param direction The direction of the merge.
	 * @param auto Check if views can automatically be merged.
	 * @return
	 */
	private boolean canMerge( WorkpaneEdge edge, Side direction, boolean auto ) {
		if( edge == null ) return false;

		// Check for end edge.
		if( edge.isWall() ) return false;

		Set<WorkpaneView> targets = null;
		switch( direction ) {
			case TOP: {
				targets = edge.topViews;
				break;
			}
			case BOTTOM: {
				targets = edge.bottomViews;
				break;
			}
			case LEFT: {
				targets = edge.leftViews;
				break;
			}
			case RIGHT: {
				targets = edge.rightViews;
				break;
			}
		}

		WorkpaneEdge commonBackEdge = null;
		for( WorkpaneView target : targets ) {
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

	private Side getPullMergeDirection( WorkpaneView target, boolean auto ) {
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

	Tool addTool( Tool tool ) {
		return addTool( tool, true );
	}

	Tool addTool( Tool tool, boolean activate ) {
		return addTool( tool, (WorkpaneView)null, activate );
	}

	//	public Tool addTool( Tool tool, Placement placement ) {
	//		if( placement == null ) placement = tool.getPlacement();
	//		return addTool( tool, determineViewFromPlacement( placement ) );
	//	}

	public Tool addTool( Tool tool, Placement placement, boolean activate ) {
		if( placement == null ) placement = tool.getPlacement();
		return addTool( tool, determineViewFromPlacement( placement ), activate );
	}

	Tool addTool( Tool tool, WorkpaneView view ) {
		return addTool( tool, view, true );
	}

	public Tool addTool( Tool tool, WorkpaneView view, boolean activate ) {
		return addTool( tool, view, view == null ? 0 : view.getTools().size(), activate );
	}

	public Tool addTool( Tool tool, WorkpaneView view, int index, boolean activate ) {
		if( tool.getToolView() != null || getViews().contains( tool.getToolView() ) ) return tool;

		try {
			startOperation();
			if( view == null ) view = determineViewFromPlacement( tool.getPlacement() );
			view.addTool( tool, index );
			if( activate ) setActiveTool( tool );
		} finally {
			finishOperation( true );
		}

		return tool;
	}

	public Tool removeTool( Tool tool ) {
		return removeTool( tool, true );
	}

	public Tool removeTool( Tool tool, boolean automerge ) {
		WorkpaneView view = tool.getToolView();
		if( view == null ) return tool;

		try {
			startOperation();
			view.removeTool( tool );
			if( tool.getSettings() != null ) tool.getSettings().delete();
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
	protected final ObservableList<Node> getChildren() {
		return super.getChildren();
	}

	@Override
	protected final void layoutChildren() {
		layout.layout();
	}

	final void layoutInArea( Node node, double areaX, double areaY, double areaWidth, double areaHeight ) {
		super.layoutInArea( node, areaX, areaY, areaWidth, areaHeight, 0, HPos.CENTER, VPos.CENTER );
	}

	/**
	 * Move the edge vertically because its orientation is horizontal. This method may be called from other edges that need to move as part of the bump and slide
	 * effect.
	 */
	private double moveVertical( WorkpaneEdge edge, double offset ) {
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
		double percent = (delta / (bounds.getHeight() - insets.getTop() - insets.getBottom()));
		edge.setPosition( edge.getPosition() + percent );

		if( isPlacementEdge( edge, Placement.DOCK_TOP ) ) topDockSize.set( edge.getPosition() );
		if( isPlacementEdge( edge, Placement.DOCK_BOTTOM ) ) bottomDockSize.set( 1 - edge.getPosition() );

		return delta;
	}

	/**
	 * Move the edge horizontally because its orientation is vertical. This method may be called from other edges that need to move as part of the bump and slide
	 * effect.
	 */
	private double moveHorizontal( WorkpaneEdge edge, double offset ) {
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
		double percent = (delta / (bounds.getWidth() - insets.getLeft() - insets.getRight()));
		edge.setPosition( edge.getPosition() + percent );

		if( isPlacementEdge( edge, Placement.DOCK_LEFT ) ) leftDockSize.set( edge.getPosition() );
		if( isPlacementEdge( edge, Placement.DOCK_RIGHT ) ) rightDockSize.set( 1 - edge.getPosition() );

		return delta;
	}

	private boolean isPlacementEdge( WorkpaneEdge edge, Placement placement ) {
		if( edge.getOrientation() == Orientation.HORIZONTAL ) {
			// Check top and bottom views
			for( WorkpaneView view : edge.getViews( Side.TOP ) ) {
				if( view.getPlacement() == placement ) return true;
			}
			for( WorkpaneView view : edge.getViews( Side.BOTTOM ) ) {
				if( view.getPlacement() == placement ) return true;
			}
		} else {
			// Check left and right views
			for( WorkpaneView view : edge.getViews( Side.LEFT ) ) {
				if( view.getPlacement() == placement ) return true;
			}
			for( WorkpaneView view : edge.getViews( Side.RIGHT ) ) {
				if( view.getPlacement() == placement ) return true;
			}
		}
		return false;
	}

	private double checkMoveNorth( WorkpaneEdge edge, double offset ) {
		double delta = offset;
		//Dimension viewSize = null;
		WorkpaneEdge blockingEdge = null;

		// Check the north views.
		for( WorkpaneView view : edge.topViews ) {
			double height = view.getHeight();
			if( height < -delta ) {
				blockingEdge = view.getEdge( Side.TOP );
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

	private double checkMoveSouth( WorkpaneEdge edge, double offset ) {
		double delta = offset;
		WorkpaneEdge blockingEdge = null;

		// Check the south views.
		for( WorkpaneView view : edge.bottomViews ) {
			double height = view.getHeight();
			if( height < delta ) {
				blockingEdge = view.getEdge( Side.BOTTOM );
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

	private double checkMoveWest( WorkpaneEdge edge, double offset ) {
		double delta = offset;
		WorkpaneEdge blockingEdge = null;

		// Check the west views.
		for( WorkpaneView view : edge.leftViews ) {
			double width = view.getWidth();
			if( width < -delta ) {
				blockingEdge = view.getEdge( Side.LEFT );
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

	private double checkMoveEast( WorkpaneEdge edge, double offset ) {
		double delta = offset;
		WorkpaneEdge blockingEdge = null;

		// Check the east views.
		for( WorkpaneView view : edge.rightViews ) {
			double width = view.getWidth();
			if( width < delta ) {
				blockingEdge = view.getEdge( Side.RIGHT );
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

	private boolean merge( WorkpaneEdge edge, Side direction ) {
		if( !canMerge( edge, direction, false ) ) return false;

		Set<WorkpaneView> sources = edge.getViews( getReverseDirection( direction ) );
		Set<WorkpaneView> targets = edge.getViews( direction );

		// Notify the listeners the views will merge.
		try {
			for( WorkpaneView source : sources ) {
				fireWorkpaneEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_WILL_MERGE, this, source ) );
			}
		} catch( WorkpaneVetoException exception ) {
			return false;
		}

		// Get needed objects.
		WorkpaneEdge farEdge = targets.iterator().next().getEdge( direction );

		// Extend the source views and edges.
		for( WorkpaneView source : sources ) {
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
		for( WorkpaneView target : targets ) {
			WorkpaneView closestSource = getClosest( sources, target, getPerpendicularDirectionOrientation( direction ) );

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
		edge.getWorkpane().removeEdge( edge );
		edge.setEdge( direction, null );
		edge.setEdge( getReverseDirection( direction ), null );
		edge.setEdge( getLeftDirection( direction ), null );
		edge.setEdge( getRightDirection( direction ), null );

		return true;
	}

	private WorkpaneView getClosest( Set<WorkpaneView> views, WorkpaneView target, Orientation orientation ) {
		WorkpaneView result = null;
		double distance = Double.MAX_VALUE;
		double resultDistance = Double.MAX_VALUE;
		double targetCenter = target.getCenter( orientation );

		for( WorkpaneView view : views ) {
			distance = Math.abs( targetCenter - view.getCenter( orientation ) );
			if( distance < resultDistance ) {
				result = view;
				resultDistance = distance;
			}
		}

		return result;
	}

	private void cleanupTargetEdge( WorkpaneView target, Side direction ) {
		WorkpaneEdge edge = target.getEdge( direction );

		// Remove the target from the edge.
		edge.getViews( getReverseDirection( direction ) ).remove( target );

		// If there are no more associated views, remove the edge.
		if( !edge.isWall() && edge.getViews( direction ).size() == 0 && edge.getViews( getReverseDirection( direction ) ).size() == 0 ) removeEdge( edge );
	}

	private WorkpaneView determineViewFromPlacement( Workpane.Placement placement ) {
		WorkpaneView view = null;

		switch( placement ) {
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
			case DOCK_TOP: {
				view = getTopDockView();
				break;
			}
			case DOCK_LEFT: {
				view = getLeftDockView();
				break;
			}
			case DOCK_RIGHT: {
				view = getRightDockView();
				break;
			}
			case DOCK_BOTTOM: {
				view = getBottomDockView();
				break;
			}
		}

		return view;
	}

	/**
	 * Split the workpane using the space to the north for a new tool view along the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private WorkpaneView splitNorth( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		return newTopView( null, leftWall, rightWall, percent );
		//return getTopDockView();
	}

	/**
	 * Split the workpane using the space to the south for a new tool view along the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private WorkpaneView splitSouth( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		return newBottomView( null, leftWall, rightWall, percent );
		//return getBottomDockView();
	}

	/**
	 * Split the workpane using the space to the west for a new tool view along the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private WorkpaneView splitWest( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		return newLeftView( null, topWall, bottomWall, percent );
		//return getLeftDockView();
	}

	/**
	 * Split the workpane using the space to the east for a new tool view along the entire edge of the workpane.
	 *
	 * @param percent
	 * @return
	 */
	private WorkpaneView splitEast( double percent ) {
		// Check the parameters.
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Create the new view.
		return newRightView( null, topWall, bottomWall, percent );
		//return getRightDockView();
	}

	/**
	 * Split an existing tool view using the space to the north for a new tool view.
	 *
	 * @param source
	 * @param percent
	 * @return
	 */
	private WorkpaneView splitNorth( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireWorkpaneEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		if( source.getPlacement() != Placement.DOCK_TOP && isDockSpace( Side.TOP, source ) ) return getTopDockView();

		// Create the new view.
		return newTopView( source, source.getEdge( Side.LEFT ), source.getEdge( Side.RIGHT ), percent );
	}

	private WorkpaneView splitSouth( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireWorkpaneEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		if( source.getPlacement() != Placement.DOCK_BOTTOM && isDockSpace( Side.BOTTOM, source ) ) return getBottomDockView();

		// Create the new view.
		return newBottomView( source, source.getEdge( Side.LEFT ), source.getEdge( Side.RIGHT ), percent );
	}

	private WorkpaneView splitWest( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireWorkpaneEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		if( source.getPlacement() != Placement.DOCK_LEFT && isDockSpace( Side.LEFT, source ) ) return getLeftDockView();

		// Create the new view.
		return newLeftView( source, source.getEdge( Side.TOP ), source.getEdge( Side.BOTTOM ), percent );
	}

	private WorkpaneView splitEast( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the will split event.
		try {
			fireWorkpaneEvent( new WorkpaneViewEvent( this, WorkpaneEvent.Type.VIEW_WILL_SPLIT, this, source ) );
		} catch( WorkpaneVetoException exception ) {
			return null;
		}

		if( source.getPlacement() != Placement.DOCK_RIGHT && isDockSpace( Side.RIGHT, source ) ) return getRightDockView();

		// Create the new view.
		return newRightView( source, source.getEdge( Side.TOP ), source.getEdge( Side.BOTTOM ), percent );
	}

	boolean isDockSpace( Side side, WorkpaneView source ) {
		if( source == null ) return false;

		WorkpaneEdge leftTurn = source.getEdge( getLeftDirection( side ) );
		WorkpaneEdge direct = source.getEdge( side );
		WorkpaneEdge rightTurn = source.getEdge( getRightDirection( side ) );

		switch( side ) {
			case TOP: {
				return leftTurn == leftWall && direct == topWall && rightTurn == rightWall;
			}
			case BOTTOM: {
				return leftTurn == rightWall && direct == bottomWall && rightTurn == leftWall;
			}
			case LEFT: {
				return leftTurn == bottomWall && direct == leftWall && rightTurn == topWall;
			}
			case RIGHT: {
				return leftTurn == topWall && direct == rightWall && rightTurn == bottomWall;
			}
		}

		return false;
	}

	private boolean isDockSpace( Side side, WorkpaneEdge leftDirection, WorkpaneEdge rightDirection ) {
		switch( side ) {
			case TOP: {
				return leftDirection == leftWall && rightDirection == rightWall;
			}
			case BOTTOM: {
				return leftDirection == rightWall && rightDirection == leftWall;
			}
			case LEFT: {
				return leftDirection == bottomWall && rightDirection == topWall;
			}
			case RIGHT: {
				return leftDirection == topWall && rightDirection == bottomWall;
			}
		}
		return false;
	}

	boolean isDockSpace( Side side, WorkpaneView source, WorkpaneEdge leftDirection, WorkpaneEdge rightDirection ) {
		return isDockSpace( side, source ) || isDockSpace( side, leftDirection, rightDirection );
	}

	private WorkpaneView getTopDockView() {
		WorkpaneView view = getDockedView( Placement.DOCK_TOP );
		if( view != null ) return view;

		if( getDockMode() == DockMode.PORTRAIT ) {
			view = split( Side.TOP, getTopDockSize() );
		} else {
			WorkpaneView leftView = getDockedView( Placement.DOCK_LEFT );
			WorkpaneView rightView = getDockedView( Placement.DOCK_RIGHT );

			WorkpaneEdge leftEdge = leftView == null ? this.leftWall : leftView.getEdge( Side.RIGHT );
			WorkpaneEdge rightEdge = rightView == null ? this.rightWall : rightView.getEdge( Side.LEFT );

			view = newTopView( null, leftEdge, rightEdge, getTopDockSize() );
		}
		view.setPlacement( Placement.DOCK_TOP );

		return view;
	}

	private WorkpaneView getLeftDockView() {
		WorkpaneView view = getDockedView( Placement.DOCK_LEFT );
		if( view != null ) return view;

		if( getDockMode() == DockMode.LANDSCAPE ) {
			view = split( Side.LEFT, getLeftDockSize() );
		} else {
			WorkpaneView topView = getDockedView( Placement.DOCK_TOP );
			WorkpaneView bottomView = getDockedView( Placement.DOCK_BOTTOM );

			WorkpaneEdge topEdge = topView == null ? this.topWall : topView.getEdge( Side.BOTTOM );
			WorkpaneEdge bottomEdge = bottomView == null ? this.bottomWall : bottomView.getEdge( Side.TOP );

			view = newLeftView( null, topEdge, bottomEdge, getLeftDockSize() );
		}
		view.setPlacement( Placement.DOCK_LEFT );

		return view;
	}

	private WorkpaneView getRightDockView() {
		WorkpaneView view = getDockedView( Placement.DOCK_RIGHT );
		if( view != null ) return view;

		if( getDockMode() == DockMode.LANDSCAPE ) {
			view = split( Side.RIGHT, getRightDockSize() );
		} else {
			WorkpaneView topView = getDockedView( Placement.DOCK_TOP );
			WorkpaneView bottomView = getDockedView( Placement.DOCK_BOTTOM );

			WorkpaneEdge topEdge = topView == null ? this.topWall : topView.getEdge( Side.BOTTOM );
			WorkpaneEdge bottomEdge = bottomView == null ? this.bottomWall : bottomView.getEdge( Side.TOP );

			view = newRightView( null, topEdge, bottomEdge, getRightDockSize() );
		}
		view.setPlacement( Placement.DOCK_RIGHT );

		return view;
	}

	private WorkpaneView getBottomDockView() {
		WorkpaneView view = getDockedView( Placement.DOCK_BOTTOM );
		if( view != null ) return view;

		if( getDockMode() == DockMode.PORTRAIT ) {
			view = split( Side.BOTTOM, getBottomDockSize() );
		} else {
			WorkpaneView leftView = getDockedView( Placement.DOCK_LEFT );
			WorkpaneView rightView = getDockedView( Placement.DOCK_RIGHT );

			WorkpaneEdge leftEdge = leftView == null ? this.leftWall : leftView.getEdge( Side.RIGHT );
			WorkpaneEdge rightEdge = rightView == null ? this.rightWall : rightView.getEdge( Side.LEFT );

			view = newBottomView( null, leftEdge, rightEdge, getBottomDockSize() );
		}
		view.setPlacement( Placement.DOCK_BOTTOM );

		return view;
	}

	private WorkpaneView newTopView( WorkpaneView source, WorkpaneEdge leftEdge, WorkpaneEdge rightEdge, double percent ) {
		WorkpaneView newView = new WorkpaneView();
		createViewSettings( newView );

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge( Orientation.HORIZONTAL );
		createEdgeSettings( newEdge );
		newEdge.setEdge( Side.LEFT, leftEdge );
		newEdge.setEdge( Side.RIGHT, rightEdge );

		// Connect the new edge to the old and new views.
		if( source == null ) {
			for( WorkpaneView view : topWall.bottomViews ) {
				topWall.bottomViews.remove( view );
				newEdge.bottomViews.add( view );
				view.setEdge( Side.TOP, newEdge );
			}
		} else {
			newEdge.bottomViews.add( source );
		}
		newEdge.topViews.add( newView );

		// Connect the new view to old and new edges.
		if( source == null ) {
			newView.setEdge( Side.TOP, topWall );
			newView.setEdge( Side.BOTTOM, newEdge );
			newView.setEdge( Side.LEFT, leftEdge );
			newView.setEdge( Side.RIGHT, rightEdge );
		} else {
			newView.setEdge( Side.TOP, source.getEdge( Side.TOP ) );
			newView.setEdge( Side.BOTTOM, newEdge );
			newView.setEdge( Side.LEFT, source.getEdge( Side.LEFT ) );
			newView.setEdge( Side.RIGHT, source.getEdge( Side.RIGHT ) );
		}

		// Connect the old edges to the new view.
		if( source == null ) {
			topWall.bottomViews.add( newView );
			leftEdge.rightViews.add( newView );
			rightEdge.leftViews.add( newView );
		} else {
			source.getEdge( Side.TOP ).bottomViews.remove( source );
			source.getEdge( Side.TOP ).bottomViews.add( newView );
			source.getEdge( Side.LEFT ).rightViews.add( newView );
			source.getEdge( Side.RIGHT ).leftViews.add( newView );
		}

		if( source == null ) {
			newEdge.setPosition( percent );

			// Connect the old edges to the new edge.
			for( WorkpaneEdge edge : getEdges() ) {
				if( edge.getEdge( Side.TOP ) != topWall ) continue;
				edge.setEdge( Side.TOP, newEdge );
			}
		} else {
			double height = source.getEdge( Side.BOTTOM ).getPosition() - source.getEdge( Side.TOP ).getPosition();
			newEdge.setPosition( source.getEdge( Side.TOP ).getPosition() + (height * percent) );

			// Connect the old view to the new edge.
			source.setEdge( Side.TOP, newEdge );
		}

		addEdge( newEdge );
		addView( newView );

		return newView;
	}

	private WorkpaneView newLeftView( WorkpaneView source, WorkpaneEdge topEdge, WorkpaneEdge bottomEdge, double percent ) {
		// Create the new view.
		WorkpaneView newView = new WorkpaneView();
		createViewSettings( newView );

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge( Orientation.VERTICAL );
		createEdgeSettings( newEdge );
		newEdge.setEdge( Side.TOP, topEdge );
		newEdge.setEdge( Side.BOTTOM, bottomEdge );

		// Connect the new edge to the old and new views.
		if( source == null ) {
			for( WorkpaneView view : leftWall.rightViews ) {
				leftWall.rightViews.remove( view );
				newEdge.rightViews.add( view );
				view.setEdge( Side.LEFT, newEdge );
			}
		} else {
			newEdge.rightViews.add( source );
		}
		newEdge.leftViews.add( newView );

		// Connect the new view to old and new edges.
		newView.setEdge( Side.TOP, topEdge );
		newView.setEdge( Side.BOTTOM, bottomEdge );
		newView.setEdge( Side.LEFT, source == null ? leftWall : source.getEdge( Side.LEFT ) );
		newView.setEdge( Side.RIGHT, newEdge );

		// Connect the old edges to the new view.
		if( source == null ) {
			leftWall.rightViews.add( newView );
			this.topWall.bottomViews.add( newView );
			this.bottomWall.topViews.add( newView );
		} else {
			source.getEdge( Side.LEFT ).rightViews.remove( source );
			source.getEdge( Side.LEFT ).rightViews.add( newView );
			source.getEdge( Side.TOP ).bottomViews.add( newView );
			source.getEdge( Side.BOTTOM ).topViews.add( newView );
		}

		if( source == null ) {
			newEdge.setPosition( percent );

			// Connect the old edges to the new edge.
			for( WorkpaneEdge edge : getEdges() ) {
				if( edge.getEdge( Side.LEFT ) != leftWall ) continue;
				edge.setEdge( Side.LEFT, newEdge );
			}
		} else {
			double width = source.getEdge( Side.RIGHT ).getPosition() - source.getEdge( Side.LEFT ).getPosition();
			newEdge.setPosition( source.getEdge( Side.LEFT ).getPosition() + (width * percent) );

			// Connect the old view to the new edge.
			source.setEdge( Side.LEFT, newEdge );
		}

		addEdge( newEdge );
		addView( newView );

		return newView;
	}

	private WorkpaneView newRightView( WorkpaneView source, WorkpaneEdge topEdge, WorkpaneEdge bottomEdge, double percent ) {
		// Create the new view.
		WorkpaneView newView = new WorkpaneView();
		createViewSettings( newView );

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge( Orientation.VERTICAL );
		createEdgeSettings( newEdge );
		newEdge.setEdge( Side.TOP, topEdge );
		newEdge.setEdge( Side.BOTTOM, bottomEdge );

		// Connect the new edge to the old and new views.
		if( source == null ) {
			for( WorkpaneView view : rightWall.leftViews ) {
				rightWall.leftViews.remove( view );
				newEdge.leftViews.add( view );
				view.setEdge( Side.RIGHT, newEdge );
			}
		} else {
			newEdge.leftViews.add( source );
		}
		newEdge.rightViews.add( newView );

		// Connect the new view to old and new edges.
		if( source == null ) {
			newView.setEdge( Side.TOP, topEdge );
			newView.setEdge( Side.BOTTOM, bottomEdge );
			newView.setEdge( Side.LEFT, newEdge );
			newView.setEdge( Side.RIGHT, rightWall );
		} else {
			newView.setEdge( Side.TOP, source.getEdge( Side.TOP ) );
			newView.setEdge( Side.BOTTOM, source.getEdge( Side.BOTTOM ) );
			newView.setEdge( Side.LEFT, newEdge );
			newView.setEdge( Side.RIGHT, source.getEdge( Side.RIGHT ) );
		}

		// Connect the old edges to the new view.
		if( source == null ) {
			rightWall.leftViews.add( newView );
			this.topWall.bottomViews.add( newView );
			this.bottomWall.topViews.add( newView );
		} else {
			source.getEdge( Side.RIGHT ).leftViews.remove( source );
			source.getEdge( Side.RIGHT ).leftViews.add( newView );
			source.getEdge( Side.TOP ).bottomViews.add( newView );
			source.getEdge( Side.BOTTOM ).topViews.add( newView );
		}

		if( source == null ) {
			newEdge.setPosition( 1 - percent );

			// Connect the old edges to the new edge.
			for( WorkpaneEdge edge : getEdges() ) {
				if( edge.getEdge( Side.RIGHT ) != rightWall ) continue;
				edge.setEdge( Side.RIGHT, newEdge );
			}
		} else {
			double width = source.getEdge( Side.RIGHT ).getPosition() - source.getEdge( Side.LEFT ).getPosition();
			newEdge.setPosition( source.getEdge( Side.RIGHT ).getPosition() - (width * percent) );

			// Connect the old view to the new edge.
			source.setEdge( Side.RIGHT, newEdge );
		}

		addEdge( newEdge );
		addView( newView );

		return newView;
	}

	private WorkpaneView newBottomView( WorkpaneView source, WorkpaneEdge leftEdge, WorkpaneEdge rightEdge, double percent ) {
		WorkpaneView newView = new WorkpaneView();
		createViewSettings( newView );

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge( Orientation.HORIZONTAL );
		createEdgeSettings( newEdge );
		newEdge.setEdge( Side.LEFT, leftEdge );
		newEdge.setEdge( Side.RIGHT, rightEdge );

		// Connect the new edge to the old and new views.
		if( source == null ) {
			for( WorkpaneView view : bottomWall.topViews ) {
				bottomWall.topViews.remove( view );
				newEdge.topViews.add( view );
				view.setEdge( Side.BOTTOM, newEdge );
			}
		} else {
			newEdge.topViews.add( source );
		}
		newEdge.bottomViews.add( newView );

		// Connect the new view to old and new edges.
		if( source == null ) {
			newView.setEdge( Side.TOP, newEdge );
			newView.setEdge( Side.BOTTOM, bottomWall );
			newView.setEdge( Side.LEFT, leftEdge );
			newView.setEdge( Side.RIGHT, rightEdge );
		} else {
			newView.setEdge( Side.TOP, newEdge );
			newView.setEdge( Side.BOTTOM, source.getEdge( Side.BOTTOM ) );
			newView.setEdge( Side.LEFT, source.getEdge( Side.LEFT ) );
			newView.setEdge( Side.RIGHT, source.getEdge( Side.RIGHT ) );
		}

		// Connect the old edges to the new view.
		if( source == null ) {
			bottomWall.topViews.add( newView );
			leftEdge.rightViews.add( newView );
			rightEdge.leftViews.add( newView );
		} else {
			source.getEdge( Side.BOTTOM ).topViews.remove( source );
			source.getEdge( Side.BOTTOM ).topViews.add( newView );
			source.getEdge( Side.LEFT ).rightViews.add( newView );
			source.getEdge( Side.RIGHT ).leftViews.add( newView );
		}

		if( source == null ) {
			newEdge.setPosition( 1 - percent );

			// Connect the old edges to the new edge.
			for( WorkpaneEdge edge : getEdges() ) {
				if( edge.getEdge( Side.BOTTOM ) != bottomWall ) continue;
				edge.setEdge( Side.BOTTOM, newEdge );
			}
		} else {
			double height = source.getEdge( Side.BOTTOM ).getPosition() - source.getEdge( Side.TOP ).getPosition();
			newEdge.setPosition( source.getEdge( Side.BOTTOM ).getPosition() - (height * percent) );

			// Connect the old view to the new edge.
			source.setEdge( Side.BOTTOM, newEdge );
		}

		addEdge( newEdge );
		addView( newView );

		return newView;
	}

	private boolean isDockedView( WorkpaneView view ) {
		return isDockedPlacement( view.getPlacement() );
	}

	private boolean isDockedView( WorkpaneView view, Placement placement ) {
		return view.getPlacement() == placement && isDockedPlacement( placement );
	}

	private boolean isDockedPlacement( Placement placement ) {
		return placement == Placement.DOCK_TOP || placement == Placement.DOCK_LEFT || placement == Placement.DOCK_RIGHT || placement == Placement.DOCK_BOTTOM;
	}

	private WorkpaneView getDockedView( Placement placement ) {
		Side side = null;
		WorkpaneEdge edge = null;
		switch( placement ) {
			case DOCK_TOP: {
				edge = topWall;
				side = Side.BOTTOM;
				break;
			}
			case DOCK_BOTTOM: {
				edge = bottomWall;
				side = Side.TOP;
				break;
			}
			case DOCK_LEFT: {
				edge = leftWall;
				side = Side.RIGHT;
				break;
			}
			case DOCK_RIGHT: {
				edge = rightWall;
				side = Side.LEFT;
				break;
			}
		}

		if( edge == null ) return null;

		for( WorkpaneView testView : edge.getViews( side ) ) {
			if( testView.getPlacement() == placement ) return testView;
		}

		return null;
	}

	private void createEdgeSettings( WorkpaneEdge edge ) {
		Settings paneSettings = getSettings();
		if( paneSettings == null ) return;

		Settings settings = paneSettings.getNode( ProgramSettings.EDGE, IdGenerator.getId() );
		settings.set( UiFactory.PARENT_WORKPANE_ID, getSettings().getName() );
		edge.setSettings( settings );
	}

	private void createViewSettings( WorkpaneView view ) {
		Settings paneSettings = getSettings();
		if( paneSettings == null ) return;

		Settings settings = paneSettings.getNode( ProgramSettings.VIEW, IdGenerator.getId() );
		settings.set( UiFactory.PARENT_WORKPANE_ID, getSettings().getName() );
		view.setSettings( settings );
	}

	private static class MergeDirection implements Comparable<MergeDirection> {

		Side direction;

		int weight;

		MergeDirection( WorkpaneView target, Side direction ) {
			this.direction = direction;
			this.weight = getMergeWeight( target, direction );
			log.trace( "Direction: " + direction + "  Weight: " + weight );
		}

		Side getDirection() {
			return direction;
		}

		int getWeight() {
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
			return side == null ? 0 : side.ordinal() + 1;
		}

		private int getMergeWeight( WorkpaneView target, Side side ) {
			WorkpaneEdge edge = null;
			Set<WorkpaneView> sourceViews = null;
			Set<WorkpaneView> targetViews = null;

			switch( side ) {
				case TOP: {
					edge = target.getEdge( Side.TOP );
					sourceViews = edge.bottomViews;
					targetViews = edge.topViews;
					break;
				}
				case BOTTOM: {
					edge = target.getEdge( Side.BOTTOM );
					sourceViews = edge.topViews;
					targetViews = edge.bottomViews;
					break;
				}
				case LEFT: {
					edge = target.getEdge( Side.LEFT );
					sourceViews = edge.rightViews;
					targetViews = edge.leftViews;
					break;
				}
				case RIGHT: {
					edge = target.getEdge( Side.RIGHT );
					sourceViews = edge.leftViews;
					targetViews = edge.rightViews;
					break;
				}
			}

			int result = 10 * (sourceViews.size() + targetViews.size() - 1);
			if( edge.isWall() ) result = Integer.MAX_VALUE;

			return result;
		}

	}

}
