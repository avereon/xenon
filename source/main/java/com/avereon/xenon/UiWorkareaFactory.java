package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.zarra.color.Colors;
import com.avereon.zarra.color.Paints;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;

public class UiWorkareaFactory {

	public static final String DOCK_TOP_SIZE = "dock-top-size";

	public static final String DOCK_LEFT_SIZE = "dock-left-size";

	public static final String DOCK_RIGHT_SIZE = "dock-right-size";

	public static final String DOCK_BOTTOM_SIZE = "dock-bottom-size";

	public static final String VIEW_ACTIVE = "view-active";

	public static final String VIEW_DEFAULT = "view-default";

	public static final String VIEW_MAXIMIZED = "view-maximized";

	private final Xenon program;

	public UiWorkareaFactory( Xenon program ) {
		this.program = program;
	}

	public Workarea create() {
		return new Workarea();
	}

	Workarea applyWorkareaSettings( Workarea workarea, Settings settings ) {
		// Restore state from settings
		workarea.setName( settings.get( UiFactory.NAME, workarea.getName() ) );
		workarea.setOrder( settings.get( UiFactory.ORDER, Integer.class, workarea.getOrder() ) );
		workarea.setPaint( Paints.parse( settings.get( UiFactory.PAINT, Paints.toString( workarea.getPaint() ) ) ) );
		workarea.setColor( Colors.parse( settings.get( UiFactory.COLOR, Colors.toString( workarea.getColor() ) ) ) );
		workarea.setActive( settings.get( UiFactory.ACTIVE, Boolean.class, workarea.isActive() ) );

		// Save new state to settings, to save default values immediately
		settings.set( UiFactory.PAINT, Paints.toString( workarea.getPaint() ) );
		settings.set( UiFactory.COLOR, Colors.toString( workarea.getColor() ) );
		settings.set( UiFactory.NAME, workarea.getName() );
		settings.set( UiFactory.ACTIVE, workarea.isActive() );

		return workarea;
	}

