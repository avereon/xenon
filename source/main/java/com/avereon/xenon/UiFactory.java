package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.IdGenerator;
import com.avereon.util.Log;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.BorderStroke;

public class UiFactory {

	public static final double DEFAULT_WIDTH = 960;

	public static final double DEFAULT_HEIGHT = 600;

	public static final double PAD = BorderStroke.THICK.getTop();

	public static final String PARENT_WORKSPACE_ID = "workspace-id";

	public static final String PARENT_WORKAREA_ID = "workarea-id";

	public static final String PARENT_WORKPANE_ID = "workpane-id";

	public static final String PARENT_WORKPANEVIEW_ID = "workpaneview-id";

	private static final String DOCK_TOP_SIZE = "dock-top-size";

	private static final String DOCK_LEFT_SIZE = "dock-left-size";

	private static final String DOCK_RIGHT_SIZE = "dock-right-size";

	private static final String DOCK_BOTTOM_SIZE = "dock-bottom-size";

	private static final System.Logger log = Log.get();

	private final Program program;

	public UiFactory( Program program ) {
		this.program = program;
	}

	public Workarea newWorkarea() {
		return newWorkarea( IdGenerator.getId(), false );
	}

	Workarea newWorkarea( String id, boolean restore ) {
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, id );
		Settings workpaneSettings = program.getSettingsManager().getSettings( ProgramSettings.PANE, id );
		workpaneSettings.set( PARENT_WORKAREA_ID, id );

		Workarea workarea = new Workarea();
		workarea.setProductId( settings.getName() );
		workarea.updateFromSettings( settings );

		Workpane workpane = workarea.getWorkpane();
		workpane.setPaneId( id );

		// When restoring clear all prior nodes before setting up the settings
		if( restore) workpane.clearNodes();
		setupWorkpaneSettings( workarea.getWorkpane(), workpaneSettings );

