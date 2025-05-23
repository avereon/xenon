package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.zerra.color.Colors;
import com.avereon.zerra.color.Paints;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.BorderStroke;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import lombok.CustomLog;
import lombok.Getter;

import static com.avereon.xenon.UiWorkareaFactory.*;

@Getter
@CustomLog
public final class UiFactory {

	public static final double PAD = BorderStroke.THICK.getTop();

	public static final String PARENT_SPACE_ID = "space-id";

	public static final String PARENT_AREA_ID = "area-id";

	public static final String PARENT_VIEW_ID = "view-id";

	/**
	 * @deprecated Use {@link #PARENT_SPACE_ID} instead.
	 */
	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	public static final String PARENT_WORKSPACE_ID = "workspace-id";

	/**
	 * @deprecated Use {@link #PARENT_AREA_ID} instead.
	 */
	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	public static final String PARENT_WORKPANE_ID = "workpane-id";

	/**
	 * @deprecated Use {@link #PARENT_VIEW_ID} instead.
	 */
	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	public static final String PARENT_WORKPANEVIEW_ID = "workpaneview-id";

	public static final String ICON = "icon";

	public static final String TITLE = "title";

	public static final String NAME = "name";

	public static final String DESCRIPTION = "description";

	public static final String ORDER = "order";

	public static final String ACTIVE = "active";

	public static final String MAXIMIZED = "maximized";

	public static final String PAINT = "paint";

	public static final String COLOR = "color";

	private final Xenon program;

	/**
	 * Flag to indicate this UI factory is being used to restore a previous state.
	 * This flag is used to prevent the factory from overwriting the state of the
	 * UI in the settings.
	 */
	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private final boolean restore;

	public UiFactory( Xenon program ) {
		this( program, false );
	}