	Workarea linkWorkareaSettingsListeners( Workarea workarea, Settings settings ) {
		// Add the change listeners
		workarea.nameProperty().addListener( ( v, o, n ) -> settings.set( UiFactory.NAME, n ) );
		workarea.orderProperty().addListener( ( v, o, n ) -> settings.set( UiFactory.ORDER, n ) );
		workarea.paintProperty().addListener( ( v, o, n ) -> settings.set( UiFactory.PAINT, Paints.toString( n ) ) );
		workarea.activeProperty().addListener( ( v, o, n ) -> settings.set( UiFactory.ACTIVE, n ) );
		workarea.workspaceProperty().addListener( ( v, o, n ) -> settings.set( UiFactory.PARENT_WORKSPACE_ID, n == null ? null : n.getUid() ) );

		// Setup existing views and edges
		workarea.getEdges().forEach( e -> setupEdgeSettings( workarea, e ) );
		workarea.getViews().forEach( v -> setupViewSettings( workarea, v ) );

		// Add the change listeners
		workarea.topDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_TOP_SIZE, newValue ) );
		workarea.leftDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_LEFT_SIZE, newValue ) );
		workarea.rightDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_RIGHT_SIZE, newValue ) );
		workarea.bottomDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_BOTTOM_SIZE, newValue ) );
		workarea.activeViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_ACTIVE, n == null ? null : n.getUid() ) );
		workarea.defaultViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_DEFAULT, n == null ? null : n.getUid() ) );
		workarea.maximizedViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_MAXIMIZED, n == null ? null : n.getUid() ) );
		workarea.getChildrenUnmodifiable().addListener( (ListChangeListener<? super Node>)c -> processAreaChildChanges( workarea, c ) );

		return workarea;
	}

	private void processAreaChildChanges( Workpane workarea, ListChangeListener.Change<? extends Node> change ) {
		while( change.next() ) {
			change.getAddedSubList().stream().filter( WorkpaneEdge.class::isInstance ).forEach( n -> setupEdgeSettings( workarea, (WorkpaneEdge)n ) );
			change.getAddedSubList().stream().filter( WorkpaneView.class::isInstance ).forEach( n -> setupViewSettings( workarea, (WorkpaneView)n ) );
			change.getRemoved().stream().filter( WorkpaneEdge.class::isInstance ).forEach( n -> removeEdgeSettings( (WorkpaneEdge)n ) );
			change.getRemoved().stream().filter( WorkpaneView.class::isInstance ).forEach( n -> removeViewSettings( (WorkpaneView)n ) );
		}
	}

	private void setupEdgeSettings( Workpane workpane, WorkpaneEdge edge ) {
		Settings edgeSettings = program.getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getUid() );
		edgeSettings.set( UiFactory.PARENT_AREA_ID, workpane.getUid() );

		// Restore state from settings
		// NOTE The edge links are restored in the UiRegenerator
		//if( edgeSettings.exists( "orientation" ) ) edge.setOrientation( Orientation.valueOf( edgeSettings.get( "orientation" ).toUpperCase() ) );
		//if( edgeSettings.exists( "position" ) ) edge.setPosition( edgeSettings.get( "position", Double.class ) );

		// Store the current values
		edgeSettings.set( "position", edge.getPosition() );
		edgeSettings.set( "orientation", edge.getOrientation().name().toLowerCase() );
		edgeSettings.set( "t", edge.getTopEdge() == null ? null : edge.getTopEdge().getUid() );
		edgeSettings.set( "l", edge.getLeftEdge() == null ? null : edge.getLeftEdge().getUid() );
		edgeSettings.set( "r", edge.getRightEdge() == null ? null : edge.getRightEdge().getUid() );
		edgeSettings.set( "b", edge.getBottomEdge() == null ? null : edge.getBottomEdge().getUid() );

		// Add the change listeners
		edge.positionProperty().addListener( ( v, o, n ) -> edgeSettings.set( "position", n ) );
		edge.orientationProperty().addListener( ( v, o, n ) -> edgeSettings.set( "orientation", n ) );
		edge.topEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "t", n == null ? null : n.getUid() ) );
		edge.leftEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "l", n == null ? null : n.getUid() ) );
		edge.rightEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "r", n == null ? null : n.getUid() ) );
		edge.bottomEdgeProperty().addListener( ( v, o, n ) -> edgeSettings.set( "b", n == null ? null : n.getUid() ) );
	}

	private void removeEdgeSettings( WorkpaneEdge edge ) {
		String id = edge.getUid();
		if( id == null ) return;
		program.getSettingsManager().getSettings( ProgramSettings.EDGE, id ).delete();
	}

	private void setupViewSettings( Workpane workpane, WorkpaneView view ) {
		Settings viewSettings = program.getSettingsManager().getSettings( ProgramSettings.VIEW, view.getUid() );
		viewSettings.set( UiFactory.PARENT_AREA_ID, workpane.getUid() );

		// Store the current values
		viewSettings.set( "placement", view.getPlacement() == null ? null : view.getPlacement().name().toLowerCase() );
		viewSettings.set( "t", view.getTopEdge() == null ? null : view.getTopEdge().getUid() );
		viewSettings.set( "l", view.getLeftEdge() == null ? null : view.getLeftEdge().getUid() );
		viewSettings.set( "r", view.getRightEdge() == null ? null : view.getRightEdge().getUid() );
		viewSettings.set( "b", view.getBottomEdge() == null ? null : view.getBottomEdge().getUid() );

		// Add the change listeners
		view.placementProperty().addListener( ( v, o, n ) -> viewSettings.set( "placement", n == null ? null : n.name().toLowerCase() ) );
		view.topEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "t", n == null ? null : n.getUid() ) );
		view.leftEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "l", n == null ? null : n.getUid() ) );
		view.rightEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "r", n == null ? null : n.getUid() ) );
		view.bottomEdgeProperty().addListener( ( v, o, n ) -> viewSettings.set( "b", n == null ? null : n.getUid() ) );
	}

	private void removeViewSettings( WorkpaneView view ) {
		String id = view.getUid();
		if( id == null ) return;
		program.getSettingsManager().getSettings( ProgramSettings.VIEW, id ).delete();
	}

}