		return workarea;
	}

	private void setupWorkpaneSettings( Workpane workpane, Settings settings ) {
		// Setup existing nodes
		workpane.getEdges().forEach( e -> setupWorkpaneEdgeSettings( e, settings ) );
		workpane.getViews().forEach( v -> setupWorkpaneViewSettings( v, settings ) );

		workpane.setTopDockSize( settings.get( DOCK_TOP_SIZE, Double.class, 0.2 ) );
		workpane.setLeftDockSize( settings.get( DOCK_LEFT_SIZE, Double.class, 0.2 ) );
		workpane.setRightDockSize( settings.get( DOCK_RIGHT_SIZE, Double.class, 0.2 ) );
		workpane.setBottomDockSize( settings.get( DOCK_BOTTOM_SIZE, Double.class, 0.2 ) );

		// Restore state from settings
		// NOTE The active, default and maximized views are restored in UiRegenerator

		// Add the change listeners
		workpane.topDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_TOP_SIZE, newValue ) );
		workpane.leftDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_LEFT_SIZE, newValue ) );
		workpane.rightDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_RIGHT_SIZE, newValue ) );
		workpane.bottomDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_BOTTOM_SIZE, newValue ) );
		workpane.activeViewProperty().addListener( ( v, o, n ) -> settings.set( "view-active", n == null ? null : n.getViewId() ) );
		workpane.defaultViewProperty().addListener( ( v, o, n ) -> settings.set( "view-default", n == null ? null : n.getViewId() ) );
		workpane.maximizedViewProperty().addListener( ( v, o, n ) -> settings.set( "view-maximized", n == null ? null : n.getViewId() ) );
		workpane.getChildrenUnmodifiable().addListener( (ListChangeListener<? super Node>)( c ) -> {
			while( c.next() ) {
				for( Node n : c.getAddedSubList() ) {
					if( n instanceof WorkpaneEdge ) setupWorkpaneEdgeSettings( (WorkpaneEdge)n, settings );
					if( n instanceof WorkpaneView ) setupWorkpaneViewSettings( (WorkpaneView)n, settings );
				}
				for( Node n : c.getRemoved() ) {
					if( n instanceof WorkpaneEdge ) removeWorkpaneEdgeSettings( (WorkpaneEdge)n, settings );
					if( n instanceof WorkpaneView ) removeWorkpaneViewSettings( (WorkpaneView)n, settings );
				}
			}
		} );

		// Store the current values
		settings.set( "view-active", workpane.getActiveView() == null ? null : workpane.getActiveView().getViewId() );
		settings.set( "view-default", workpane.getDefaultView() == null ? null : workpane.getDefaultView().getViewId() );
		settings.set( "view-maximized", workpane.getMaximizedView() == null ? null : workpane.getMaximizedView().getViewId() );
	}

	private void setupWorkpaneEdgeSettings( WorkpaneEdge edge, Settings settings ) {
		Settings edgeSettings = settings.getNode( ProgramSettings.EDGE, edge.getEdgeId() );
		edgeSettings.set( UiFactory.PARENT_WORKPANE_ID, settings.getName() );

		// Restore state from settings
		if( edgeSettings.exists( "orientation" ) ) edge.setOrientation( Orientation.valueOf( edgeSettings.get( "orientation" ).toUpperCase() ) );
		if( edgeSettings.exists( "position" ) ) edge.setPosition( edgeSettings.get( "position", Double.class ) );
		// NOTE The edges are restored in the UiRegenerator

		// Add the change listeners
		edge.positionProperty().addListener( ( v, o, n ) -> edgeSettings.set( "position", n ) );
		edge.orientationProperty().addListener( ( v, o, n ) -> edgeSettings.set( "orientation", n ) );
		edge.topEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "t", n == null ? null : n.getEdgeId() ) );
		edge.leftEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "l", n == null ? null : n.getEdgeId() ) );
		edge.rightEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "r", n == null ? null : n.getEdgeId() ) );
		edge.bottomEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "b", n == null ? null : n.getEdgeId() ) );

		// Store the current values
		edgeSettings.set( "position", edge.getPosition() );
		edgeSettings.set( "orientation", edge.getOrientation().name().toLowerCase() );
		edgeSettings.set( "t", edge.getTopEdge() == null ? null : edge.getTopEdge().getEdgeId() );
		edgeSettings.set( "l", edge.getLeftEdge() == null ? null : edge.getLeftEdge().getEdgeId() );
		edgeSettings.set( "r", edge.getRightEdge() == null ? null : edge.getRightEdge().getEdgeId() );
		edgeSettings.set( "b", edge.getBottomEdge() == null ? null : edge.getBottomEdge().getEdgeId() );
		log.log( Log.WARN, "Added: " + edge );
	}

	private void removeWorkpaneEdgeSettings( WorkpaneEdge edge, Settings settings ) {
		String id = edge.getEdgeId();
		// FIXME Some events may happen after the settings are deleted
		if( id != null ) settings.getNode( ProgramSettings.EDGE, id ).delete();
		log.log( Log.WARN, "Removed: " + edge );
	}

	private void setupWorkpaneViewSettings( WorkpaneView view, Settings settings ) {
		Settings viewSettings = settings.getNode( ProgramSettings.VIEW, view.getViewId() );
		viewSettings.set( UiFactory.PARENT_WORKPANE_ID, settings.getName() );

		// Restore state from settings
		if( viewSettings.exists( "placement" ) ) view.setPlacement( Workpane.Placement.valueOf( viewSettings.get( "placement" ).toUpperCase() ) );
		// NOTE The edges are restored in the UiRegenerator

		// Add the change listeners
		view.placementProperty().addListener( ( v, o, n ) -> viewSettings.set( "placement", n == null ? null : n.name().toLowerCase() ) );
		view.topEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "t", n == null ? null : n.getEdgeId() ) );
		view.leftEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "l", n == null ? null : n.getEdgeId() ) );
		view.rightEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "r", n == null ? null : n.getEdgeId() ) );
		view.bottomEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "b", n == null ? null : n.getEdgeId() ) );

		// Store the current values
		viewSettings.set( "placement", view.getPlacement() == null ? null : view.getPlacement().name().toLowerCase() );
		viewSettings.set( "t", view.getTopEdge() == null ? null : view.getTopEdge().getEdgeId() );
		viewSettings.set( "l", view.getLeftEdge() == null ? null : view.getLeftEdge().getEdgeId() );
		viewSettings.set( "r", view.getRightEdge() == null ? null : view.getRightEdge().getEdgeId() );
		viewSettings.set( "b", view.getBottomEdge() == null ? null : view.getBottomEdge().getEdgeId() );
		log.log( Log.WARN, "Added: " + view );
	}

	private void removeWorkpaneViewSettings( WorkpaneView view, Settings settings ) {
		String id = view.getViewId();
		// FIXME Some events may happen after the settings are deleted
		if( id != null ) settings.getNode( ProgramSettings.VIEW, id ).delete();
		log.log( Log.WARN, "Removed: " + view );
	}

}