	public void reset() {
		getProgram().getSettingsManager().getSettings( ProgramSettings.UI ).delete();
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	public UiFactory( Xenon program, boolean restore ) {
		this.program = program;
		this.restore = restore;
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	public Workarea create() {
		return create( IdGenerator.getId() );
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	Workarea create( String id ) {
		LinearGradient paint = new LinearGradient( 0, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop( 0, Color.BLUEVIOLET.darker().darker() ), new Stop( 1, Color.TRANSPARENT ) );

		Workarea workarea = new Workarea();
		workarea.setUid( id );
		workarea.setPaint( paint );
		workarea.setIcon( "workarea" );

		setupWorkareaSettings( workarea );
		setupWorkpaneSettings( workarea );

		return workarea;
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void setupWorkareaSettings( Workarea workarea ) {
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, workarea.getUid() );

		// Restore state from settings
		workarea.setPaint( Paints.parse( settings.get( PAINT, Paints.toString( workarea.getPaint() ) ) ) );
		workarea.setColor( Colors.parse( settings.get( COLOR, Colors.toString( workarea.getColor() ) ) ) );
		workarea.setName( settings.get( NAME, workarea.getName() ) );
		workarea.setActive( settings.get( ACTIVE, Boolean.class, workarea.isActive() ) );

		// Save new state to settings
		settings.set( PAINT, Paints.toString( workarea.getPaint() ) );
		settings.set( COLOR, Colors.toString( workarea.getColor() ) );
		settings.set( NAME, workarea.getName() );
		settings.set( ACTIVE, workarea.isActive() );

		// Add the change listeners
		workarea.paintProperty().addListener( ( v, o, n ) -> settings.set( PAINT, Paints.toString( n ) ) );
		workarea.nameProperty().addListener( ( v, o, n ) -> settings.set( NAME, n ) );
		workarea.activeProperty().addListener( ( v, o, n ) -> settings.set( ACTIVE, n ) );
		workarea.workspaceProperty().addListener( ( v, o, n ) -> settings.set( UiFactory.PARENT_WORKSPACE_ID, n == null ? null : n.getUid() ) );

		if( !restore ) {
			// Save new state to settings
			settings.set( VIEW_ACTIVE, workarea.getActiveView() == null ? null : workarea.getActiveView().getUid() );
			settings.set( VIEW_DEFAULT, workarea.getDefaultView() == null ? null : workarea.getDefaultView().getUid() );
			settings.set( VIEW_MAXIMIZED, workarea.getMaximizedView() == null ? null : workarea.getMaximizedView().getUid() );

			// Setup existing views and edges
			workarea.getEdges().forEach( e -> setupWorkpaneEdgeSettings( workarea, e ) );
			workarea.getViews().forEach( v -> setupWorkpaneViewSettings( workarea, v ) );
		}

		// Add the change listeners
		workarea.topDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_TOP_SIZE, newValue ) );
		workarea.leftDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_LEFT_SIZE, newValue ) );
		workarea.rightDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_RIGHT_SIZE, newValue ) );
		workarea.bottomDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_BOTTOM_SIZE, newValue ) );
		workarea.activeViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_ACTIVE, n == null ? null : n.getUid() ) );
		workarea.defaultViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_DEFAULT, n == null ? null : n.getUid() ) );
		workarea.maximizedViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_MAXIMIZED, n == null ? null : n.getUid() ) );
		workarea.getChildrenUnmodifiable().addListener( (ListChangeListener<? super Node>)c -> processWorkareaChildrenChanges( workarea, c ) );
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void setupWorkpaneSettings( Workpane workpane ) {
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.PANE, workpane.getUid() );
		settings.set( PARENT_WORKPANE_ID, workpane.getUid() );

		if( !restore ) {
			// Save new state to settings
			settings.set( VIEW_ACTIVE, workpane.getActiveView() == null ? null : workpane.getActiveView().getUid() );
			settings.set( VIEW_DEFAULT, workpane.getDefaultView() == null ? null : workpane.getDefaultView().getUid() );
			settings.set( VIEW_MAXIMIZED, workpane.getMaximizedView() == null ? null : workpane.getMaximizedView().getUid() );

			// Setup existing views and edges
			workpane.getEdges().forEach( e -> setupWorkpaneEdgeSettings( workpane, e ) );
			workpane.getViews().forEach( v -> setupWorkpaneViewSettings( workpane, v ) );
		}

		// Add the change listeners
		workpane.topDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_TOP_SIZE, newValue ) );
		workpane.leftDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_LEFT_SIZE, newValue ) );
		workpane.rightDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_RIGHT_SIZE, newValue ) );
		workpane.bottomDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( UiWorkareaFactory.DOCK_BOTTOM_SIZE, newValue ) );
		workpane.activeViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_ACTIVE, n == null ? null : n.getUid() ) );
		workpane.defaultViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_DEFAULT, n == null ? null : n.getUid() ) );
		workpane.maximizedViewProperty().addListener( ( v, o, n ) -> settings.set( VIEW_MAXIMIZED, n == null ? null : n.getUid() ) );
		workpane.getChildrenUnmodifiable().addListener( (ListChangeListener<? super Node>)c -> processWorkareaChildrenChanges( workpane, c ) );
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void processWorkareaChildrenChanges( Workpane workarea, ListChangeListener.Change<? extends Node> change ) {
		while( change.next() ) {
			change.getAddedSubList().stream().filter( WorkpaneEdge.class::isInstance ).forEach( n -> setupWorkpaneEdgeSettings( workarea, (WorkpaneEdge)n ) );
			change.getAddedSubList().stream().filter( WorkpaneView.class::isInstance ).forEach( n -> setupWorkpaneViewSettings( workarea, (WorkpaneView)n ) );
			change.getRemoved().stream().filter( WorkpaneEdge.class::isInstance ).forEach( n -> removeWorkpaneEdgeSettings( (WorkpaneEdge)n ) );
			change.getRemoved().stream().filter( WorkpaneView.class::isInstance ).forEach( n -> removeWorkpaneViewSettings( (WorkpaneView)n ) );
		}
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void setupWorkpaneEdgeSettings( Workpane workpane, WorkpaneEdge edge ) {
		Settings edgeSettings = program.getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getUid() );
		edgeSettings.set( UiFactory.PARENT_WORKPANE_ID, workpane.getUid() );

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

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void removeWorkpaneEdgeSettings( WorkpaneEdge edge ) {
		String id = edge.getUid();
		if( id == null ) return;
		program.getSettingsManager().getSettings( ProgramSettings.EDGE, id ).delete();
	}

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void setupWorkpaneViewSettings( Workpane workpane, WorkpaneView view ) {
		Settings viewSettings = program.getSettingsManager().getSettings( ProgramSettings.VIEW, view.getUid() );
		viewSettings.set( UiFactory.PARENT_WORKPANE_ID, workpane.getUid() );

		// Restore state from settings
		// NOTE The edge links are restored in the UiRegenerator
		//if( viewSettings.exists( "placement" ) ) view.setPlacement( Workpane.Placement.valueOf( viewSettings.get( "placement" ).toUpperCase() ) );

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

	// TODO Remove in 1.8
	@Deprecated( since = "1.7", forRemoval = true )
	private void removeWorkpaneViewSettings( WorkpaneView view ) {
		String id = view.getUid();
		if( id == null ) return;
		program.getSettingsManager().getSettings( ProgramSettings.VIEW, id ).delete();
	}

}
