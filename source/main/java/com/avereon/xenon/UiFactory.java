package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workspace.Workarea;
import javafx.geometry.Side;
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

	private static final int RESTORE_TOOL_TIMEOUT = 10;

	private Program program;

	public UiFactory( Program program ) {
		this.program = program;
	}

	public Workarea newWorkarea() {
		String id = IdGenerator.getId();
		Workarea workarea = newWorkarea( id );

		Settings viewSettings = program.getSettingsManager().getSettings( ProgramSettings.VIEW, id );
		viewSettings.set( PARENT_WORKPANE_ID, id );
		viewSettings.set( "t", Side.TOP.name().toLowerCase() );
		viewSettings.set( "l", Side.LEFT.name().toLowerCase() );
		viewSettings.set( "r", Side.RIGHT.name().toLowerCase() );
		viewSettings.set( "b", Side.BOTTOM.name().toLowerCase() );
		workarea.getWorkpane().getDefaultView().setSettings( viewSettings );

		return workarea;
	}

	Workarea newWorkarea( String id ) {
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, id );
		Settings workpaneSettings = program.getSettingsManager().getSettings( ProgramSettings.PANE, id );
		workpaneSettings.set( PARENT_WORKAREA_ID, id );

		Workarea workarea = new Workarea();
		workarea.setSettings( settings );
		setupWorkpaneSettings( workarea.getWorkpane(), workpaneSettings );

		return workarea;
	}

	private void setupWorkpaneSettings( Workpane workpane, Settings settings ) {
		workpane.setTopDockSize( settings.get( DOCK_TOP_SIZE, Double.class, 0.2 ) );
		workpane.setLeftDockSize( settings.get( DOCK_LEFT_SIZE, Double.class, 0.2 ) );
		workpane.setRightDockSize( settings.get( DOCK_RIGHT_SIZE, Double.class, 0.2 ) );
		workpane.setBottomDockSize( settings.get( DOCK_BOTTOM_SIZE, Double.class, 0.2 ) );

		workpane.setSettings( settings );

		workpane.topDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_TOP_SIZE, newValue ) );
		workpane.leftDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_LEFT_SIZE, newValue ) );
		workpane.rightDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_RIGHT_SIZE, newValue ) );
		workpane.bottomDockSizeProperty().addListener( ( observable, oldValue, newValue ) -> settings.set( DOCK_BOTTOM_SIZE, newValue ) );
	}

}
