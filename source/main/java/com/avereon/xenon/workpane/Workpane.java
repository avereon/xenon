package com.avereon.xenon.workpane;

import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.util.Log;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.lang.System.Logger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Workpane extends Control implements WritableIdentity {

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

	private static final boolean DEFAULT_AUTO_MERGE = true;

	private static final Logger log = Log.get();

	private final WorkpaneEdge topWall;

	private final WorkpaneEdge leftWall;

	private final WorkpaneEdge rightWall;

	private final WorkpaneEdge bottomWall;

	private WorkpaneDropHint dragHint;

	private final DoubleProperty edgeSizeProperty;

	private final BooleanProperty autoMergeProperty;

	private final WorkpaneLayout layout;

	private final ObjectProperty<WorkpaneView> activeViewProperty;

	private final ObjectProperty<WorkpaneView> defaultViewProperty;

	private final ObjectProperty<WorkpaneView> maximizedViewProperty;

	private final ObjectProperty<Tool> activeToolProperty;

	private final ObjectProperty<DockMode> dockModeProperty;

	private final DoubleProperty topDockSize;

	private final DoubleProperty leftDockSize;

	private final DoubleProperty rightDockSize;

	private final DoubleProperty bottomDockSize;

	private final AtomicInteger operation;

	private final Queue<WorkpaneEvent> events;

	private final DropListener defaultDropHandler;

	private DropListener dropHandler;

	public Workpane() {
		layout = new WorkpaneLayout( this );

		getStyleClass().add( "workpane" );

		edgeSizeProperty = new SimpleDoubleProperty( DEFAULT_EDGE_SIZE );
		autoMergeProperty = new SimpleBooleanProperty( DEFAULT_AUTO_MERGE );
		activeViewProperty = new SimpleObjectProperty<>();
		defaultViewProperty = new SimpleObjectProperty<>();
		maximizedViewProperty = new SimpleObjectProperty<>();
		activeToolProperty = new SimpleObjectProperty<>();
		dockModeProperty = new SimpleObjectProperty<>();

		topDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );
		leftDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );
		rightDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );
		bottomDockSize = new SimpleDoubleProperty( DEFAULT_WALL_SPLIT_RATIO );

		operation = new AtomicInteger();
		events = new LinkedList<>();

		// Create the wall edges
		topWall = new WorkpaneEdge( Side.TOP ).setOrientation( Orientation.HORIZONTAL );
		leftWall = new WorkpaneEdge( Side.LEFT ).setOrientation( Orientation.VERTICAL );
		rightWall = new WorkpaneEdge( Side.RIGHT ).setOrientation( Orientation.VERTICAL );
		bottomWall = new WorkpaneEdge( Side.BOTTOM ).setOrientation( Orientation.HORIZONTAL );

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

		setOnToolDrop( defaultDropHandler = new DefaultDropHandler() );
		setDockMode( DEFAULT_DOCK_MODE );

		// TODO Set a better default background
		setBackground( new Background( new BackgroundFill( new Color( 0.2, 0.2, 0.2, 1.0 ), CornerRadii.EMPTY, Insets.EMPTY ) ) );

		visibleProperty().addListener( ( o, v, n ) -> setActive( n ) );
	}

	@Override
	public String getUid() {
		return getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setUid( String id ) {
		getProperties().put( Identity.KEY, id );
	}

	/**
	 * Returns an unmodifiable list of the edges.
	 *
	 * @return The edges in the workpane
	 */
	public Set<WorkpaneEdge> getEdges() {
		return getChildren()
			.stream()
			.filter( ( c ) -> c instanceof WorkpaneEdge )
			.map( n -> (WorkpaneEdge)n )
			.filter( e -> !e.isWall() )
			.collect( Collectors.toSet() );
	}

	/**
	 * Returns an unmodifiable list of the views.
	 *
	 * @return The views in the workpane
	 */
	public Set<WorkpaneView> getViews() {
		return getChildren().stream().filter( n -> n instanceof WorkpaneView ).map( n -> (WorkpaneView)n ).collect( Collectors.toSet() );
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
		return edgeSizeProperty.get();
	}

	public void setEdgeSize( double size ) {
		edgeSizeProperty.set( size );
		updateComponentTree( true );
	}

	public DoubleProperty edgeSizeProperty() {
		return edgeSizeProperty;
	}

	public boolean isAutoMerge() {
		return autoMergeProperty == null ? DEFAULT_AUTO_MERGE : autoMergeProperty.get();
	}

	public void setAutoMerge( boolean autoMerge ) {
		autoMergeProperty.set( autoMerge );
	}

	public BooleanProperty autoMergeProperty() {
		return autoMergeProperty;
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
			//
		}

		defaultViewProperty.set( view );
		defaultView = view;

		if( defaultView != null ) {
			//
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
			//
		}

		maximizedViewProperty.set( view );
		maximizedView = view;

		if( maximizedView != null ) {
			//
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

	public void setOnToolDrop( DropListener listener ) {
		this.dropHandler = listener == null ? defaultDropHandler : listener;
	}

	DropListener getOnToolDrop() {
		return this.dropHandler;
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

	void handleDrop( DropEvent event ) throws Exception {
		try {
			dropHandler.handleDrop( event );
		} finally {
			setDropHint( null );
		}
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new WorkpaneSkin( this );
	}

	private void setActive( boolean active ) {
		try {
			startOperation();
			if( active ) {
				Tool tool = getActiveTool();
				if( tool != null ) tool.callActivate();
			} else {
				getViews().forEach( v -> {
					Tool tool = v.getActiveTool();
					if( tool != null ) tool.callConceal();
				} );
			}
		} finally {
			finishOperation( true );
		}
	}

	private void startOperation() {
		operation.incrementAndGet();
	}

	private boolean isOperationActive() {
		return operation.get() > 0;
	}

	private void finishOperation( boolean changed ) {
		int value = operation.decrementAndGet();
		if( value < 0 ) log.log( Log.ERROR, "Workpane operation flag is less than zero." );
		updateComponentTree( changed );
	}

	private void dispatchEvents() {
		WorkpaneEvent event;
		while( (event = events.poll()) != null ) fireEvent( event );
	}

	WorkpaneEvent queueEvent( WorkpaneEvent event ) {
		if( !isOperationActive() ) throw new RuntimeException( "Event should only be queued during active operations: " + event.getEventType() );
		events.offer( event );
		return event;
	}

	private void updateComponentTree( boolean changed ) {
		if( isOperationActive() ) return;

		if( changed ) events.offer( new WorkpaneEvent( this, WorkpaneEvent.CHANGED, this ) );

		layoutChildren();
		dispatchEvents();
	}

	private void doSetActiveView( WorkpaneView view, boolean setActiveToolAlso ) {
		if( view != null && (view == getActiveView() || !getViews().contains( view )) ) return;

		startOperation();
		try {
			WorkpaneView activeToolView = getActiveView();
			if( activeToolView != null ) {
				activeToolView.setActive( false );
				queueEvent( new ViewEvent( this, ViewEvent.DEACTIVATED, this, activeToolView ) );
			}

			// Change the active view
			activeViewProperty.set( view );

			// Handle the new active view
			activeToolView = getActiveView();
			if( activeToolView != null ) {
				activeToolView.setActive( true );
				if( setActiveToolAlso ) doSetActiveTool( activeToolView.getActiveTool(), false );
				queueEvent( new ViewEvent( this, ViewEvent.ACTIVATED, this, activeToolView ) );
			}
		} finally {
			finishOperation( true );
		}
	}

	private void doSetActiveTool( Tool tool, boolean activateViewAlso ) {
		// Make sure the tool is contained by this workpane
		if( tool != null && tool.getWorkpane() != this ) return;

		Tool activeTool;
		startOperation();
		try {
			activeTool = getActiveTool();
			if( activeTool != null ) {
				activeTool.callDeactivate();
			}

			// Change the active view
			WorkpaneView view = tool == null ? null : tool.getToolView();
			if( view != null && getViews().contains( view ) ) {
				view.setActiveTool( tool );
				if( activateViewAlso && view != getActiveView() ) doSetActiveView( view, false );
			}

			// Change the active tool
			activeToolProperty.set( tool );

			activeTool = getActiveTool();
			if( activeTool != null ) {
				activeTool.callActivate();
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

	public void clearNodes() {
		restoreNodes( Set.of(), Set.of() );
	}

	/**
	 * For use when restoring the state of the workpane.
	 */
	public void restoreNodes( Set<WorkpaneEdge> edges, Set<WorkpaneView> views ) {
		startOperation();
		try {
			// Remove existing views and edges
			getViews().forEach( this::removeView );
			getEdges().forEach( this::removeEdge );

			// Add edges and views
			edges.forEach( this::addEdge );
			views.forEach( this::addView );
		} finally {
			finishOperation( false );
		}
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private WorkpaneView addView( WorkpaneView view ) {
		if( view == null ) return null;

		startOperation();
		try {
			view.setWorkpane( this );
			getChildren().add( view );
			queueEvent( new ViewEvent( this, ViewEvent.ADDED, this, view ) );
		} finally {
			finishOperation( true );
		}

		return view;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private WorkpaneView removeView( WorkpaneView view ) {
		if( view == null ) return null;

		try {
			startOperation();

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

			queueEvent( new ViewEvent( this, ViewEvent.REMOVED, this, view ) );
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

	@SuppressWarnings( "UnusedReturnValue" )
	private WorkpaneEdge addEdge( WorkpaneEdge edge ) {
		if( edge == null ) return null;

		edge.setWorkpane( this );
		getChildren().add( edge );
		queueEvent( new EdgeEvent( this, EdgeEvent.ADDED, this, edge ) );
		edge.positionProperty().addListener( ( observable, oldValue, newValue ) -> queueEvent( new EdgeEvent( this, EdgeEvent.MOVED, this, edge ) ) );

		return edge;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private WorkpaneEdge removeEdge( WorkpaneEdge edge ) {
		if( edge == null ) return null;

		getChildren().remove( edge );
		edge.setWorkpane( null );

		queueEvent( new EdgeEvent( this, EdgeEvent.REMOVED, this, edge ) );

		return edge;
	}

	/**
	 * Move the specified edge the specified offset in pixels.
	 *
	 * @param edge The edge to move
	 * @param offset The distance to move in pixels
	 * @return The actual distance moved
	 */
	@SuppressWarnings( "UnusedReturnValue" )
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
	 * Split the workpane using the space in the specified direction to make a new
	 * tool view along the entire edge of the workpane.
	 *
	 * @param side Which side of the pane to split
	 * @return The new view
	 */
	public WorkpaneView split( Side side ) {
		return split( side, DEFAULT_WALL_SPLIT_RATIO );
	}

	/**
	 * Split the workpane using the space in the specified direction to make a new tool view along the entire edge of the workpane. The new tool view is created
	 * using the specified percentage of the original space.
	 *
	 * @param side Which side of the pane to split
	 * @param percent The percent of space to use
	 * @return The new view
	 */
	public WorkpaneView split( Side side, double percent ) {
		WorkpaneView result = null;
		startOperation();
		try {
			// Calculate the location of the split.
			switch( side ) {
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

			queueEvent( new ViewEvent( this, ViewEvent.SPLIT, this, null ) );
		} finally {
			finishOperation( true );
		}

		return result;
	}

	/**
	 * Split an existing tool view using the space in the specified direction to create a new tool view.
	 *
	 * @param view The view to split
	 * @param side Which side of the view to split
	 * @return The new view
	 */
	public WorkpaneView split( WorkpaneView view, Side side ) {
		return split( view, side, DEFAULT_VIEW_SPLIT_RATIO );
	}

	/**
	 * Split an existing tool view using the space in the specified direction to create a new tool view. The new tool view is created using the specified
	 * percentage of the original space.
	 *
	 * @param view The view to split
	 * @param side Which side of the pane to split
	 * @param percent The percent of space to use
	 * @return The new view
	 */
	public WorkpaneView split( WorkpaneView view, Side side, double percent ) {
		WorkpaneView result = null;
		startOperation();
		try {
			// Calculate the location of the split.
			switch( side ) {
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
			queueEvent( new ViewEvent( this, ViewEvent.SPLIT, this, view ) );
		} finally {
			finishOperation( true );
		}

		return result;
	}

	private static Side getOppositeSide( Side side ) {
		switch( side ) {
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

	@SuppressWarnings( "SuspiciousNameCombination" )
	private static Side getSideAtLeft( Side side ) {
		switch( side ) {
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

	@SuppressWarnings( "SuspiciousNameCombination" )
	private static Side getSideAtRight( Side side ) {
		switch( side ) {
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

	private static Orientation getPerpendicularDirectionOrientation( Side side ) {
		return side.isHorizontal() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
	}

	/**
	 * Performs an automatic pull merge. The side is automatically determined by
	 * a weighted algorithm.
	 *
	 * @param target The target view
	 * @return If the merge was successful
	 */
	@SuppressWarnings( "UnusedReturnValue" )
	public boolean pullMerge( WorkpaneView target ) {
		Side direction = getPullMergeDirection( target, true );
		return direction != null && pullMerge( target, direction );
	}

	/**
	 * Performs a pull merge toward the specified side.
	 *
	 * @param target The target view
	 * @param side The side to which to pull
	 * @return If the merge was successful
	 */
	public boolean pullMerge( WorkpaneView target, Side side ) {
		// Check the parameters.
		if( target == null ) return false;

		boolean result = false;
		try {
			startOperation();
			result = merge( target.getEdge( getOppositeSide( side ) ), side );
			if( result ) queueEvent( new ViewEvent( this, ViewEvent.MERGED, this, target ) );
		} finally {
			finishOperation( result );
		}

		return result;
	}

	/**
	 * Performs a push merge in the specified direction.
	 *
	 * @param source The view to merge from
	 * @param side The side to merge toward
	 * @return True if the merge was successful, false otherwise
	 */
	@SuppressWarnings( "UnusedReturnValue" )
	public boolean pushMerge( WorkpaneView source, Side side ) {
		if( source == null ) return false;

		boolean result = false;
		try {
			startOperation();
			result = merge( source.getEdge( side ), side );
			if( result ) queueEvent( new ViewEvent( this, ViewEvent.MERGED, this, source ) );
		} finally {
			finishOperation( result );
		}

		return result;
	}

	public boolean canPushMerge( WorkpaneView source, Side side, boolean auto ) {
		return canMerge( source.getEdge( side ), side, auto );
	}

	public boolean canPullMerge( WorkpaneView target, Side side, boolean auto ) {
		return canMerge( target.getEdge( getOppositeSide( side ) ), side, auto );
	}

	/**
	 * Returns whether views on the source (opposite of direction) side of the
	 * edge can be merged into the space occupied by the views on the target
	 * (towards direction) side of the edge. The method returns false if any of
	 * the following conditions exist:
	 * <ul>
	 *   <li>If the edge is a wall edge</li>
	 *   <li>If any of the target views is the default view</li>
	 *   <li>If the target views do not share a common back edge</li>
	 *   <li>If the auto flag is set to true and any of the target views have tools</li>
	 * </ul>
	 *
	 * @param edge The edge across which views are to be merged.
	 * @param direction The direction of the merge.
	 * @param auto Check if views can automatically be merged.
	 * @return True if the views can be merged, false otherwise
	 */
	private boolean canMerge( WorkpaneEdge edge, Side direction, boolean auto ) {
		if( edge == null ) return false;
		if( edge.isWall() ) return false;

		WorkpaneEdge commonBackEdge = null;
		for( WorkpaneView target : edge.getViews( direction ) ) {
			// Check for the default view in targets.
			if( target == getDefaultView() ) return false;

			// If auto, check the tool counts
			if( auto && target.getTools().size() > 0 ) return false;

			// Check targets for common back edge
			if( commonBackEdge == null ) commonBackEdge = target.getEdge( direction );
			if( target.getEdge( direction ) != commonBackEdge ) return false;
		}

		return true;
	}

	private Side getPullMergeDirection( WorkpaneView target, boolean auto ) {
		List<MergeDirection> directions = new ArrayList<>( 4 );

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

	/**
	 * Add a tool to the workpane. The difference between this method and
	 * {@link #openTool(Tool, WorkpaneView, int, boolean)} is that this method
	 * does not fire the tool open events.
	 *
	 * @param tool The tool to open
	 * @param view The tool view to which to add the tool
	 * @param index The tab index in the tool view where to add the tool
	 * @param activate If the tool should be activated when added
	 * @return The tool that was opened
	 */
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

	Tool openTool( Tool tool, WorkpaneView view ) {
		return openTool( tool, view, true );
	}

	private Tool openTool( Tool tool, WorkpaneView view, boolean activate ) {
		return openTool( tool, view, view == null ? 0 : view.getTools().size(), activate );
	}

	public Tool openTool( Tool tool, WorkpaneView view, Placement placement, boolean activate ) {
		if( placement == null ) placement = tool.getPlacement();
		if( view == null ) view = determineViewFromPlacement( placement );
		return openTool( tool, view, activate );
	}

	/**
	 * Open a tool in the workpane. The difference between this method and
	 * {@link #addTool(Tool, WorkpaneView, int, boolean)} is that this method
	 * fires the tool open events.
	 *
	 * @param tool The tool to open
	 * @param view The tool view to which to add the tool
	 * @param index The tab index in the tool view where to add the tool
	 * @return The tool that was opened
	 */
	private Tool openTool( Tool tool, WorkpaneView view, int index, boolean activate ) {
		if( tool.getToolView() != null || getViews().contains( tool.getToolView() ) ) return tool;

		Workpane pane = tool.getWorkpane();
		tool.fireEvent( new ToolEvent( null, ToolEvent.OPENING, pane, tool ) );
		addTool( tool, view, index, activate );
		tool.fireEvent( new ToolEvent( null, ToolEvent.OPENED, pane, tool ) );

		return tool;
	}

	/**
	 * Remove the tool from the workpane. The difference between this method and
	 * {@link #closeTool(Tool)} is that this method does not fire close events.
	 * This method is intended to be used for moving tools between views.
	 *
	 * @param tool The tool to remove
	 * @return The tool that was removed
	 */
	public Tool removeTool( Tool tool ) {
		return removeTool( tool, isAutoMerge() );
	}

	/**
	 * Remove the tool from the workpane. The difference between this method and
	 * {@link #closeTool(Tool, boolean)} is that this method does not fire close
	 * events. This method is intended to be used for moving tools between views.
	 *
	 * @param tool The tool to remove
	 * @param autoMerge If the pane should automatically merge the view
	 * @return The tool that was removed
	 */
	private Tool removeTool( Tool tool, boolean autoMerge ) {
		WorkpaneView view = tool.getToolView();
		if( view == null ) return tool;

		try {
			startOperation();
			view.removeTool( tool );
			if( autoMerge ) pullMerge( view );
		} finally {
			finishOperation( true );
		}

		return tool;
	}

	/**
	 * Close the tool and remove it from the workpane. The difference between this
	 * method and {@link #removeTool(Tool)} is that this method fires the close
	 * events.
	 *
	 * @param tool The tool to close
	 * @return The tool that was closed
	 */
	public Tool closeTool( Tool tool ) {
		return closeTool( tool, true );
	}

	/**
	 * Close the tool and remove it from the workpane. The difference between this
	 * method and {@link #removeTool(Tool, boolean)} is that this method fires the
	 * close events.
	 *
	 * @param tool The tool to close
	 * @param autoMerge If the workpane should automatically merge the tool view
	 * @return The tool that was closed
	 */
	Tool closeTool( Tool tool, boolean autoMerge ) {
		if( tool == null ) return null;
		Workpane pane = tool.getWorkpane();

		// Notify tool listeners of intent to close
		tool.fireEvent( new ToolEvent( null, ToolEvent.CLOSING, pane, tool ) );

		// Remove the tool
		if( tool.getCloseOperation() == CloseOperation.REMOVE ) removeTool( tool, autoMerge );

		// Notify tool listeners of view closure
		tool.fireEvent( new ToolEvent( null, ToolEvent.CLOSED, pane, tool ) );

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
	 * This method moves a tool from where it is, to a different view. The source
	 * view and target view do not have to be in the same workpane but must be in
	 * the same JVM.
	 *
	 * @param sourceTool The tool to move
	 * @param targetView The view to move the tool to
	 * @param side The side the target view was split from
	 * @param index The tab index in the target view
	 */
	public static void moveTool( Tool sourceTool, WorkpaneView targetView, Side side, int index ) {
		Workpane sourcePane = sourceTool.getWorkpane();
		WorkpaneView sourceView = sourceTool.getToolView();
		Workpane targetPane = targetView.getWorkpane();

		// NOTE The next remove and add steps can get messy due to merging views
		// It is possible that when addTool is called the target view no longer
		// exists because it had been auto merged during removeTool. If the source
		// and target views are the same then turn off auto merge because the tool
		// would just go back where it came from otherwise.
		boolean differentViews = sourceView != targetView;
		boolean automerge = sourcePane.isAutoMerge() && differentViews;

		// Dropped on the side of a view...split it
		if( side != null ) targetView = targetPane.split( targetView, side );

		sourcePane.removeTool( sourceTool, automerge );

		int targetViewTabCount = targetView.getTools().size();
		if( index < 0 || index > targetViewTabCount ) index = targetViewTabCount;
		targetPane.addTool( sourceTool, targetView, index, true );
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

	/**
	 * Merge toward a specific side.
	 *
	 * @param edge The edge to merge across
	 * @param direction The side to merge toward
	 * @return True if the merge is successful, false otherwise
	 */
	private boolean merge( WorkpaneEdge edge, Side direction ) {
		if( !canMerge( edge, direction, false ) ) return false;

		Set<WorkpaneView> sources = edge.getViews( getOppositeSide( direction ) );
		Set<WorkpaneView> targets = edge.getViews( direction );

		// Notify the listeners the views will merge
		sources.forEach( source -> fireEvent( new ViewEvent( this, ViewEvent.MERGING, this, source ) ) );

		// Get needed objects.
		WorkpaneEdge farEdge = targets.iterator().next().getEdge( direction );

		// Extend the source views and edges.
		for( WorkpaneView source : sources ) {
			source.setEdge( direction, farEdge );

			if( source.getEdge( getSideAtLeft( direction ) ).getEdge( direction ) == edge ) {
				source.getEdge( getSideAtLeft( direction ) ).setEdge( direction, farEdge );
			}
			if( source.getEdge( getSideAtRight( direction ) ).getEdge( direction ) == edge ) {
				source.getEdge( getSideAtRight( direction ) ).setEdge( direction, farEdge );
			}
			farEdge.getViews( getOppositeSide( direction ) ).add( source );
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
				closeTool( tool, false );
				addTool( tool, closestSource );
			}

			// Clean up target edges.
			cleanupTargetEdge( target, direction );
			cleanupTargetEdge( target, getOppositeSide( direction ) );
			cleanupTargetEdge( target, getSideAtLeft( direction ) );
			cleanupTargetEdge( target, getSideAtRight( direction ) );

			// Remove the target view.
			removeView( target );
		}

		// Remove the edge.
		edge.getWorkpane().removeEdge( edge );
		edge.setEdge( direction, null );
		edge.setEdge( getOppositeSide( direction ), null );
		edge.setEdge( getSideAtLeft( direction ), null );
		edge.setEdge( getSideAtRight( direction ), null );

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
		edge.getViews( getOppositeSide( direction ) ).remove( target );

		// If there are no more associated views, remove the edge.
		if( !edge.isWall() && edge.getViews( direction ).size() == 0 && edge.getViews( getOppositeSide( direction ) ).size() == 0 ) removeEdge( edge );
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

		// Fire the splitting event.
		fireEvent( new ViewEvent( this, ViewEvent.SPLITTING, this, source ) );

		return isDockSpace( Side.TOP, source ) ? getTopDockView() : newTopView( source, source.getEdge( Side.LEFT ), source.getEdge( Side.RIGHT ), percent );
	}

	private WorkpaneView splitSouth( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the splitting event.
		fireEvent( new ViewEvent( this, ViewEvent.SPLITTING, this, source ) );

		return isDockSpace( Side.BOTTOM, source ) ? getBottomDockView() : newBottomView( source,
			source.getEdge( Side.LEFT ),
			source.getEdge( Side.RIGHT ),
			percent
		);
	}

	private WorkpaneView splitWest( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the splitting event.
		fireEvent( new ViewEvent( this, ViewEvent.SPLITTING, this, source ) );

		return isDockSpace( Side.LEFT, source ) ? getLeftDockView() : newLeftView( source, source.getEdge( Side.TOP ), source.getEdge( Side.BOTTOM ), percent );
	}

	private WorkpaneView splitEast( WorkpaneView source, double percent ) {
		// Check the parameters.
		if( source == null ) throw new IllegalArgumentException( "WorkpaneView cannot be null." );
		if( percent < 0f || percent > 1f ) throw new IllegalArgumentException( "Percent must be in range 0 - 1." );

		// Fire the splitting event.
		fireEvent( new ViewEvent( this, ViewEvent.SPLITTING, this, source ) );

		return isDockSpace( Side.RIGHT, source ) ? getRightDockView() : newRightView( source, source.getEdge( Side.TOP ), source.getEdge( Side.BOTTOM ), percent );
	}

	boolean isDockSpace( Side side, WorkpaneView source ) {
		if( source == null ) return false;

		WorkpaneEdge leftTurn = source.getEdge( getSideAtLeft( side ) );
		WorkpaneEdge direct = source.getEdge( side );
		WorkpaneEdge rightTurn = source.getEdge( getSideAtRight( side ) );

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

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge().setOrientation( Orientation.HORIZONTAL );
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

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge().setOrientation( Orientation.VERTICAL );
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

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge().setOrientation( Orientation.VERTICAL );
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

		// Create the new edge.
		WorkpaneEdge newEdge = new WorkpaneEdge().setOrientation( Orientation.HORIZONTAL );
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

	private static class MergeDirection implements Comparable<MergeDirection> {

		Side direction;

		int weight;

		MergeDirection( WorkpaneView target, Side direction ) {
			this.direction = direction;
			this.weight = getMergeWeight( target, direction );
			log.log( Log.TRACE, "Direction: " + direction + "  Weight: " + weight );
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

	private static class DefaultDropHandler implements DropListener {

		public TransferMode[] getSupportedModes( Tool tool ) {
			return MOVE_ONLY;
		}

		public void handleDrop( DropEvent event ) throws Exception {
			// NOTE If the event source is null the drag came from outside the program
			if( event.getSource() == null || event.getTransferMode() != TransferMode.MOVE ) return;

			Tool sourceTool = event.getSource();
			WorkpaneView targetView = event.getTarget();
			Workpane targetPane = targetView.getWorkpane();
			int index = event.getIndex();
			Side side = event.getSide();
			boolean droppedOnArea = event.getArea() == DropEvent.Area.TOOL_AREA;

			// Check if being dropped on self
			if( droppedOnArea && side == null && sourceTool == targetView.getActiveTool() ) return;
			moveTool( sourceTool, targetView, side, index );
		}

	}

}
